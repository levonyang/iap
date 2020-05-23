package com.haizhi.iap.configure.service.impl;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.CollectionCreateOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.configure.component.RegisterCenter;
import com.haizhi.iap.configure.controller.model.Schema;
import com.haizhi.iap.configure.enums.DataType;
import com.haizhi.iap.configure.enums.ImportStatus;
import com.haizhi.iap.configure.exception.ConfigException;
import com.haizhi.iap.configure.model.Component;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.model.FirstMenu;
import com.haizhi.iap.configure.model.SecondMenu;
import com.haizhi.iap.configure.model.SourceFieldMap;
import com.haizhi.iap.configure.repo.ComponentRepo;
import com.haizhi.iap.configure.repo.DataSourceRepo;
import com.haizhi.iap.configure.repo.FirstMenuRepo;
import com.haizhi.iap.configure.repo.SecondMenuRepo;
import com.haizhi.iap.configure.service.DataSourceService;
import com.haizhi.iap.configure.util.DatasourceUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.dbcp.BasicDataSource;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2017/4/13 下午7:35.
 */
@Service
@Slf4j
public class DataSourceServiceImpl implements DataSourceService {

    @Setter
    @Autowired
    DataSourceRepo dataSourceRepo;

    @Setter
    @Autowired
    MongoDatabase mongoDatabase;

    @Setter
    @Autowired
    ArangoDatabase arangoDatabase;

    @Setter
    @Autowired
    @Qualifier("producerTemplate")
    ProducerTemplate template;

    @Setter
    @Autowired
    RegisterCenter registerCenter;

    @Setter
    @Autowired
    ComponentRepo componentRepo;

    @Setter
    @Autowired
    SecondMenuRepo secondMenuRepo;

    @Setter
    @Autowired
    FirstMenuRepo firstMenuRepo;

    @Override
    public List<DataSourceConfig> getSourceConfigList(String dataType, String sourceType,
                                                      Integer importStatus, Integer offset, Integer count) {
        return dataSourceRepo.findConfigByCondition(dataType, sourceType, importStatus, offset, count);
    }

    @Override
    public Long countSourceConfigList(String dataType, String sourceType, Integer importStatus) {
        return dataSourceRepo.countConfigByCondition(dataType, sourceType, importStatus);
    }

    @Override
    public DataSourceConfig findConfigById(Long id) {
        return dataSourceRepo.findConfigById(id);
    }

    @Override
    public DataSourceConfig findConfigWithMapById(Long id) {
        DataSourceConfig config = dataSourceRepo.findConfigById(id);
        config.setFieldMapList(dataSourceRepo.getFieldsBySourceId(id));
        return config;
    }

    @Override
    public DataSourceConfig findConfigByName(String name) {
        return dataSourceRepo.findConfigByName(name);
    }

    @Override
    public List<SourceFieldMap> getFieldsBySourceId(Long datasourceId) {
        return dataSourceRepo.getFieldsBySourceId(datasourceId);
    }

    @Override
    public SourceFieldMap getFieldByFieldId(Long sourceFieldId) {
        return dataSourceRepo.getFieldByFieldId(sourceFieldId);
    }

    @Override
    public List<String> joint(DataSourceConfig config) {
        switch (config.getSourceType().toLowerCase()) {
            case "mysql":
                BasicDataSource ds = DatasourceUtil.getMysqlDataSource(config.getHost(), config.getPort(),
                        config.getDatabase(), config.getUsername(), config.getPassword());
                return listMysqlTables(ds);
            case "sqlserver":

                break;
            case "oracle":

                break;
            case "hive":

                break;
            case "postgresql":

                break;
            default:
                throw new ServiceAccessException(ConfigException.UNSUPPORTED_SOURCE_TYPE);
        }
        return null;
    }

