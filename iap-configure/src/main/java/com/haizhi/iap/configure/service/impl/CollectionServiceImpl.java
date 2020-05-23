package com.haizhi.iap.configure.service.impl;

import com.google.common.base.Strings;
import com.haizhi.iap.configure.model.Param;
import com.haizhi.iap.configure.model.SourceFieldMap;
import com.haizhi.iap.configure.repo.CollectionRepo;
import com.haizhi.iap.configure.repo.DataSourceRepo;
import com.haizhi.iap.configure.service.CollectionService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2017/4/27 下午5:27.
 */
@Service
public class CollectionServiceImpl implements CollectionService {

    @Setter
    @Autowired
    CollectionRepo collectionRepo;

    @Setter
    @Autowired
    DataSourceRepo sourceRepo;

    @Override
    public List<Map> getCollectionByNameAndCondition(String collName, String keyFieldName, Integer offset, Integer count, String companyName, Param param) {
        if(param.getIsOrder() != null && Strings.isNullOrEmpty(param.getOrderFieldName())){
            SourceFieldMap map = sourceRepo.getFieldByFieldId(param.getOrderKey());
            if(map != null){
                param.setOrderFieldName(map.getSourceField());
            }
        }
        return collectionRepo.getCollectionByNameAndCondition(collName, keyFieldName, offset, count, companyName, param);
    }

    @Override
    public Long countAllByNameAndCondition(String collName, String keyFieldName, String companyName) {
        return collectionRepo.countAllByNameAndCondition(collName, keyFieldName, companyName);
    }
}
