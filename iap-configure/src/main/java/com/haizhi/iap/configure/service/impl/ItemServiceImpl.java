package com.haizhi.iap.configure.service.impl;

import com.haizhi.iap.configure.model.Item;
import com.haizhi.iap.configure.repo.ItemRepo;
import com.haizhi.iap.configure.service.ItemService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/11 下午2:37.
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Setter
    @Autowired
    ItemRepo itemRepo;

    @Override
    public List<Item> getItems(Long parentId) {
        return itemRepo.getItems(parentId);
    }

    @Override
    public List<Item> getAll() {
        return itemRepo.getAll();
    }

    @Override
    public Item create(Item item) {
        return itemRepo.create(item);
    }

    @Override
    public void batchDelete(List<Long> itemIds) {
        itemRepo.batchDelete(itemIds);
    }

    @Override
    public void batchUpdate(List<Item> items) {
        itemRepo.batchUpdate(items);
    }
}