    @Override
    public Schema getSchema(DataSourceConfig config) {
        switch (config.getSourceType().toLowerCase()) {
            case "mysql":
                BasicDataSource ds = DatasourceUtil.getMysqlDataSource(config.getHost(), config.getPort(),
                        config.getDatabase(), config.getUsername(), config.getPassword());

                return getSchema(ds, config.getSourceTable());
            case "sqlserver":

                break;
            case "oracle":

                break;
            case "hive":

                break;
            case "postgresql":

                break;
            default:
                throw new ServiceAccessException(ConfigException.UNSUPPORTED_SOURCE_TYPE);
        }
        return null;
    }

    @Override
    public boolean create(DataSourceConfig config) {
        DataSourceConfig sourceConfig = dataSourceRepo.createConfig(config);
        if (sourceConfig.getId() == null) {
            return false;
        } else {
            if (config.getDataType().equals(DataType.DETAIL.getName())) {
                //创建mongo表 建立主键索引
                mongoDatabase.createCollection(config.getTargetTable());
                MongoCollection col = mongoDatabase.getCollection(config.getTargetTable());
                for (SourceFieldMap map : config.getFieldMapList()) {
                    if (map.getIsKey() == 1) {
                        col.createIndex(new Document(map.getSourceField(), 1));
                        break;
                    }
                }
            } else {
                //创建arango表
                CollectionCreateOptions options = new CollectionCreateOptions().type(CollectionType.EDGES);
                arangoDatabase.createCollection(config.getTargetTable(), options);
            }
            return dataSourceRepo.createFieldMap(config.getId(), config.getFieldMapList());
        }

    }

    @Override
    public boolean delete(Long sourceConfigId) {
        if (registerCenter.getPool().get(sourceConfigId) != null && !registerCenter.getPool().get(sourceConfigId).isInterrupted()) {
            throw new ServiceAccessException(ConfigException.SHUT_IMPORTING_FIRST);
        } else {
            List<Component> componentList = componentRepo.findBySourceConfigId(sourceConfigId);
            if (componentList.size() > 0) {
                //xx(一级)下面的xx,xx控件, 恶心到死的需求TMD
                Map<Long, List<SecondMenu>> map = Maps.newHashMap();
                for (Component component : componentList) {
                    if (map.containsKey(component.getFirstMenuId())) {
                        List<SecondMenu> secondMenus = map.get(component.getFirstMenuId());
                        secondMenus.add(secondMenuRepo.getById(component.getSecondMenuId()));
                        map.replace(component.getFirstMenuId(), secondMenus);
                    } else {
                        map.put(component.getFirstMenuId(), Lists.newArrayList(secondMenuRepo.getById(component.getSecondMenuId())));
                    }
                }

                StringBuffer buffer = new StringBuffer("删除该主题，需要先删除详情页面配置中");
                for (Long firstMenuId : map.keySet()) {
                    FirstMenu firstMenu = firstMenuRepo.getById(firstMenuId);
                    if(firstMenu == null){
                        continue;
                    }
                    buffer.append(firstMenu.getName() + "下面的");
                    for (SecondMenu secondMenu : map.get(firstMenuId)) {
                        buffer.append(secondMenu.getName() + "、");
                    }
                    buffer.replace(buffer.lastIndexOf("、"), buffer.lastIndexOf("、") + 1, "");
                    buffer.append("控件；");
                }
                buffer.replace(buffer.lastIndexOf("；"), buffer.lastIndexOf("；") + 1, "");
                throw new ServiceAccessException(-1, buffer.toString());
            }
        }

        DataSourceConfig config = dataSourceRepo.findConfigById(sourceConfigId);
        boolean configDeleted = dataSourceRepo.deleteConfig(sourceConfigId);
        boolean mapDeleted = dataSourceRepo.deleteFieldMap(sourceConfigId);
        if (config != null) {
            if (config.getDataType().equals(DataType.DETAIL.getName())) {
                //删除生成的mongo表
                MongoCollection col = mongoDatabase.getCollection(config.getTargetTable());
                if (col != null) {
                    col.drop();
                }
            } else {
                //删除生成的arango表
                try {
                    ArangoCollection collection = arangoDatabase.collection(config.getTargetTable());
                    if (collection != null && collection.exists()) {
                        collection.drop();
                        log.info("删除arango collection {}", config.getTargetTable());
                    }
                } catch (ArangoDBException ex) {
                    //这里如果报错，由于是事物的，上面会删不掉
                    log.error("{}", ex);
                }
            }
        }
        return configDeleted && mapDeleted;
    }

