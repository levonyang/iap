package com.haizhi.iap.configure.service;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.CollectionCreateOptions;
import com.google.common.collect.Lists;
import com.haizhi.iap.configure.component.RegisterCenter;
import com.haizhi.iap.configure.enums.DataType;
import com.haizhi.iap.configure.enums.ImportStatus;
import com.haizhi.iap.configure.enums.UpdateMode;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.model.SourceFieldMap;
import com.haizhi.iap.configure.repo.DataSourceRepo;
import com.haizhi.iap.configure.util.DatasourceUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by chenbo on 2017/10/10.
 * 导入处理过程
 */
@Slf4j
@Service
public class ImportProcess {

    private static final long BATCH_SIZE = 1000;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Setter
    @Autowired
    MongoDatabase mongoDatabase;

    @Setter
    @Autowired
    ArangoDatabase arangoDatabase;

    @Setter
    @Autowired
    DataSourceRepo dataSourceRepo;

    @Setter
    @Autowired
    RegisterCenter registerCenter;

    public void process(DataSourceConfig config) {
        if (config == null) {
            return;
        }

        List<SourceFieldMap> sourceFieldMapList = dataSourceRepo.getFieldsBySourceId(config.getId());
        dataSourceRepo.updateConfig(config.getId(), ImportStatus.IMPORTING.getCode(), 0d);
        log.info("start import datasourceConfig {}", config.getId());
        switch (config.getSourceType()) {
            case "mysql":
                if (config.getDataType().equals(DataType.DETAIL.getName())) {
                    mysqlToMongoImport(config, sourceFieldMapList);
                } else if (config.getDataType().equals(DataType.GRAPH.getName())) {
                    mysqlToArangoImport(config, sourceFieldMapList);
                }
                break;
            case "sqlserver":

                break;
            case "oracle":

                break;
            case "hive":

                break;
            case "postgresql":

                break;
        }
        //成功或失败后,从注册中心删掉
        registerCenter.remove(config.getId());
    }

    private void mysqlToArangoImport(DataSourceConfig config, List<SourceFieldMap> sourceFieldMapList) {
        BasicDataSource dataSource = DatasourceUtil.getMysqlDataSource(config.getHost(), config.getPort(),
                config.getDatabase(), config.getUsername(), config.getPassword());
        String countSql = "select count(1) from " + config.getSourceTable();
        Connection conn = null;
        long count = 0;
        long oldCount = 0;
        ArangoCollection collection = null;
        try {
            conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(countSql);
            while (rs.next()) {
                count = rs.getLong(1);
            }
            //更新数量origin_num
            dataSourceRepo.updateOriginNum(config.getId(), count);

            //批量读取mysql数据
            long cursor = 0;
            String sql = null;
            collection = arangoDatabase.collection(config.getTargetTable());
            if(!collection.exists()){
                log.error("arangodb collection {} not exists.", config.getTargetTable());
                return;
            }
            if (config.getUpdateMode().equals(UpdateMode.OVERLAY.ordinal())) {
                //覆盖得先删数据
//                collection.truncate();//很慢
                collection.drop();
                CollectionCreateOptions options = new CollectionCreateOptions().type(CollectionType.EDGES);
                arangoDatabase.createCollection(config.getTargetTable(), options);
                log.info("truncate arango {} finished.", config.getTargetTable());
            } else {
                //增量先获取原始数据
                oldCount = collection.count().getCount();
            }
            while (cursor <= count && !registerCenter.isShut(config.getId())) {
                sql = String.format("select * from %s limit %d, %d", config.getSourceTable(),
                        cursor, BATCH_SIZE);
                rs = stmt.executeQuery(sql);
                List<BaseDocument> documents = Lists.newArrayList();

                while (rs.next()) {
                    BaseEdgeDocument doc = new BaseEdgeDocument();
                    for (SourceFieldMap map : sourceFieldMapList) {
                        //组织数据
                        if (map.getName().equals("from")) {
                            doc.setFrom(rs.getObject(map.getSourceField()).toString());
                        } else if (map.getName().equals("to")) {
                            doc.setTo(rs.getObject(map.getSourceField()).toString());
                        } else if (rs.getObject(map.getSourceField()) instanceof Date) {
                            doc.addAttribute(map.getSourceField(), dateFormat.format(rs.getObject(map.getSourceField())));
                        } else {
                            doc.addAttribute(map.getSourceField(), rs.getObject(map.getSourceField()));
                        }
                    }
                    documents.add(doc);
                }
                //批量塞mongo
                if (documents.size() > 0) {
                    collection.insertDocuments(documents);
                }
                //更新百分比
                BigDecimal decimal = new BigDecimal(cursor * 100 / count);
                double percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                dataSourceRepo.updateConfig(config.getId(), percent);
                log.info("mysql config {} {} to arangodb percent: {} ", config.getId(), config.getName(), percent);
                //改变游标
                cursor = cursor + BATCH_SIZE;
            }
            if (!registerCenter.isShut(config.getId())) {
                //正常结束
                //导入成功处理,更新数量actual_num
                Long actualNum = collection.count().getCount();
                if (config.getUpdateMode().equals(UpdateMode.INCREMENT.ordinal())) {
                    //增量
                    actualNum = actualNum - oldCount;
                }
                dataSourceRepo.updateConfig(config.getId(), ImportStatus.FINISHED.getCode(), 100d, actualNum);
                log.info("mysql config {} {} to arangodb success! ", config.getId(), config.getName());
            }
        } catch (Exception sqlEx) {
            //导入失败处理,更新数量actual_num
            if (collection != null && collection.exists()) {
                Long actualNum = collection.count().getCount();
                dataSourceRepo.updateConfig(config.getId(), ImportStatus.FAILED.getCode(), actualNum);
            } else {
                dataSourceRepo.updateConfig(config.getId(), ImportStatus.FAILED.getCode());
            }
            log.info("mysql config {} {} to arangodb failed! msg: {}", config.getId(), config.getName(), sqlEx.getMessage());
        } finally {
            DatasourceUtil.closeMysqlConn(conn);
        }
    }

