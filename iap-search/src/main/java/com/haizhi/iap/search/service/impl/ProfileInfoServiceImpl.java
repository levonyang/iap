package com.haizhi.iap.search.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lombok.Setter;

import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.tab.first.ProfileInfo;
import com.haizhi.iap.search.controller.model2.tab.second.AssociatedInfo;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.DynamicInfo;
import com.haizhi.iap.search.model.Notification;
import com.haizhi.iap.search.repo.DetailGraphRepo;
import com.haizhi.iap.search.repo.DynamicInfoRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.NotificationRepo;
import com.haizhi.iap.search.repo.RedisRepo;
import com.haizhi.iap.search.service.ProfileInfoService;
import com.haizhi.iap.search.utils.PageUtil;

/**
 * Created by chenbo on 2017/11/10.
 */
@Service
public class ProfileInfoServiceImpl implements ProfileInfoService {
    @Setter
    @Autowired
    DetailGraphRepo detailGraphRepo;

    @Setter
    @Autowired
    DynamicInfoRepo dynamicInfoRepo;

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;
    
    @Autowired
    NotificationRepo notificationRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    private static DateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public ProfileInfo getPageBySubType(Map<String, Object> companyBasic, SearchRequest request) {
        ProfileInfo.ProfileInfoSubType subType = ProfileInfo.ProfileInfoSubType.get(request.getSubType());
        if (subType == null) {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        } else {
            ProfileInfo profileInfo = new ProfileInfo();
            switch (subType) {
                case SELF_INFO:
                    profileInfo.setSelfInfo(getSelfInfo(companyBasic, request.getOnlyCount()));
                    break;
                case ASSOCIATED_INFO:
                    profileInfo.setAssociatedInfo(getAssociatedInfo(request.getName(), request.getOnlyCount(),
                            request.getOffset(), request.getCount()));
                    break;
                case DYNAMIC_INFO:
                    profileInfo.setDynamicInfo(getDynamicInfo(request.getName(), null,
                            request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
            }
            return profileInfo;
        }
    }

    @Override
    public ProfileInfo getPageByThirdType(SearchRequest request) {
        ProfileInfo.ProfileInfoSubType profileInfoSubType = ProfileInfo.ProfileInfoSubType.get(request.getSubType());
        if (profileInfoSubType == null) {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        } else {
            ProfileInfo profileInfo = new ProfileInfo();
            switch (profileInfoSubType) {
                case SELF_INFO: //自身特性
                    break;
                case ASSOCIATED_INFO:
                    AssociatedInfo.AssociatedInfoSubType assoInfoSubType =
                            AssociatedInfo.AssociatedInfoSubType.get(request.getThirdType());
                    if (assoInfoSubType == null) {
                        throw new ServiceAccessException(SearchException.UNSUPPORTED_THIRD_TYPE);
                    } else {
                        AssociatedInfo associatedInfo = new AssociatedInfo();
                        switch (assoInfoSubType) {
                            case CONCERT:
                                associatedInfo.setConcert(getConcert(request.getName(), request.getOnlyCount(),
                                        request.getOffset(), request.getCount()));
                                break;
                            case CONTRIBUTOR_INVEST_OFFICE:
                                associatedInfo.setContributorInvestOffice(getContributorInvestOffice(request.getName(),
                                        request.getOnlyCount(), request.getOffset(), request.getCount()));
                                break;
                            case KEY_PERSON_INVEST_OFFICE:
                                associatedInfo.setKeyPersonInvestOffice(getKeyPersonInvestOffice(request.getName(),
                                        request.getOnlyCount(), request.getOffset(), request.getCount()));
                                break;
                        }
                        profileInfo.setAssociatedInfo(associatedInfo);
                    }
                    break;
                case DYNAMIC_INFO:
                    profileInfo.setDynamicInfo(getDynamicInfo(request.getName(), request.getThirdType(),
                            request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
            }
            return profileInfo;
        }
    }

    /**
     * 自身特征
     * @param companyBasic
     * @param onlyCount
     * @return
     */
    @Override
    public DataItem getSelfInfo(Map<String, Object> companyBasic, Boolean onlyCount) {
        DataItem selfInfo = new DataItem();
        if (onlyCount != null && !onlyCount) {
            selfInfo.setData(companyBasic);
        }
        selfInfo.setTotalCount(companyBasic == null ? 0 : 1L);
        return selfInfo;
    }

    /**
     * 获取
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public AssociatedInfo getAssociatedInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        AssociatedInfo associatedInfo = new AssociatedInfo();
        associatedInfo.setConcert(getConcert(companyName, onlyCount, offset, count));
        associatedInfo.setContributorInvestOffice(getContributorInvestOffice(companyName, onlyCount, offset, count));
        associatedInfo.setKeyPersonInvestOffice(getKeyPersonInvestOffice(companyName, onlyCount, offset, count));
        return associatedInfo;
    }

    /**
     * 动态特征
     * @param companyName
     * @param infoType (营销信息:marketing,风险信息:risk)
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getDynamicInfo(String companyName, String infoType, Boolean onlyCount, Integer offset, Integer count) {
        DataItem dynamicInfo = new DataItem();
        if (Strings.isNullOrEmpty(infoType)) {
            infoType = "_all";
        }
        List<DynamicInfo> dynamicInfoList = getDynamicInfoV2(companyName, infoType); //查询数据
        //在内存里进行分页
        if (onlyCount != null && !onlyCount) {
            dynamicInfo.setData(PageUtil.pageList(dynamicInfoList, offset, count));
        }
        dynamicInfo.setTotalCount((long) dynamicInfoList.size());
        return dynamicInfo;
    }

    public List<DynamicInfo> getDynamicInfoV2(String companyName, String infoType) {
       
        List<DynamicInfo> result = Lists.newArrayList();
        //infoType:[营销信息:marketing,风险信息:risk]
        List<Notification> notifications = notificationRepo.findByCondition(null, null, companyName, infoType);
        notifications.forEach(item->{
        	DynamicInfo dynamicInfo = new DynamicInfo();
        	dynamicInfo.setDocId(item.getId().toString());
        	dynamicInfo.setTitle(item.getTitle());
        	dynamicInfo.setType(item.getRuleName());
        	if(item.getType()<200){
        		dynamicInfo.setTypeEn("marketing");
        	}else{
        		dynamicInfo.setTypeEn("risk");
        	}
        	dynamicInfo.setSubTypeEn(item.getType().toString());
        	
        	dynamicInfo.setDate(item.getPushTime());
        	dynamicInfo.setDetail(JSON.parseObject(item.getDetail(),  Map.class));
        	result.add(dynamicInfo);
        });
        return result;
    }
    
    @Override
    public List<DynamicInfo> getDynamicInfo(String companyName, String infoType) {
        //每个类型抽一个月的数据
        List<DynamicInfo> result = Lists.newArrayList();
        Map<String, String> stockTypeMap = enterpriseRepo.getSectorMap(companyName);
        List<String> stockCodes = Lists.newArrayList(stockTypeMap.values());

        List<DynamicInfo> changeRecords = Collections.EMPTY_LIST;
        List<DynamicInfo> courtKtgg = Collections.EMPTY_LIST;
        List<DynamicInfo> judgeProcess = Collections.EMPTY_LIST;
        List<DynamicInfo> judgementWenshu = Collections.EMPTY_LIST;
        List<DynamicInfo> bulletin = Collections.EMPTY_LIST;
        List<DynamicInfo> bid = Collections.EMPTY_LIST;
        List<DynamicInfo> listingNotice = Collections.EMPTY_LIST;
        List<DynamicInfo> annualReport = Collections.EMPTY_LIST;
        List<DynamicInfo> patent = Collections.EMPTY_LIST;
        List<DynamicInfo> shixinInfo = Collections.EMPTY_LIST;
        List<DynamicInfo> zhixingInfo = Collections.EMPTY_LIST;
        List<DynamicInfo> owingTax = Collections.EMPTY_LIST;
        List<DynamicInfo> taxPayerLevelA = Collections.EMPTY_LIST;
        List<DynamicInfo> penalty = Collections.EMPTY_LIST;

        String startTime = getOneMonthBefore();
        switch (infoType) {
            case "_all":
                changeRecords = dynamicInfoRepo.getChangeRecords(companyName, startTime);
                courtKtgg = dynamicInfoRepo.getCourtKtgg(companyName, startTime);
                judgeProcess = dynamicInfoRepo.getJudgeProcess(companyName, startTime);
                judgementWenshu = dynamicInfoRepo.getJudgementWenshu(companyName, startTime);
                bulletin = dynamicInfoRepo.getBulletin(companyName, startTime);
                bid = dynamicInfoRepo.getBid(companyName, startTime);
                listingNotice = dynamicInfoRepo.getListingNotice(stockCodes, startTime);
                annualReport = dynamicInfoRepo.getAnnualReport(companyName, startTime);
                patent = dynamicInfoRepo.getPatent(companyName, startTime);
                shixinInfo = dynamicInfoRepo.getShixinInfo(companyName, startTime);
                zhixingInfo = dynamicInfoRepo.getZhixingInfo(companyName, startTime);
                owingTax = dynamicInfoRepo.getOwingTax(companyName, startTime);
                taxPayerLevelA = dynamicInfoRepo.getTaxPayerLevelA(companyName, startTime);
                penalty = dynamicInfoRepo.getPenalty(companyName, startTime);
                break;
            case "risk":
                courtKtgg = dynamicInfoRepo.getCourtKtgg(companyName, startTime);
                judgeProcess = dynamicInfoRepo.getJudgeProcess(companyName, startTime);
                judgementWenshu = dynamicInfoRepo.getJudgementWenshu(companyName, startTime);
                bulletin = dynamicInfoRepo.getBulletin(companyName, startTime);
                shixinInfo = dynamicInfoRepo.getShixinInfo(companyName, startTime);
                zhixingInfo = dynamicInfoRepo.getZhixingInfo(companyName, startTime);
                owingTax = dynamicInfoRepo.getOwingTax(companyName, startTime);
                penalty = dynamicInfoRepo.getPenalty(companyName, startTime);
                break;
            case "marketing":
                bid = dynamicInfoRepo.getBid(companyName, startTime);
                patent = dynamicInfoRepo.getPatent(companyName, startTime);
                taxPayerLevelA = dynamicInfoRepo.getTaxPayerLevelA(companyName, startTime);
                break;
            case "unknown":
                changeRecords = dynamicInfoRepo.getChangeRecords(companyName, startTime);
                annualReport = dynamicInfoRepo.getAnnualReport(companyName, startTime);
                listingNotice = dynamicInfoRepo.getListingNotice(stockCodes, startTime);
                break;
            default:

        }
        //对所有数据排序
        result.addAll(changeRecords);
        result.addAll(courtKtgg);
        result.addAll(judgeProcess);
        result.addAll(judgementWenshu);
        result.addAll(bulletin);
        result.addAll(bid);
        result.addAll(listingNotice);
        result.addAll(annualReport);
        result.addAll(patent);
        result.addAll(shixinInfo);
        result.addAll(zhixingInfo);
        result.addAll(owingTax);
        result.addAll(taxPayerLevelA);
        result.addAll(penalty);
        sortByTimeDesc(result);
        return result;
    }

    private DataItem getConcert(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem concert = new DataItem();
        List concertList = redisRepo.getGraphConcert(companyName);
        if (concertList == null || concertList.size() < 1) {
            concertList = detailGraphRepo.getGraphConcert(companyName);
        }
        if (onlyCount != null && !onlyCount) {
            List concertData = PageUtil.pageList(concertList, offset, count);
            concert.setData(concertData);
        }
        redisRepo.pushGraphConcert(concertList, companyName);
        concert.setTotalCount(concertList == null ? 0l : (long) concertList.size());
        return concert;
    }

    private DataItem getContributorInvestOffice(String companyName, Boolean onlyCount, Integer offset, Integer count) {
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
        long c = 0;
        if(contributorList!=null){
        	c = (long) contributorList.size();
        }
        contributor.setTotalCount(c);
        return contributor;
    }

    private DataItem getKeyPersonInvestOffice(String companyName, Boolean onlyCount, Integer offset, Integer count) {
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

    //获取一个月前的时间
    private String getOneMonthBefore() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/shanghai"));
        calendar.add(Calendar.MONTH, -1);
        return dayFormatter.format(calendar.getTime());
    }

    //对动态信息倒序排序
    private List<DynamicInfo> sortByTimeDesc(List<DynamicInfo> dynamicInfoList) {
        Collections.sort(dynamicInfoList, (o1, o2) -> {
            if (o1.getDate() == null && o2.getDate() == null) {
                return 0;
            }
            if (o1.getDate() == null) {
                return 1;
            } else if (o2.getDate() == null) {
                return -1;
            } else {
                return o2.getDate().compareTo(o1.getDate());
            }
        });
        return dynamicInfoList;
    }
}
