package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.controller.model.Schema;
import com.haizhi.iap.configure.enums.ImportStatus;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.model.SourceFieldMap;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/13 下午7:35.
 */
public interface DataSourceService {

    /**
     * 获取数据列表
     *
     * @return
     */
    List<DataSourceConfig> getSourceConfigList(String dataType, String sourceType,
                                               Integer importStatus, Integer offset, Integer count);

    /**
     * count数据列表
     *
     * @return
     */
    Long countSourceConfigList(String dataType, String sourceType, Integer importStatus);

    /**
     * 根据id去查
     */
    DataSourceConfig findConfigById(Long id);

    DataSourceConfig findConfigWithMapById(Long id);

    /**
     * 根据名字查
     */
    DataSourceConfig findConfigByName(String name);

    /**
     * 根据数据列表的id获取表的字段信息
     *
     * @param datasourceId
     * @return
     */
    List<SourceFieldMap> getFieldsBySourceId(Long datasourceId);

    SourceFieldMap getFieldByFieldId(Long sourceFieldId);

    /**
     * 对接数据源,返回库中的表名/集合名
     *
     * @param config
     * @return
     */
    List<String> joint(DataSourceConfig config);

    Schema getSchema(DataSourceConfig config);

    boolean create(DataSourceConfig config);

    boolean delete(Long sourceConfigId);

    void notifyImport(Long sourceConfigId, Integer updateMode);

    void stopImport(Long sourceConfigId);

    void updateConfigStatusWithActualNum(DataSourceConfig config, ImportStatus status);

    List<DataSourceConfig> findByStatus(ImportStatus status);
}
