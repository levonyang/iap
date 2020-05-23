package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model2.tab.first.PublicSentiment;
import com.haizhi.iap.search.controller.model2.tab.second.BaiduNews;

/**
 * Created by chenbo on 2017/11/9.
 */
public interface PublicSentimentService {

    PublicSentiment getPageBySubType(String companyName, Boolean onlyCount, String subType, Integer offset, Integer count);

    PublicSentiment getPageByThirdType(String companyName, Boolean onlyCount, String subType, String thirdType, Integer offset, Integer count);

    BaiduNews getBaiduNews(String companyName, Boolean onlyCount, Integer offset, Integer count);

}
