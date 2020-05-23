package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.tab.first.MarketInfo;

import java.util.Map;

/**
 * Created by chenbo on 2017/11/9.
 */
public interface MarketInfoService {

    MarketInfo getPageBySubType(String companyName,Boolean onlyCount, String subType,
                                Map<String, Object> basic, Integer offset, Integer count);

    DataItem getBidInfo(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getWinInfo(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getLandAuction(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getPatent(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getTaxPayerLevelA(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getHiringInfo(String companyName, Boolean onlyCount, Integer offset, Integer count);
}
