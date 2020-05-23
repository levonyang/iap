package com.haizhi.iap.configure.service.impl;

import com.haizhi.iap.configure.model.Param;
import com.haizhi.iap.configure.repo.ParamRepo;
import com.haizhi.iap.configure.service.ParamService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author dmy
 * @Date 2017/5/8 下午7:01.
 */
@Service
public class ParamServiceImpl implements ParamService {

    @Setter
    @Autowired
    ParamRepo paramRepo;

    @Override
    public Param getByComponentId(Long componentId) {
        return paramRepo.getByComponentId(componentId);
    }

    @Override
    public Param getParamByDataAndComponent(Long datasourceid, Long componentId) {
        return paramRepo.getParamByDataAndComponent(datasourceid, componentId);
    }
}
