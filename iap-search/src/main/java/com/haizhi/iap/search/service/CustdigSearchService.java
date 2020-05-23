package com.haizhi.iap.search.service;

import com.haizhi.iap.common.bean.CustdigParam;

import java.util.List;
import java.util.Set;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/11 11:05
 */
public interface CustdigSearchService {
    /**
     * 查询多个企业的关系
     * @return
     */
    Object searchTravel(CustdigParam param);

    /**
     *
     * @param param
     * @return
     */
    Object searchShortPath(CustdigParam param);

    /**
     * 通过企业名称查询企业id列表
     * @param companys
     * @return
     */
    Set<String> findCustByname(List<String> companys);
}
