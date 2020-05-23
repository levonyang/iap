package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.tab.first.BasicInfo;
import com.haizhi.iap.search.controller.model2.tab.second.CompanyInfo;
import com.haizhi.iap.search.controller.model2.tab.second.CustomsInfo;
import com.haizhi.iap.search.controller.model2.tab.second.ListInfo;

import java.util.Map;

/**
 * Created by chenbo on 2017/11/8.
 */
public interface BasicInfoService {

    BasicInfo getPageBySubType(Map<String, Object> companyBasic, SearchRequest request);

    BasicInfo getPageByThirdType(Map<String, Object> companyBasic, SearchRequest request);

    CompanyInfo getCompanyInfo(Map<String, Object> basic, SearchRequest request);

    DataItem getAnnualReport(String companyName, Boolean onlyCount, Integer offset, Integer count);

    ListInfo getListInfo(SearchRequest request);

    CustomsInfo getCustomsInfo(String companyName, Boolean onlyCount, Integer offset, Integer count);

}
