package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.Component;

import java.util.List;

/**
 * @Author chenbo
 * @Date 2017/4/11 下午2:05.
 */
public interface ComponentService {

    /**
     * 根据二级导航id获取menucomponents
     * @param secondMenuId
     * @return
     */
    Component getComponent(Long secondMenuId);

    /**
     * 根据id获取component
     * @param id
     * @return
     */
    Component getComponentById(Long id);

    /**
     * 获取所有的menucomponent
     * @return
     */
    List<Component> getAll();

    /**
     * 在二级导航中新增组件
     * @param menuComponent
     * @return
     */
    Component create(Component menuComponent);

    /**
     * 批量删除menucomponent
     * @param mcIds
     */
    void batchDelete(List<Long> mcIds);

    /**
     * 批量修改menucomponent
     * @param components
     */
    void batchUpdate(List<Component> components);
}
