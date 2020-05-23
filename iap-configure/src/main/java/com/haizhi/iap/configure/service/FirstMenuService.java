package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.FirstMenu;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/6 下午5:49.
 */
public interface FirstMenuService {

    /**
     * 获取所有的一级导航
     * @return
     */
    List<FirstMenu> getAll();

    /**
     * 按名字查
     */
    FirstMenu findByName(String name);

    /**
     * 新增一级导航
     * @param firstMenu
     * @return
     */
    FirstMenu create(FirstMenu firstMenu);

    /**
     * 新增一级导航,一级附带二级导航、组件信息
     */
    void createWithDetail(List<FirstMenu> firstMenu);

    /**
     * 批量删除一级导航
     * @param fmIds
     */
    void batchDelete(List<Long> fmIds);

    /**
     * 批量修改一级导航
     * @param firstMenus
     */
    void batchUpdate(List<FirstMenu> firstMenus);
}
