package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2017/4/27 下午5:25.
 */
public interface CollectionService {
    /**
     * 根据表名和条件获取数据
     *
     * @param collName
     * @param offset
     * @param count
     * @param companyName
     * @param param
     * @return
     */
    List<Map> getCollectionByNameAndCondition(String collName, String keyFieldName, Integer offset, Integer count, String companyName, Param param);

    /**
     * 按条件统计集合里面的数据总数
     *
     * @param collName
     * @param companyName
     * @return
     */
    Long countAllByNameAndCondition(String collName, String keyFieldName, String companyName);
}
