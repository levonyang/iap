package com.haizhi.iap.configure.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.configure.enums.CountForm;
import com.haizhi.iap.configure.exception.ConfigException;
import com.haizhi.iap.configure.model.*;
import com.haizhi.iap.configure.service.*;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by chenbo on 17/5/24.
 */
@Api(tags="【数据配置-页面配置模块】页面配置查询和修改")
@Slf4j
@RestController
@RequestMapping(value = "/config/content")
public class ContentController {
    @Setter
    @Autowired
    FirstMenuService firstMenuService;

    @Setter
    @Autowired
    SecondMenuService secondMenuService;

    @Setter
    @Autowired
    DataSourceService dataSourceService;

    @Setter
    @Autowired
    ComponentService componentService;

    @Setter
    @Autowired
    ItemService itemService;

    @Setter
    @Autowired
    CollectionService collectionService;

    @Setter
    @Autowired
    ParamService paramService;

    /**
     * 获取数据源列表
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getContent() {
        List<FirstMenu> firstMenus = firstMenuService.getAll();

        List<FirstMenu> result = Lists.newArrayList();

        for (FirstMenu firstMenu : firstMenus) {

            //init二级导航
            List<SecondMenu> secondMenus = secondMenuService.getSecondMenus(firstMenu.getId());

            for (SecondMenu secondMenu : secondMenus) {

                //init组件
                Component component = componentService.getComponent(secondMenu.getId());
                if (component != null && !component.getId().equals(0l)) {

                    String[] dataSourceIds = component.getDatasourceIds().split(",");

                    Map<Long, DataSourceConfig> dataSourceMap = Maps.newHashMap();
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
                    }
                    component.setDataSourceConfigList(Lists.newArrayList(dataSourceMap.values()));

                    //获取组件对应的数据源参数,暂时只针对一个数据源
                    Param param = paramService.getByComponentId(component.getId());
                    component.setParam(param);

                    //init组件里面的元素
                    List<Item> items = itemService.getItems(component.getId());
                    for (Item item : items) {
                        SourceFieldMap sourceFieldMap = dataSourceService.getFieldByFieldId(item.getSourceFieldId());
                        if (sourceFieldMap != null) {
                            item.setSourceFieldId(sourceFieldMap.getId());
                            item.setSourceFiledName(sourceFieldMap.getName());
                            item.setSourceFileKey(sourceFieldMap.getSourceField());
                            item.setDatasourceId(dataSourceMap.get(sourceFieldMap.getSourceConfigId()).getId());
                            item.setDatasourceName(dataSourceMap.get(sourceFieldMap.getSourceConfigId()).getName());
                            item.setDatasourceKey(dataSourceMap.get(sourceFieldMap.getSourceConfigId()).getTargetTable());
                        }
                    }

                    component.setItems(items.stream().filter(item -> {
                        return item.getType() == 0 ? true : false;
                    }).collect(Collectors.toList()));
                    component.setTopItems(items.stream().filter(item -> {
                        return item.getType() == 1 ? true : false;
                    }).collect(Collectors.toList()));
                    component.setCountItems(items.stream().filter(item -> {
                        return item.getType() == 2 ? true : false;
                    }).collect(Collectors.toList()));
                    secondMenu.setComponent(component);
                }

            }

            firstMenu.setSecondMenus(secondMenus);
            result.add(firstMenu);
        }

        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * 保存/更新页面配置
     *
     * @param firstMenus
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper modifyContent(@RequestBody List<FirstMenu> firstMenus) {
        if (firstMenus == null) {
            return ConfigException.MISS_FIRST_MENU.get();
        } else if (firstMenus.size() > 9) {
            return ConfigException.OVER_LIMIT_FIRST_MENU.get();
        }

        Set<String> firstMenuNames = Sets.newHashSet();
        for (FirstMenu firstMenu : firstMenus) {

            if (firstMenu.getIsFix() == null) {
                return ConfigException.MISS_FIRST_MENU_FIX.get();
            } else if (firstMenu.getIsFix().equals(1)) {//固定导航
                continue;
            }

            if (firstMenu.getOrder() == null || firstMenu.getOrder().equals(0)) {
                return ConfigException.MISS_FIRST_MENU_LOCATION.get();
            }
            if (firstMenu.getName() == null) {
                return ConfigException.MISS_FIRST_MENU_NAME.get();
            } else if (firstMenu.getName().length() > 8) {
                return ConfigException.FIRST_MENU_LIMIT.get();
            } else if (firstMenuNames.contains(firstMenu.getName())) {
                return ConfigException.FIRST_MENU_NAME_REPEAT.get();
            } else {
                firstMenuNames.add(firstMenu.getName());
            }

            //二级导航判断
            Set<String> secondMenuNames = Sets.newHashSet();
            List<SecondMenu> secondMenus = firstMenu.getSecondMenus();
            if (secondMenus != null) {
                for (SecondMenu secondMenu : secondMenus) {
                    if (secondMenu.getOrder() == null || secondMenu.getOrder().equals(0)) {
                        return ConfigException.MISS_SECOND_MENU_ORDER.get();
                    }
                    if (secondMenu.getName() == null) {
                        return ConfigException.MISS_SECOND_MENU_NAME.get();
                    }
                    if (secondMenu.getName().length() > 24) {
                        return ConfigException.SECOND_MENU_LIMIT.get();
                    }
                    if (secondMenuNames.contains(secondMenu.getName())) {
                        return ConfigException.SECOND_MENU_NAME_REPEAT.get();
                    }
                    secondMenuNames.add(secondMenu.getName());

                    //组件判断
                    Component component = secondMenu.getComponent();
                    if (component != null) {
                        if (Strings.isNullOrEmpty(component.getDatasourceIds())) {
                            return ConfigException.MISS_MENU_COMPONENT_DATASOURCE.get();
                        }
                        Set dataSourceIds = Sets.newHashSet();
                        String[] sourceIdStrs = component.getDatasourceIds().split(",");
                        for (int i = 0; i < sourceIdStrs.length; i++) {
                            dataSourceIds.add(Long.parseLong(sourceIdStrs[i]));
                        }

                        //数据源参数判断
                        Param param = component.getParam();
                        if (param != null) {
                            if (param.getDatasourceId() == null || param.getDatasourceId().equals(0l)) {
                                return ConfigException.MISS_PARAM_SOURCE_ID.get();
                            }
                            if (param.getIsOrder().equals(1) && param.getOrderKey() == null) {//开启排序
                                return ConfigException.MISS_PARAM_ORDER_KEY.get();
                            }

                        }

                        //元素判断
                        Set<String> itemNames = Sets.newHashSet();
                        Set<String> topItemNames = Sets.newHashSet();
                        Set<String> countItemNames = Sets.newHashSet();
                        List<Item> itemsAll = component.getItems();
                        itemsAll = itemsAll.stream().peek(item -> item.setType(0)).collect(Collectors.toList());
                        if (component.getTopItems() != null) {
                            for (Item item : component.getTopItems()) {
                                item.setType(1);
                                itemsAll.add(item);
                            }
                        }
                        if (component.getCountItems() != null) {
                            for (Item item : component.getCountItems()) {
                                item.setType(2);
                                itemsAll.add(item);
                            }
                        }

                        if (itemsAll != null) {
                            for (Item item : itemsAll) {
                                if (item.getColSpace() == null || item.getRowSpace() == null) {
                                    return ConfigException.MISS_ITEM_LOCATION.get();
                                }
                                if (item.getSourceFieldId() == null) {
                                    return ConfigException.MISS_ITEM_FIELD.get();
                                }
                                if (item.getName() == null) {
                                    return ConfigException.MISS_ITEM_NAME.get();
                                }
                                if (item.getName().length() > 24) {
                                    return ConfigException.ITEM_NAME_LIMIT.get();
                                }

                                if (item.getColSpace() == null || item.getRowSpace() == null) {
                                    return ConfigException.MISS_ITEM_SPACEINFO.get();
                                }

                                if (item.getSourceFieldId() == null) {
                                    return ConfigException.MISS_ITEM_SOURCE_FIELD_ID.get();
                                } else if (!item.getSourceFieldId().equals(0l)) {
                                    SourceFieldMap sourceFieldMap = dataSourceService.getFieldByFieldId(item.getSourceFieldId());
                                    if (sourceFieldMap == null) {
                                        return Wrapper.builder()
                                                .msg(ConfigException.NO_SUCH_SOURCEFIELD.get().getMsg() + item.getSourceFieldId())
                                                .status(ConfigException.NO_SUCH_SOURCEFIELD.get().getStatus())
                                                .build();
                                    } else if (!dataSourceIds.contains(sourceFieldMap.getSourceConfigId())) {
                                        return ConfigException.WRONG_FIELD_IN_SOURCE.get();
                                    }
                                }

                                if (item.getEleType() == null) {
                                    return ConfigException.MISS_ITEM_TYPE.get();
                                } else if (item.getEleType().equals(1)) {//计算器类型
                                    if (Strings.isNullOrEmpty(item.getCountForm())) {
                                        return ConfigException.MISS_ITEM_COUNT_FORM.get();
                                    }
                                }

                                if (item.getType().equals(0)) {
                                    if (itemNames.contains(item.getName())) {
                                        return ConfigException.ITEM_NAME_REPEAT.get();
                                    }
                                    itemNames.add(item.getName());
                                } else if (item.getType().equals(1)) {
                                    if (topItemNames.contains(item.getName())) {
                                        return ConfigException.ITEM_NAME_REPEAT.get();
                                    }
                                    topItemNames.add(item.getName());
                                } else if (item.getType().equals(2)) {
                                    if (countItemNames.contains(item.getName())) {
                                        return ConfigException.ITEM_NAME_REPEAT.get();
                                    }
                                    countItemNames.add(item.getName());
                                }

                            }
                        }
                    }
                }

            }
        }

        //处理数据
        firstMenuService.createWithDetail(firstMenus);

        return Wrapper.OK;
    }

    /**
     * 获取一级导航显示的数据总量
     *
     * @param companyName
     * @return
     */
    @RequestMapping(value = "/fmtotal_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getFirstMenuTotalList(@RequestParam("company_name") String companyName) {
        if (Strings.isNullOrEmpty(companyName)) {
            return ConfigException.NO_COMPANY_NAME.get();
        }

        List<FirstMenu> firstMenus = firstMenuService.getAll();

        Map<Long, Integer> result = Maps.newHashMap();

        for (FirstMenu firstMenu : firstMenus) {
            int count = 0;

            List<SecondMenu> secondMenus = secondMenuService.getSecondMenus(firstMenu.getId());

            for (SecondMenu secondMenu : secondMenus) {
                Component component = componentService.getComponent(secondMenu.getId());
                if (component != null && !component.getId().equals(0l)) {
                    if (component.getType() == 1) {
                        continue;
                    } else {

                        //查询是否配置了内容
                        List<Item> items = itemService.getItems(component.getId());
                        if (items == null) {
                            continue;
                        }

                        Set<Long> posNoCountSourceIds = Sets.newHashSet();
                        for (Item item : items) {
                            if (!item.getType().equals(2)) {//位置类型：非计算器
                                SourceFieldMap sourceFieldMap = dataSourceService.getFieldByFieldId(item.getSourceFieldId());
                                posNoCountSourceIds.add(sourceFieldMap.getSourceConfigId());
                            }
                        }

                        List<Long> dataSourceIdList = posNoCountSourceIds.stream().collect(Collectors.toList());
                        if (dataSourceIdList.size() == 0) {
                            continue;
                        }

                        List<SourceFieldMap> sourceFieldMapList = dataSourceService.getFieldsBySourceId(dataSourceIdList.get(0));
                        String keyFieldName = null;
                        for (SourceFieldMap map : sourceFieldMapList) {
                            if (map.getIsKey() == 1) {
                                keyFieldName = map.getSourceField();
                                break;
                            }
                        }

                        DataSourceConfig dataSourceConfig = dataSourceService.findConfigById(dataSourceIdList.get(0));
                        count += collectionService.countAllByNameAndCondition(dataSourceConfig.getTargetTable(), keyFieldName, companyName);

                    }
                }
            }

            result.put(firstMenu.getId(), count);
        }

        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * 获取所有的计算方式
     *
     * @return
     */
    @RequestMapping(value = "/countforms", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getCountForms() {
        List<String> data = Lists.newArrayList();
        for (CountForm countForm : CountForm.values()) {
            data.add(countForm.getName());
        }
        return Wrapper.OKBuilder.data(data).build();
    }
}
