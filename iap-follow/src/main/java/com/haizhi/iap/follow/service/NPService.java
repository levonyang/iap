package com.haizhi.iap.follow.service;

import com.haizhi.iap.follow.model.NPScore;

import java.util.Date;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/12/18 下午6:20.
 */
public interface NPService {
    List<NPScore> findByType(Integer offset, Integer count, String type, Date date);

    Integer getCountByType(String type, Date date);
}
