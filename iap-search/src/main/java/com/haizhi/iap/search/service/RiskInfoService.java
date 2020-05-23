package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.tab.first.RiskInfo;
import com.haizhi.iap.search.controller.model2.tab.second.AllPenalty;

import java.util.Map;

/**
 * Created by chenbo on 2017/11/9.
 */
public interface RiskInfoService {

    RiskInfo getPageByThirdType(String companyName, Boolean onlyCount, String thirdType, Integer offset, Integer count);

    RiskInfo getPageBySubType(Map<String, Object> basic, SearchRequest request);

    DataItem getCourtKtgg(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getCourtFygg(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getJudgement(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getJudgeProcess(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    AllPenalty getAllPenalty(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getOwingTax(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getShixinInfo(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getZhixingInfo(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getEnvInfo(String companyName, Boolean onlyCounting, Integer offset, Integer count);

    DataItem getChattelMortgageInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count);

    DataItem getEquityPledgedInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count);

    DataItem getAbnormalOperationInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count);
}
