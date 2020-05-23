package com.haizhi.iap.configure.util;

import com.google.common.collect.Lists;
import com.haizhi.iap.common.factory.MongoFactory;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * Created by chenbo on 2017/10/12.
 * 将某个已有的mongo表迁移到mysql,方便测试
 */
@Slf4j
public class DataMigrationTest {
    private ClassPathXmlApplicationContext context;
    private JdbcTemplate template;
    private MongoDatabase mongoDatabase;

    private static String host = "sm5";
    private static Integer port = 3306;
    private static String database = "test";
    private static String username = "bigdata";
    private static String password = "bigdata";
    private static String mongoUrl = "mongodb://readme:readme@172.16.215.16:40042/app_data?connectTimeoutMS=60000";
    private static String mongoDBName = "app_data";

    @Before
    public void init() {
        context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml",
                "classpath:spring/applicationContext-data.xml");

        //创建datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?Unicode=true&characterEncoding=utf8");
        ds.setUsername(username);
        ds.setPassword(password);
        template = new JdbcTemplate(ds);

        MongoClient mongoClient = MongoFactory.get(mongoUrl);
        mongoDatabase = mongoClient.getDatabase(mongoDBName);
    }

    @Test
    public void migrationBaiduBaipinJob() throws SQLException {
        String tableName = "baidu_baipin_job";

        //判断mysql表存不存在,不存在则创建
        ResultSet rs = template.getDataSource().getConnection().getMetaData().getTables(null, null, tableName, null);
        if (!rs.next()) {
            createBaiduBaipinJobMysqlTable();
        } else {
            String countSql = "select count(1) from " + tableName;
            Long dataCount = template.queryForObject(countSql, Long.class);
            if (dataCount != null && dataCount > 0) {
                log.error("table {} is not empty! ", tableName);
                return;
            }
        }
        //从mongo中批量读取数据
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        long count = collection.count();
        MongoCursor<Document> cursor = collection.find().batchSize(1000).iterator();
        List<Document> batchList = Lists.newArrayList();
        long index = 0;
        while (cursor.hasNext()) {
            batchList.add(cursor.next());
            if (batchList.size() >= 1000) {
                //批量插入mysql
                batchInsertToBaiduBaipinJobMysql(batchList);
                index += batchList.size();
                BigDecimal decimal = new BigDecimal(index * 100 / count);
                double percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                log.info("insert into baidu_baipin_job percent: {}%", percent);

                batchList = Lists.newArrayList();
            }
        }
        if (batchList.size() > 0) {
            batchInsertToBaiduBaipinJobMysql(batchList);
            log.info("insert into baidu_baipin_job percent: {}%", 100);
        }
    }

    @Test
    public void createBaiduBaipinJobMysqlTable() {
        String tableName = "baidu_baipin_job";
        StringBuffer buffer = new StringBuffer("CREATE TABLE `" + tableName + "` (\n" +
                "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `salary` varchar(255) DEFAULT NULL,\n" +
                "  `city` varchar(64) DEFAULT NULL,\n" +
                "  `education` varchar(64) DEFAULT NULL,\n" +
                "  `experience` varchar(64) DEFAULT NULL,\n" +
                "  `source_url` text,\n" +
                "  `source` varchar(255) DEFAULT NULL,\n" +
                "  `job` varchar(255) DEFAULT NULL,\n" +
                "  `publish_date` varchar(64) DEFAULT NULL,\n" +
                "  `company_name` varchar(255) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  KEY `idx_company_name` (`company_name`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        template.update(buffer.toString());
    }

    private void batchInsertToBaiduBaipinJobMysql(List<Document> batchList) {
        String tableName = "baidu_baipin_job";
        String sql = "insert into " + tableName +
                "(salary, city, education, experience, source_url, source, job, publish_date, company_name) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        template.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                if (batchList.get(i).get("salary") instanceof String) {
                    ps.setString(1, batchList.get(i).getString("salary"));
                } else {
                    ps.setString(1, batchList.get(i).get("salary").toString());
                }
                if (batchList.get(i).get("city") instanceof String) {
                    ps.setString(2, batchList.get(i).getString("city"));
                } else {
                    ps.setString(2, batchList.get(i).get("city").toString());
                }
                if (batchList.get(i).get("education") instanceof String) {
                    ps.setString(3, batchList.get(i).getString("education"));
                } else {
                    ps.setString(3, batchList.get(i).get("education").toString());
                }
                if (batchList.get(i).get("experience") instanceof String) {
                    ps.setString(4, batchList.get(i).getString("experience"));
                } else {
                    ps.setString(4, batchList.get(i).get("experience").toString());
                }
                if (batchList.get(i).get("source_url") instanceof String) {
                    ps.setString(5, batchList.get(i).getString("source_url"));
                } else {
                    ps.setString(5, batchList.get(i).get("source_url").toString());
                }
                if (batchList.get(i).get("source") instanceof String) {
                    ps.setString(6, batchList.get(i).getString("source"));
                } else {
                    ps.setString(6, batchList.get(i).get("source").toString());
                }
                if (batchList.get(i).get("job") instanceof String) {
                    ps.setString(7, batchList.get(i).getString("job"));
                } else {
                    ps.setString(7, batchList.get(i).get("job").toString());
                }
                if (batchList.get(i).get("publish_date") instanceof String) {
                    ps.setString(8, batchList.get(i).getString("publish_date"));
                } else {
                    ps.setString(8, batchList.get(i).get("publish_date").toString());
                }
                if (batchList.get(i).get("company_name") instanceof String) {
                    ps.setString(9, batchList.get(i).getString("company_name"));
                } else {
                    ps.setString(9, batchList.get(i).get("company_name").toString());
                }
            }

            @Override
            public int getBatchSize() {
                return batchList.size();
            }
        });
    }
}
