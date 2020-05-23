package com.haizhi.iap.configure.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.configure.enums.CountForm;
import com.haizhi.iap.configure.exception.ConfigException;
import com.haizhi.iap.configure.model.*;
import com.haizhi.iap.configure.service.*;
import com.haizhi.iap.configure.util.ComputeUtil;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chenbo on 17/5/25.
 */
@Api(tags="【数据配置-真实数据模块】获取配置后对应公司的真实数据")
@Slf4j
@RestController
@RequestMapping("/config/real_data")
public class DetailConfigController {

    @Setter
    @Autowired
    DataSourceService dataSourceService;

    @Setter
    @Autowired
    CollectionService collectionService;

    @Setter
    @Autowired
    ComponentService componentService;

    @Setter
    @Autowired
    ItemService itemService;

    @Setter
    @Autowired
    ParamService paramService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    Wrapper getDataConfig(@RequestParam("component_id") Long componentId,
                          @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
                          @RequestParam("company_name") String companyName) {

        if (componentId.equals(0L) || componentId == null) {
            return ConfigException.MISS_COMPONENT_ID.get();
        }
        if (Strings.isNullOrEmpty(companyName)) {
            return ConfigException.NO_COMPANY_NAME.get();
        }

        Component component = componentService.getComponentById(componentId);
        if (component == null || component.getId().equals(0L)) {
            return ConfigException.NOT_EXIST_COMPONENT.get();
        }

        Map<String, Object> response = Maps.newHashMap();

        //获取数据列表和参数列表
        String[] dataSourceIds = component.getDatasourceIds().split(",");
        Map<Long, DataSourceConfig> dataSourceMap = Maps.newHashMap();
        Map<Long, Param> paramMap = Maps.newHashMap();
        for (int i = 0; i < dataSourceIds.length; i++) {
            if (dataSourceIds[i] == null || dataSourceIds[i].equals(0l)) {
                continue;
            }
            Long sourceId;
            try {
                sourceId = Long.parseLong(dataSourceIds[i]);
            } catch (NumberFormatException ex) {
                log.error("{}", ex);
                continue;
            }
            DataSourceConfig dataSourceConfig = dataSourceService.findConfigById(sourceId);
            dataSourceMap.put(sourceId, dataSourceConfig);

            Param param = paramService.getParamByDataAndComponent(sourceId, componentId);
            if (param != null && !param.getId().equals(0l)) {
                paramMap.put(sourceId, param);
            }
        }

        //获取数据列表对应的真实数据
        Map<String, List<Map>> dataMap = Maps.newHashMap();
        long total = 0;
        for (Long sourceId : dataSourceMap.keySet()) {  //TODO后期不需要分页参数，待修改
            Param param = paramMap.get(sourceId);
            List<SourceFieldMap> sourceFieldMapList = dataSourceService.getFieldsBySourceId(sourceId);
            String keyFieldName = null;
            boolean keyFieldFound = false;
            boolean orderFieldFound = false;
            for (SourceFieldMap map : sourceFieldMapList) {

                if (map.getIsKey() == 1) {
                    keyFieldName = map.getSourceField();
                    keyFieldFound = true;
                }

                if (param.getOrderKey() == null) {
                    if (keyFieldFound) {
                        break;
                    }
                } else if (map.getId().equals(param.getOrderKey())) {
                    param.setOrderFieldName(map.getSourceField());
                    orderFieldFound = true;
                }

                if (keyFieldFound && orderFieldFound) {
                    break;
                }
            }
            int count = component.getPageCount();

            List<Map> dbObjs = collectionService.getCollectionByNameAndCondition(dataSourceMap.get(sourceId).getTargetTable(),
                    keyFieldName, offset, count, companyName, param);
            if (dbObjs != null && dbObjs.size() > 0) {
                dataMap.put(dataSourceMap.get(sourceId).getTargetTable(), dbObjs);
            }

            if(component.getIsPage().equals(1)){
                total += collectionService.countAllByNameAndCondition(
                        dataSourceMap.get(sourceId).getTargetTable(), keyFieldName, companyName);
            }
        }
        response.put("total", total);
        //获取页面配置信息
        List<Item> items = itemService.getItems(component.getId());
        Map<Item, SourceFieldMap> fieldMap = Maps.newHashMap();

        //组件下没有元素，直接返回
        if (dataMap.size() <= 0 || items.size() < 1) {
            response.put("total", 0);
            response.put("source_data", null);
            response.put("source_count", 0);
            return Wrapper.OKBuilder.data(response).build();
        }

        for (Item item : items) {
            SourceFieldMap sourceFieldMap = dataSourceService.getFieldByFieldId(item.getSourceFieldId());
            if (sourceFieldMap != null) {
                fieldMap.put(item, sourceFieldMap);
            }
        }

        if (component.getType() == 1) {//横向表格
            Map<String, Map<Long, String>> data = Maps.newHashMap();
            Map<Long, String> posCountData = Maps.newHashMap();
            Map<Long, String> posNoCountData = Maps.newHashMap();

            for (Item item : fieldMap.keySet()) {
                SourceFieldMap field = fieldMap.get(item);
                List<Map> dboList = dataMap.get(dataSourceMap.get(field.getSourceConfigId()).getTargetTable());

                if (dboList != null && dboList.size() > 0) {
                    //计算器类型是支持纵向数据源的
                    if (item.getEleType().equals(1)) {
//                        if (field.getType().equals(0))
//                            return ConfigException.KEY_IS_NOT_NUM.get();

                        if (item.getType().equals(2)) {
                            posCountData.put(item.getId(), computeValue(item.getCountForm(), field.getSourceField(), dboList));
                        } else {
                            posNoCountData.put(item.getId(), computeValue(item.getCountForm(), field.getSourceField(), dboList));
                        }
                    } else {
                        //TODO 用到的表的数据是一份,横向表格不能选纵向数据源,后期会校验,现默认取第一个集合的数据
                        Map dbo = dboList.get(0);
                        if (dbo != null && dbo.get(field.getSourceField()) != null) {
                            posNoCountData.put(item.getId(), dbo.get(field.getSourceField()).toString());
                        }
                    }
                }
            }
            data.put("pos_nocount", posNoCountData);
            data.put("pos_count", posCountData);

            response.put("source_data", data);
            response.put("source_count", posNoCountData.size() + posCountData.size());

        } else {
            //关联的列表性质的数据源
            Map<String, List<Map>> posNoCountDataLists = Maps.newHashMap();
            for (Item item : fieldMap.keySet()) {
                SourceFieldMap field = fieldMap.get(item);
                if (!item.getType().equals(2)) {//非计算器"位置"的数据源
                    String sourceName = dataSourceMap.get(field.getSourceConfigId()).getTargetTable();
                    posNoCountDataLists.put(sourceName, dataMap.get(sourceName));
                }
            }
            List<Map> posNoCountDataList = (List<Map>) posNoCountDataLists.values().toArray()[0];

            List<Item> posCountItemList = fieldMap.keySet().stream().filter(item -> item.getType().equals(2)).collect(Collectors.toList());
            List<Item> posNoCountItemList = fieldMap.keySet().stream().filter(item -> !item.getType().equals(2)).collect(Collectors.toList());

            if (component.getType() == 2) {//纵向表格

                Map<String, Object> data = Maps.newHashMap();
                Map<Long, String> posCountData = Maps.newHashMap();
                List<Map<Long, String>> posNoCountData = Lists.newArrayList();

                for (Map dbo : posNoCountDataList) {
                    Map<Long, String> posNoCountItem = Maps.newHashMap();
                    for (Item item : posNoCountItemList) {
                        SourceFieldMap field = fieldMap.get(item);

                        if (item.getEleType().equals(1)) {
//                            if (field.getType().equals(0))
//                                return ConfigException.KEY_IS_NOT_NUM.get();
                            posNoCountItem.put(item.getId(), computeValue(item.getCountForm(), field.getSourceField(), posNoCountDataList));
                        } else {
                            //TODO 多个数据源，在此处做关联匹配, 需要关联字段的配置

                            if (dbo.get(field.getSourceField()) != null) {
                                posNoCountItem.put(item.getId(), dbo.get(field.getSourceField()).toString());
                            }
                        }
                    }
                    posNoCountData.add(posNoCountItem);
                }

                for (Item item : posCountItemList) {
                    SourceFieldMap field = fieldMap.get(item);
//                    if (field.getType().equals(0)) {
//                        return ConfigException.KEY_IS_NOT_NUM.get();
//                    }
                    posCountData.put(item.getId(), computeValue(item.getCountForm(), field.getSourceField(), dataMap.get(dataSourceMap.get(field.getSourceConfigId()).getTargetTable())));
                }

                data.put("pos_nocount", posNoCountData);
                data.put("pos_count", posCountData);

                response.put("source_data", data);
                response.put("source_count", posNoCountData.size());

            } else {//卡片
                Map<String, Object> data = Maps.newHashMap();
                List<Map<String, Object>> posNoCountData = Lists.newArrayList();
                Map<Long, String> posCountData = Maps.newHashMap();

                for (Map dbo : posNoCountDataList) {
                    Map<String, Object> posNoCountItem = Maps.newHashMap();
                    Map<Long, String> topData = Maps.newHashMap();
                    Map<Long, String> bottomData = Maps.newHashMap();

                    for (Item item : posNoCountItemList) {

                        SourceFieldMap field = fieldMap.get(item);
                        //TODO 多个数据源，在此处做关联匹配
                        if (item.getEleType().equals(1)) {
//                            if (field.getType().equals(0))
//                                return ConfigException.KEY_IS_NOT_NUM.get();

                            if (item.getType().equals(1)) {//top
                                topData.put(item.getId(), computeValue(item.getCountForm(), field.getSourceField(), posNoCountDataList));
                            } else if (item.getType().equals(0)) {//bottom
                                bottomData.put(item.getId(), computeValue(item.getCountForm(), field.getSourceField(), posNoCountDataList));
                            }
                        } else {
                            if (item.getType().equals(1)) {//top
                                if (dbo.get(field.getSourceField()) != null) {
                                    topData.put(item.getId(), dbo.get(field.getSourceField()).toString());
                                } else {
                                    topData.put(item.getId(), "--");
                                }
                            } else {//bottom
                                if (dbo.get(field.getSourceField()) != null) {
                                    bottomData.put(item.getId(), dbo.get(field.getSourceField()).toString());
                                } else {
                                    bottomData.put(item.getId(), "--");
                                }
                            }
                        }
                    }

                    posNoCountItem.put("top", topData);
                    posNoCountItem.put("bottom", bottomData);
                    posNoCountData.add(posNoCountItem);
                }

                for (Item item : posCountItemList) {
                    SourceFieldMap field = fieldMap.get(item);
//                    if (field.getType().equals(0))
//                        return ConfigException.KEY_IS_NOT_NUM.get();
                    posCountData.put(item.getId(), computeValue(item.getCountForm(), field.getSourceField(), dataMap.get(dataSourceMap.get(field.getSourceConfigId()).getTargetTable())));
                }

                data.put("pos_nocount", posNoCountData);
                data.put("pos_count", posCountData);

                response.put("source_data", data);
                response.put("source_count", posNoCountData.size());
            }

        }

        return Wrapper.OKBuilder.data(response).build();
    }

    private String computeValue(String countForm, String key, List<Map> dboList) {
        List<String> values = Lists.newArrayList();
        for (Map dbo : dboList) {
            if (dbo.get(key) == null) {//某个集合不含该字段,则不计算
                continue;
            } else {
                values.add(dbo.get(key).toString());
            }
        }

        if (countForm.equals(CountForm.SUM.getName())) {
            return ComputeUtil.computeSum(values);
        } else if (countForm.equals(CountForm.AVG.getName())) {
            return ComputeUtil.computeAvg(values);
        } else {
            return "0";
        }
    }
}
