package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.AcquirerEvents;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.tab.first.AssociatedRelation;

/**
 * Created by chenbo on 2017/11/9.
 */
public interface AssociatedRelationService {

    DataItem getInvest(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getInvestEvents(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getFinancialEvents(String companyName, Boolean onlyCount, Integer offset, Integer count);

    AcquirerEvents getAcquirerEvents(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getExitEvents(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getConcert(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getContributorInvestOffice(String companyName, Boolean onlyCount, Integer offset, Integer count);

    DataItem getKeyPersonInvestOffice(String companyName, Boolean onlyCount, Integer offset, Integer count);

    AssociatedRelation getPageBySubType(String companyName, Boolean onlyCount, String subTypeName, Integer offset, Integer count);

}
