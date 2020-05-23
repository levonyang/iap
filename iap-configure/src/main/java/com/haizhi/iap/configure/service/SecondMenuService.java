package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.SecondMenu;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/7 下午5:04.
 */
public interface SecondMenuService {

    /**
     * 根据一级导航id获取二级导航列表
     * @param parentId
     * @return
     */
    List<SecondMenu> getSecondMenus(Long parentId);

    /**
     * 获取所有的二级导航
     * @return
     */
    List<SecondMenu> getAll();

    /**
     * 创建二级导航
     * @param secondMenu
     * @return
     */
    SecondMenu create(SecondMenu secondMenu);

    /**
     * 批量删除二级导航
     * @param smIds
     */
    void batchDelete(List<Long> smIds);

    /**
     * 批量编辑二级导航
     * @param secondMenus
     */
    void batchUpdate(List<SecondMenu> secondMenus);

}
