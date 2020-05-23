package com.haizhi.iap.configure.service.impl;

import com.google.common.collect.Lists;
import com.haizhi.iap.configure.model.*;
import com.haizhi.iap.configure.repo.*;
import com.haizhi.iap.configure.service.FirstMenuService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author dmy
 * @Date 2017/4/6 下午5:49.
 */
@Service
public class FirstMenuServiceImpl implements FirstMenuService {

    @Setter
    @Autowired
    FirstMenuRepo firstMenuRepo;

    @Setter
    @Autowired
    SecondMenuRepo secondMenuRepo;

    @Setter
    @Autowired
    ComponentRepo componentRepo;

    @Setter
    @Autowired
    ItemRepo itemRepo;

    @Setter
    @Autowired
    ParamRepo paramRepo;

    @Override
    public List<FirstMenu> getAll() {
        return firstMenuRepo.getAll();
    }

    @Override
    public FirstMenu findByName(String name) {
        return firstMenuRepo.findByName(name);
    }

    @Override
    public FirstMenu create(FirstMenu firstMenu) {
        return firstMenuRepo.create(firstMenu);
    }

    @Override
    public void createWithDetail(List<FirstMenu> firstMenus) {
        //first_menu record
        List<Long> firstMenuIds = Lists.newArrayList();
        List<FirstMenu> updateFirstMenus = Lists.newArrayList();
        List<FirstMenu> existFirstMenus = firstMenuRepo.getAll();

        for (FirstMenu firstMenu : firstMenus) {
            //second_menu record
            List<Long> secondMenuIds = Lists.newArrayList();
            List<SecondMenu> updateSecondMenus = Lists.newArrayList();
            List<SecondMenu> existSeconds = Lists.newArrayList();

            //menu_component record
            List<Long> componentIds = Lists.newArrayList();
            List<Component> updateComponents = Lists.newArrayList();
            List<Component> existComponents = Lists.newArrayList();

            //param record
            List<Long> paramIds = Lists.newArrayList();
            List<Param> updateParams = Lists.newArrayList();
            List<Param> existParams = Lists.newArrayList();

            //item rcord
            List<Long> itemIds = Lists.newArrayList();
            List<Item> updateItems = Lists.newArrayList();
            List<Item> existItems = Lists.newArrayList();

            //新增处理
            if (firstMenu.getId() == null || firstMenu.getId().equals(0l)) {
                //新增一级导航  ???bug --dmy
                FirstMenu newFirstMenu = create(firstMenu);
                if (newFirstMenu.getId() == null || newFirstMenu.getId().equals(0l)) {
                    newFirstMenu = findByName(firstMenu.getName());
                }
                firstMenu.setId(newFirstMenu.getId());
            } else {
                updateFirstMenus.add(firstMenu);
                firstMenuIds.add(firstMenu.getId());
                existSeconds = secondMenuRepo.getSecondMenus(firstMenu.getId());
                existComponents = componentRepo.findByFirstMenu(firstMenu.getId());
                existItems = itemRepo.findByFirstMenu(firstMenu.getId());
                existParams = paramRepo.findByFirstMenu(firstMenu.getId());
            }

            List<SecondMenu> secondMenus = firstMenu.getSecondMenus();
            for (SecondMenu secondMenu : secondMenus) {
                if (secondMenu.getId() == null || secondMenu.getId().equals(0l)) {
                    //新增二级导航
                    secondMenu.setFirstMenuId(firstMenu.getId());

                    SecondMenu newSecond = secondMenuRepo.create(secondMenu);
                    if (newSecond.getId() == null || newSecond.getId().equals(0l)) {
                        newSecond = secondMenuRepo.findByNameAndFirst(secondMenu.getName(), secondMenu.getFirstMenuId());
                    }
                    secondMenu.setId(newSecond.getId());
                } else {
                    //待更新
                    updateSecondMenus.add(secondMenu);
                    //待比较,看是否有删除
                    secondMenuIds.add(secondMenu.getId());
                }

                Component component = secondMenu.getComponent();
                if (component != null) {
                    if (component.getId() == null || component.getId().equals(0l)) {
                        //新增组件内容
                        component.setFirstMenuId(firstMenu.getId());
                        component.setSecondMenuId(secondMenu.getId());

                        Component newComponent = componentRepo.create(component);
                        if (newComponent.getId() == null || newComponent.getId().equals(0l)) {
                            newComponent = componentRepo.getComponent(component.getSecondMenuId());
                        }
                        component.setId(newComponent.getId());
                    } else {
                        updateComponents.add(component);
                        componentIds.add(component.getId());
                    }

                    //数据源参数，暂时只针对一个数据源
                    Param param = component.getParam();
                    if (param != null) {
                        if (param.getId() == null || param.getId().equals(0l)) {
                            //新增数据源参数
                            param.setFirstMenuId(firstMenu.getId());
                            param.setComponentId(component.getId());

                            Param newParam = paramRepo.create(param);
                            if (newParam.getId() == null || newParam.getId().equals(0l)) {
                                newParam = paramRepo.getParamByDataAndComponent(param.getDatasourceId(), param.getComponentId());
                            }
                            param.setId(newParam.getId());
                        } else {
                            updateParams.add(param);
                            paramIds.add(param.getId());
                        }
                    }


                    List<Item> itemViewsAll = component.getItems();
                    itemViewsAll = itemViewsAll.stream().peek(item -> item.setType(0)).collect(Collectors.toList());
                    if (component.getTopItems() != null) {
                        for (Item item : component.getTopItems()) {
                            item.setType(1);
                            itemViewsAll.add(item);
                        }
                    }
                    if (component.getCountItems() != null) {
                        for (Item item : component.getCountItems()) {
                            item.setType(2);
                            itemViewsAll.add(item);
                        }
                    }
                    for (Item item : itemViewsAll) {
                        if (item.getId() == null || item.getId().equals(0l)) {
                            //新增元素
                            item.setComponentId(component.getId());
                            item.setFirstMenuId(firstMenu.getId());
                            item.setSourceFieldId(item.getSourceFieldId());

                            //有可能为空，当组件对应一个数据源时
                            Item newItem = itemRepo.create(item);
                            if (newItem.getId() == null || newItem.getId().equals(0l)) {
                                newItem = itemRepo.findByNameAndComponent(item.getName(), item.getComponentId());
                            }
                            item.setId(newItem.getId());
                        } else {
                            updateItems.add(item);
                            itemIds.add(item.getId());
                        }
                    }
                }
            }

            List<Long> deleteSecondIds = Lists.newArrayList();
            List<Long> deleteComponentIds = Lists.newArrayList();
            List<Long> deleteItemIds = Lists.newArrayList();
            List<Long> deleteParamIds = Lists.newArrayList();

            deleteSecondIds.addAll(existSeconds.stream()
                    .filter(secondMenu -> !secondMenuIds.contains(secondMenu.getId()))
                    .map(SecondMenu::getId).collect(Collectors.toList()));

            deleteComponentIds.addAll(existComponents.stream()
                    .filter(component -> !componentIds.contains(component.getId()))
                    .map(Component::getId).collect(Collectors.toList()));

            deleteItemIds.addAll(existItems.stream()
                    .filter(item -> !itemIds.contains(item.getId()))
                    .map(Item::getId).collect(Collectors.toList()));

            deleteParamIds.addAll(existParams.stream()
                    .filter(param -> !paramIds.contains(param.getId()))
                    .map(Param::getId).collect(Collectors.toList()));

            secondMenuRepo.batchUpdate(updateSecondMenus);
            componentRepo.batchUpdate(updateComponents);
            paramRepo.batchUpdate(updateParams);
            itemRepo.batchUpdate(updateItems);

            secondMenuRepo.batchDelete(deleteSecondIds);
            componentRepo.batchDelete(deleteComponentIds);
            paramRepo.batchDelete(deleteParamIds);
            itemRepo.batchDelete(deleteItemIds);

        }

        //修改、删除处理
        firstMenuRepo.batchUpdate(updateFirstMenus);
        List<Long> deleteFirstIds = Lists.newArrayList();
        deleteFirstIds.addAll(existFirstMenus.stream()
                .filter(firstMenu -> !firstMenuIds.contains(firstMenu.getId()))
                .map(FirstMenu::getId).collect(Collectors.toList()));
        firstMenuRepo.batchDelete(deleteFirstIds);

    }

    @Override
    public void batchDelete(List<Long> fmIds) {
        firstMenuRepo.batchDelete(fmIds);
    }

    @Override
    public void batchUpdate(List<FirstMenu> firstMenuViews) {
        firstMenuRepo.batchUpdate(firstMenuViews);
    }

}
