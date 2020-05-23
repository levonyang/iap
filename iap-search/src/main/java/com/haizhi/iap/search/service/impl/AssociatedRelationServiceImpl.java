package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.controller.model.AcquirerEvents;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.tab.first.AssociatedRelation;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.repo.DetailGraphRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.GraphRepo;
import com.haizhi.iap.search.repo.RedisRepo;
import com.haizhi.iap.search.service.AssociatedRelationService;
import com.haizhi.iap.search.utils.PageUtil;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/11/9.
 */
@Service
public class AssociatedRelationServiceImpl implements AssociatedRelationService {

    @Setter
    @Autowired
    DetailGraphRepo detailGraphRepo;

    @Setter
    @Autowired
    GraphRepo graphRepo;

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    /**
     * 对外投资
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getInvest(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        List<Map> investList = graphRepo.getOuterInvest("Company/" + SecretUtil.md5(companyName));
        DataItem invest = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List investData = PageUtil.pageList(investList, offset, count);
            invest.setData(investData);
        }
        invest.setTotalCount((long) investList.size());
        return invest;
    }

    /**
     * 投资事件
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getInvestEvents(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem investEvents = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> investEventsData = enterpriseRepo.getInvestEvents(companyName, offset, count);
            investEvents.setData(investEventsData);
        }
        investEvents.setTotalCount(enterpriseRepo.countInvestEvents(companyName));
        return investEvents;
    }

    @Override
    public DataItem getFinancialEvents(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem financialEvents = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> financialEventsData = enterpriseRepo.getFinancialEvents(companyName, offset, count);
            financialEvents.setData(financialEventsData);
        }
        financialEvents.setTotalCount(enterpriseRepo.countFinancialEvents(companyName));
        return financialEvents;
    }

    @Override
    public AcquirerEvents getAcquirerEvents(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        AcquirerEvents acquirerEvents = new AcquirerEvents();
        Long acquirerCount = enterpriseRepo.countAcquirerEvents(companyName);
        Long acquireredCount = enterpriseRepo.countAcquireredEvents(companyName);
        if (onlyCount != null && !onlyCount) {
            //获取并购与被并购事件
            List<Map> acquirerEventsList = enterpriseRepo.getAcquirerEvents(companyName, offset, count);
            List<Map> acquireredEventsList = enterpriseRepo.getAcquireredEvents(companyName, offset, count);
            acquirerEvents.setAcquirer(new DataItem(acquirerEventsList, acquirerCount));
            acquirerEvents.setAcquirered(new DataItem(acquireredEventsList, acquireredCount));
        }
        acquirerEvents.setTotalCount(acquirerCount + acquireredCount);
        return acquirerEvents;
    }

    @Override
    public DataItem getExitEvents(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem exitEvents = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> exitEventsData = enterpriseRepo.getInvestEvents(companyName, offset, count);
            exitEvents.setData(exitEventsData);
        }
        exitEvents.setTotalCount(enterpriseRepo.countInvestEvents(companyName));
        return exitEvents;
    }

    /**
     * 一致行动人
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getConcert(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem concert = new DataItem();
        List concertList = redisRepo.getGraphConcert(companyName);
        if (concertList == null || concertList.size() < 1) {
            concertList = detailGraphRepo.getGraphConcert(companyName); //查询一致行动人
        }
        if (onlyCount != null && !onlyCount) {
            List concertData = PageUtil.pageList(concertList, offset, count);
            concert.setData(concertData);
        }
        redisRepo.pushGraphConcert(concertList, companyName);
        concert.setTotalCount((long) concertList.size());
        return concert;
    }

    /**
     * 股东对外投资及任职
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getContributorInvestOffice(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem contributor = new DataItem();
        List contributorList = redisRepo.getGraphContributor(companyName);
        if (contributorList == null || contributorList.size() < 1) {
            contributorList = detailGraphRepo.getGraphContributor(companyName);
        }
        if (onlyCount != null && !onlyCount) {
            List concertData = PageUtil.pageList(contributorList, offset, count);
            contributor.setData(concertData);
        }
        redisRepo.pushGraphContributor(contributorList, companyName);
        contributor.setTotalCount((long) contributorList.size());
        return contributor;
    }

    /**
     * 高管对外投资及任职
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getKeyPersonInvestOffice(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem keyPerson = new DataItem();
        List keyPersonList = redisRepo.getGraphKeyPerson(companyName);
        if (keyPersonList == null || keyPersonList.size() < 1) {
            keyPersonList = detailGraphRepo.getGraphKeyPerson(companyName);
        }
        if (onlyCount != null && !onlyCount) {
            List keyPersonData = PageUtil.pageList(keyPersonList, offset, count);
            keyPerson.setData(keyPersonData);
        }
        redisRepo.pushGraphKeyPerson(keyPersonList, companyName);
        keyPerson.setTotalCount((long) keyPersonList.size());
        return keyPerson;
    }

    public AssociatedRelation getPageBySubType(String companyName, Boolean onlyCount, String subTypeName, Integer offset, Integer count) {
        AssociatedRelation.RelationSubType subType = AssociatedRelation.RelationSubType.get(subTypeName);
        if (subType != null) {
            AssociatedRelation relation = new AssociatedRelation();

            switch (subType) {
                case INVEST:
                    relation.setInvest(getInvest(companyName, onlyCount, offset, count));
                    break;
                case INVEST_EVENTS:
                    relation.setInvestEvents(getInvestEvents(companyName, onlyCount, offset, count));
                    break;
                case FINANCIAL_EVENTS:
                    relation.setFinancialEvents(getFinancialEvents(companyName, onlyCount, offset, count));
                    break;
                case ACQUIRER_EVENTS:
                    relation.setAcquirerEvents(getAcquirerEvents(companyName, onlyCount, offset, count));
                    break;
                case EXIT_EVENTS:
                    relation.setExitEvents(getExitEvents(companyName, onlyCount, offset, count));
                    break;
                case CONCERT:
                    relation.setConcert(getConcert(companyName, onlyCount, offset, count));
                    break;
                case CONTRIBUTOR_INVEST_OFFICE:
                    relation.setContributorInvestOffice(getContributorInvestOffice(companyName, onlyCount, offset, count));
                    break;
                case KEY_PERSON_INVEST_OFFICE:
                    relation.setKeyPersonInvestOffice(getKeyPersonInvestOffice(companyName, onlyCount, offset, count));
                    break;
            }
            return relation;
        } else {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        }
    }
}
