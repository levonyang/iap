package com.haizhi.iap.configure.service.impl;

import com.haizhi.iap.configure.model.SecondMenu;
import com.haizhi.iap.configure.repo.SecondMenuRepo;
import com.haizhi.iap.configure.service.SecondMenuService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/7 下午5:04.
 */
@Service
public class SecondMenuServiceImpl implements SecondMenuService {

    @Setter
    @Autowired
    SecondMenuRepo secondMenuDao;

    @Override
    public List<SecondMenu> getSecondMenus(Long parentId) {
        return secondMenuDao.getSecondMenus(parentId);
    }

    @Override
    public List<SecondMenu> getAll() {
        return secondMenuDao.getAll();
    }

    @Override
    public SecondMenu create(SecondMenu secondMenu) {
        return secondMenuDao.create(secondMenu);
    }

    @Override
    public void batchDelete(List<Long> smIds) {
        secondMenuDao.batchDelete(smIds);
    }

    @Override
    public void batchUpdate(List<SecondMenu> secondMenuViews) {
        secondMenuDao.batchUpdate(secondMenuViews);
    }

}
