package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.tab.first.ProfileInfo;
import com.haizhi.iap.search.controller.model2.tab.second.AssociatedInfo;
import com.haizhi.iap.search.model.DynamicInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/11/10.
 */
public interface ProfileInfoService {

    ProfileInfo getPageBySubType(Map<String, Object> companyBasic, SearchRequest request);

    ProfileInfo getPageByThirdType(SearchRequest request);

    DataItem getSelfInfo(Map<String, Object> companyBasic, Boolean onlyCounting);

    AssociatedInfo getAssociatedInfo(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getDynamicInfo(String company, String infoType, Boolean onlyCount, Integer offset, Integer count);

    List<DynamicInfo> getDynamicInfo(String companyName, String infoType);

}

