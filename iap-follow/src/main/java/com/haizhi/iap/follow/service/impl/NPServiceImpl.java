package com.haizhi.iap.follow.service.impl;

import com.haizhi.iap.follow.model.NPScore;
import com.haizhi.iap.follow.repo.NPRepo;
import com.haizhi.iap.follow.service.NPService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/12/18 下午6:25.
 */
@Slf4j
@Service
public class NPServiceImpl implements NPService{

    @Setter
    @Autowired
    NPRepo npRepo;

    @Override
    public List<NPScore> findByType(Integer offset, Integer count, String type, Date date) {
        return npRepo.findByType(offset, count, type, date);
    }

    @Override
    public Integer getCountByType(String type, Date date) {
        return npRepo.getCountByType(type, date);
    }

}
