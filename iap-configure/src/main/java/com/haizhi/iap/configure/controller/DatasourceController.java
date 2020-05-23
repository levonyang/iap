package com.haizhi.iap.configure.controller;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.configure.component.RegisterCenter;
import com.haizhi.iap.configure.exception.ConfigException;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.model.SourceFieldMap;
import com.haizhi.iap.configure.service.DataSourceService;

/**
 * Created by chenbo on 17/5/25.
 */
@Api(tags="【数据配置-主题模块】对主题进行增删改查")
@RestController
@RequestMapping(value = "/config/datasource")
public class DatasourceController {

    @Setter
    @Autowired
    DataSourceService dataSourceService;

    @Setter
    @Autowired
    RegisterCenter registerCenter;

    /**
     * 对接获取schema名列表
     *
     * @param config
     */
    @RequestMapping(value = "/joint", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper testSource(@RequestBody DataSourceConfig config) {
        if (Strings.isNullOrEmpty(config.getSourceType())) {
            return ConfigException.MISS_SOURCE_TYPE.get();
        }
        if (Strings.isNullOrEmpty(config.getHost())) {
            return ConfigException.MISS_HOST.get();
        }
        if (config.getPort() == null) {
            return ConfigException.MISS_PORT.get();
        }
        if (Strings.isNullOrEmpty(config.getUsername())) {
            return ConfigException.MISS_USERNAME.get();
        }
        if (Strings.isNullOrEmpty(config.getDatabase())) {
            return ConfigException.MISS_DATABASE.get();
        }
        return Wrapper.OKBuilder.data(dataSourceService.joint(config)).build();
    }

    /**
     * 预览获取schema详情和数据
     *
     * @param config
     */
    @RequestMapping(value = "/preview", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper preview(@RequestBody DataSourceConfig config) {
        if (Strings.isNullOrEmpty(config.getSourceType())) {
            return ConfigException.MISS_SOURCE_TYPE.get();
        }
        if (Strings.isNullOrEmpty(config.getHost())) {
            return ConfigException.MISS_HOST.get();
        }
        if (config.getPort() == null) {
            return ConfigException.MISS_PORT.get();
        }
        if (Strings.isNullOrEmpty(config.getUsername())) {
            return ConfigException.MISS_USERNAME.get();
        }
        if (Strings.isNullOrEmpty(config.getDatabase())) {
            return ConfigException.MISS_DATABASE.get();
        }
        if (Strings.isNullOrEmpty(config.getSourceTable())) {
            return ConfigException.MISS_SOURCE_TABLE.get();
        }
        return Wrapper.OKBuilder.data(dataSourceService.getSchema(config)).build();
    }

    /**
     * 创建数据主题
     *
     * @param config
     * @return
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Wrapper create(@RequestBody DataSourceConfig config) {
        if (Strings.isNullOrEmpty(config.getSourceType())) {
            return ConfigException.MISS_SOURCE_TYPE.get();
        }
        if (Strings.isNullOrEmpty(config.getName())) {
            return ConfigException.MISS_NAME.get();
        } else if (dataSourceService.findConfigByName(config.getName()) != null) {
            return ConfigException.NAME_ALREADY_EXISTS.get();
        }
        if (Strings.isNullOrEmpty(config.getDataType())) {
            return ConfigException.MISS_DATA_TYPE.get();
        }
        if (Strings.isNullOrEmpty(config.getSourceType())) {
            return ConfigException.MISS_SOURCE_TYPE.get();
        }
        if (Strings.isNullOrEmpty(config.getHost())) {
            return ConfigException.MISS_HOST.get();
        }
        if (config.getPort() == null) {
            return ConfigException.MISS_PORT.get();
        }
        if (Strings.isNullOrEmpty(config.getUsername())) {
            return ConfigException.MISS_USERNAME.get();
        }
        if (Strings.isNullOrEmpty(config.getDatabase())) {
            return ConfigException.MISS_DATABASE.get();
        }
        if (Strings.isNullOrEmpty(config.getSourceTable())) {
            return ConfigException.MISS_SOURCE_TABLE.get();
        }
        if (config.getFieldMapList() == null || config.getFieldMapList().size() < 1) {
            return ConfigException.MISS_FIELD_MAP_LIST.get();
        }
        List<String> sourceFiedNames = Lists.newArrayList();
        List<String> names = Lists.newArrayList();
        if (config.getDataType().equals("graph")) {
            boolean containsFrom = false;
            boolean containsTo = false;
            for (SourceFieldMap map : config.getFieldMapList()) {
                if (Strings.isNullOrEmpty(map.getSourceField()) || Strings.isNullOrEmpty(map.getName())) {
                    return ConfigException.MISS_SOURCE_FIELD_OR_NAME.get();
                }
                if (sourceFiedNames.contains(map.getSourceField())) {
                    return ConfigException.DULPLICATE_SOURCE_FIELD_OR_NAME.get();
                } else {
                    sourceFiedNames.add(map.getSourceField());
                }
                if (names.contains(map.getName())) {
                    return ConfigException.DULPLICATE_SOURCE_FIELD_OR_NAME.get();
                } else {
                    names.add(map.getName());
                }
                if (map.getName().equals("from")) {
                    containsFrom = true;
                } else if (map.getName().equals("to")) {
                    containsTo = true;
                }
            }
            if (!(containsFrom && containsTo)) {
                return ConfigException.MISS_FROM_TO_MAP.get();
            }
        } else {
            int keyNum = 0;
            //判断是否包含多个主键
            for (SourceFieldMap map : config.getFieldMapList()) {
                if (Strings.isNullOrEmpty(map.getSourceField()) || Strings.isNullOrEmpty(map.getName())) {
                    return ConfigException.MISS_SOURCE_FIELD_OR_NAME.get();
                }
                if (sourceFiedNames.contains(map.getSourceField())) {
                    return ConfigException.DULPLICATE_SOURCE_FIELD_OR_NAME.get();
                } else {
                    sourceFiedNames.add(map.getSourceField());
                }
                if (names.contains(map.getName())) {
                    return ConfigException.DULPLICATE_SOURCE_FIELD_OR_NAME.get();
                } else {
                    names.add(map.getName());
                }
                if (map.getIsKey() == 1) {
                    keyNum += 1;
                }
            }
            if (keyNum > 1) {
                return ConfigException.MULTIPLE_KEY.get();
            }
        }
        //生成mongo表名
        config.setTargetTable(config.generateTargetTable());
        return Wrapper.OKBuilder.data(dataSourceService.create(config)).build();
    }

    /**
     * 删除主题
     *
     * @param sourceConfigId
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public Wrapper delete(@RequestParam(value = "source_config_id") Long sourceConfigId) {
        if (sourceConfigId != null) {
            dataSourceService.delete(sourceConfigId);
        }
        return Wrapper.OK;
    }

    /**
     * 主题列表
     *
     * @return
     */
    @RequestMapping(value = "/source_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getDataSourceList(@RequestParam(value = "data_type", defaultValue = "") String dataType,
                                     @RequestParam(value = "source_type", defaultValue = "") String sourceType,
                                     @RequestParam(value = "import_status", defaultValue = "-1") Integer importStatus,
                                     @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                     @RequestParam(value = "count", defaultValue = "10") Integer count) {
        if (Strings.isNullOrEmpty(dataType)) {
            dataType = null;
        }
        if (Strings.isNullOrEmpty(sourceType)) {
            sourceType = null;
        }
        if (importStatus < 0) {
            importStatus = null;
        }
        List<DataSourceConfig> dataSourceConfigs = dataSourceService.getSourceConfigList(dataType, sourceType,
                importStatus, offset, count);
        Long totalCount = dataSourceService.countSourceConfigList(dataType, sourceType, importStatus);
        Map<String, Object> data = new MapBuilder().put("data", dataSourceConfigs)
                .put("total_count", totalCount)
                .build();
        return Wrapper.OKBuilder.data(data).build();
    }

    /**
     * 单个主题详情
     *
     * @return
     */
    @RequestMapping(value = "/source", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getDataSourceList(@RequestParam(value = "source_config_id") Long id) {
        if (id == null) {
            return ConfigException.MISS_SOURCE_CONFIG_ID.get();
        }
        DataSourceConfig dataSourceConfig = dataSourceService.findConfigWithMapById(id);
        return Wrapper.OKBuilder.data(dataSourceConfig).build();
    }

    /**
     * 启动导入
     *
     * @param sourceConfigId
     * @return
     */
    @RequestMapping(value = "/start_import", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper importDatasource(@RequestParam(value = "source_config_id") Long sourceConfigId,
                                    @RequestParam(value = "update_mode") Integer updateMode) {
        if (sourceConfigId == null) {
            return ConfigException.MISS_SOURCE_CONFIG_ID.get();
        }
        if (!(updateMode.equals(0) || updateMode.equals(1))) {
            return ConfigException.WRONG_UPDATE_MODE.get();
        }
        dataSourceService.notifyImport(sourceConfigId, updateMode);
        return Wrapper.OK;
    }

    /**
     * 停止导入
     */
    @RequestMapping(value = "/stop_import", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper stopImport(@RequestParam(value = "source_config_id") Long sourceConfigId) {
        if (sourceConfigId == null) {
            return ConfigException.MISS_SOURCE_CONFIG_ID.get();
        }
        dataSourceService.stopImport(sourceConfigId);
        return Wrapper.OK;
    }

    /**
     * 获取某个数据源的所有字段信息
     *
     * @param sourceConfigId
     * @return
     */
    @RequestMapping(value = "/field_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getFields(@RequestParam("source_config_id") Long sourceConfigId) {
        if (sourceConfigId == null) {
            return ConfigException.MISS_SOURCE_CONFIG_ID.get();
        }
        List<SourceFieldMap> sourceFieldMapList = dataSourceService.getFieldsBySourceId(sourceConfigId);
        return Wrapper.OKBuilder.data(sourceFieldMapList).build();
    }
}