    @Override
    public void notifyImport(Long sourceConfigId, Integer updateMode) {
        DataSourceConfig config = dataSourceRepo.findConfigById(sourceConfigId);
        if (config != null) {
            config.setUpdateMode(updateMode);
            if (!updateMode.equals(config.getUpdateMode())) {
                dataSourceRepo.updateMode(sourceConfigId, updateMode);
            }
            template.sendBody("direct:start_import", config);
        }
    }

    @Override
    public void stopImport(Long sourceConfigId) {
        DataSourceConfig config = dataSourceRepo.findConfigById(sourceConfigId);
        if (!config.getImportStatus().equals(ImportStatus.IMPORTING.getCode())) {
            throw new ServiceAccessException(ConfigException.ABORTED_NOT_ALLOWED);
        } else {
            updateConfigStatusWithActualNum(config, ImportStatus.ABORTING);

            template.asyncRequestBody("direct:stop_import", sourceConfigId);
        }
    }

    @Override
    public void updateConfigStatusWithActualNum(DataSourceConfig config, ImportStatus status) {
        if (config.getDataType().equals(DataType.DETAIL.getName())) {
            //mongo获取数量，更新actual_num
            MongoCollection<Document> coll = mongoDatabase.getCollection(config.getTargetTable());
            Long count = coll.count();
            dataSourceRepo.updateConfig(config.getId(), status.getCode(), count);
        } else if (config.getDataType().equals(DataType.GRAPH.getName())) {
            //arango获取数量，更新actual_num
            try {
                ArangoCollection coll = arangoDatabase.collection(config.getTargetTable());
                Long count = coll.count().getCount();
                dataSourceRepo.updateConfig(config.getId(), status.getCode(), count);
            } catch (ArangoDBException ex) {
                dataSourceRepo.updateConfig(config.getId(), status.getCode());
            }
        }
    }

    @Override
    public List<DataSourceConfig> findByStatus(ImportStatus status) {
        if (status == null) {
            return Lists.newArrayList();
        }
        return dataSourceRepo.findConfigByCondition(null, null, status.getCode(), null, null);
    }

    private Schema getSchema(BasicDataSource ds, String tableName) {

        List<String> fields = Lists.newArrayList();
        List<List<Object>> data = Lists.newArrayList();
        Connection conn = null;
        String sql = "select * from " + tableName + " limit 0,5";
        PreparedStatement stmt;
        try {
            conn = ds.getConnection();
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                fields.add(md.getColumnName(i));
            }
            while (rs.next()) {
                List<Object> rowData = Lists.newArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.add(rs.getObject(i));
                }
                data.add(rowData);
            }
        } catch (SQLException e) {
            throw new ServiceAccessException(ConfigException.WRONG_CONN);
        } finally {
            DatasourceUtil.closeMysqlConn(conn);
        }
        Schema schema = new Schema();
        schema.setData(data);
        schema.setFields(fields);
        return schema;
    }

    private List<String> listMysqlTables(BasicDataSource ds) {
        List<String> result = Lists.newArrayList();
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                result.add(rs.getString(3));
            }
        } catch (SQLException e) {
            throw new ServiceAccessException(ConfigException.WRONG_CONN);
        } finally {
            DatasourceUtil.closeMysqlConn(conn);
        }

        return result;
    }

}
