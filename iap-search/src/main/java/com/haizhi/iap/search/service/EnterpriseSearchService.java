package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.*;
import com.haizhi.iap.search.model.DynamicInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/2/13.
 */
public interface EnterpriseSearchService {
    Object search(EnterpriseReq req);

    Basic basic(EnterpriseReq req);

    List<AnnualReport> annualReport(EnterpriseReq req);

    Listing listing(EnterpriseReq req);

    Investment invest(EnterpriseReq req);

    IntellectualProperty intellectualProperty(EnterpriseReq req);

    Bidding bidding(EnterpriseReq req);

    Risk risk(EnterpriseReq req);

    Sentiment publicSentiment(EnterpriseReq req);

    InvestInstitution investInstitution(EnterpriseReq req);

    List<Map<String, Object>> getGraphConcert(String companyName);

    List<Map<String, Object>> getGraphKeyPerson(String companyName);

    List<Map> getGraphContributor(String companyName);

    List<DynamicInfo> getDynamicInfo(String companyName, String type);

    Pair<String, String> getFaction(String company);

    List<Map> getTaxRank(String company);

    Boolean isStored(String company);

    Map<String, Double> getExchangeRateMap();

    List<Map<String, Object>> briefBatch(List<String> companyName);

}
