package com.haizhi.iap.configure.service.impl;

import com.haizhi.iap.configure.model.Component;
import com.haizhi.iap.configure.repo.ComponentRepo;
import com.haizhi.iap.configure.service.ComponentService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/11 下午2:05.
 */
@Service
public class ComponentServiceImpl implements ComponentService {

    @Setter
    @Autowired
    ComponentRepo componentRepo;

    @Override
    public Component getComponent(Long parentId) {
        return componentRepo.getComponent(parentId);
    }

    @Override
    public Component getComponentById(Long id) {
        return componentRepo.getComponentById(id);
    }

    @Override
    public List<Component> getAll() {
        return componentRepo.getAll();
    }

    @Override
    public Component create(Component menuComponent) {
        return componentRepo.create(menuComponent);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        componentRepo.batchDelete(ids);
    }

    @Override
    public void batchUpdate(List<Component> componentViews) {
        componentRepo.batchUpdate(componentViews);
    }
}
