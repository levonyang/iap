package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.Item;

import java.util.List;

/**
 * Created by chenbo on 17/4/18.
 */
public interface ItemService {

    /**
     * 根据component_id获取所有元素
     * @param componentId
     * @return
     */
    List<Item> getItems(Long componentId);

    /**
     * 获取所有的元素
     * @return
     */
    List<Item> getAll();

    /**
     * 在组件中新增元素
     * @param item
     * @return
     */
    Item create(Item item);

    /**
     * 批量删除元素
     * @param itemIds
     */
    void batchDelete(List<Long> itemIds);

    /**
     * 批量修改元素
     * @param items
     */
    void batchUpdate(List<Item> items);
}