    private void mysqlToMongoImport(DataSourceConfig config, List<SourceFieldMap> sourceFieldMapList) {
        BasicDataSource dataSource = DatasourceUtil.getMysqlDataSource(config.getHost(), config.getPort(),
                config.getDatabase(), config.getUsername(), config.getPassword());
        String countSql = "select count(1) from " + config.getSourceTable();
        Connection conn = null;
        long count = 0;
        long oldCount = 0;
        MongoCollection<Document> collection = null;
        try {
            conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(countSql);
            while (rs.next()) {
                count = rs.getLong(1);
            }
            //更新数量origin_num
            dataSourceRepo.updateOriginNum(config.getId(), count);

            //批量读取mysql数据
            long cursor = 0;
            String sql = null;
            collection = mongoDatabase.getCollection(config.getTargetTable());

            if (config.getUpdateMode().equals(UpdateMode.OVERLAY.ordinal())) {
                //覆盖得先删数据
                collection.deleteMany(new BasicDBObject());
                log.info("truncate mongo {} finished.", config.getTargetTable());
            } else {
                oldCount = collection.count();
            }
            while (cursor <= count && !registerCenter.isShut(config.getId())) {
                sql = String.format("select * from %s limit %d, %d", config.getSourceTable(),
                        cursor, BATCH_SIZE);
                rs = stmt.executeQuery(sql);
                List<Document> documents = Lists.newArrayList();
                while (rs.next()) {
                    Document doc = new Document();
                    for (SourceFieldMap map : sourceFieldMapList) {
                        //组织数据
                        if (rs.getObject(map.getSourceField()) instanceof Date) {
                            doc.put(map.getSourceField(), dateFormat.format(rs.getObject(map.getSourceField())));
                        } else {
                            doc.put(map.getSourceField(), rs.getObject(map.getSourceField()));
                        }
                    }
                    documents.add(doc);
                }
                //批量塞mongo
                if (documents.size() > 0) {
                    collection.insertMany(documents);
                }
                //更新百分比
                BigDecimal decimal = new BigDecimal(cursor * 100 / count);
                double percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                dataSourceRepo.updateConfig(config.getId(), percent);
                log.info("mysql config {} {} to mongo percent: {} ", config.getId(), config.getName(), percent);
                //改变游标
                cursor = cursor + BATCH_SIZE;
            }
            Long actualNum = collection.count();
            //导入成功处理，更新数量actual_num
            if (!registerCenter.isShut(config.getId())) {
                //正常结束
                if (config.getUpdateMode().equals(UpdateMode.INCREMENT.ordinal())) {
                    //增量
                    actualNum = actualNum - oldCount;
                }
                dataSourceRepo.updateConfig(config.getId(), ImportStatus.FINISHED.getCode(), 100d, actualNum);
                log.info("mysql config {} {} to mongo success! ", config.getId(), config.getName());
            }
        } catch (Exception sqlEx) {
            //导入失败处理
            //更新数量actual_num
            if (collection != null) {
                Long actualNum = collection.count();
                dataSourceRepo.updateConfig(config.getId(), ImportStatus.FAILED.getCode(), actualNum);
            } else {
                dataSourceRepo.updateConfig(config.getId(), ImportStatus.FAILED.getCode());
            }
            log.info("mysql config {} {} to mongo failed! msg: {}", config.getId(), config.getName(), sqlEx.getMessage());
        } finally {
            DatasourceUtil.closeMysqlConn(conn);
        }
    }

}
