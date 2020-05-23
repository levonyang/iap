package com.haizhi.iap.search.service.impl;

import com.google.common.collect.Maps;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.SearchType;
import com.haizhi.iap.search.controller.model2.tab.first.*;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.BrowsingHistory;
import com.haizhi.iap.search.model.DynamicInfo;
import com.haizhi.iap.search.repo.BrowsingHistoryRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.service.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment;
import org.bson.Document;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.print.DocFlavor.STRING;
import javax.print.attribute.HashAttributeSet;

/**
 * Created by chenbo on 2017/11/7.
 */
@Slf4j
@Service
public class CompanyServiceImpl implements CompanyService {
    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Setter
    @Autowired
    BrowsingHistoryRepo browsingHistoryRepo;

    @Setter
    @Autowired
    ProfileInfoService profileInfoService;

    @Setter
    @Autowired
    BasicInfoService basicInfoService;

    @Setter
    @Autowired
    AssociatedRelationService relationService;

    @Setter
    @Autowired
    RiskInfoService riskInfoService;

    @Setter
    @Autowired
    MarketInfoService marketInfoService;

    @Setter
    @Autowired
    PublicSentimentService sentimentService;

    @Override
    public Object search(SearchRequest request) {
        Object data = null;
        SearchType searchType = SearchType.get(request.getType());
        if (request.getType() == null) {
            searchType = SearchType.ALL;
        }
        if (request.getName() != null) {
            Document basic = enterpriseRepo.getBasic(request.getName());
            if (basic == null) {
                throw new ServiceAccessException(SearchException.UN_CRAW_DATA);
            } else {
                createSearchHistory(request.getName());
            }
            switch (searchType) {
                case ALL: //当选择查看所有模块时，就只能看到每个模块的信息数据量(即只有数字)
                    request.setSubType(null);
                    Map<String, Object> all = Maps.newHashMap();
                    all.put(SearchType.PROFILE_INFO.getName(), profileInfo(basic, request));
                    all.put(SearchType.BASIC_INFO.getName(), basicInfo(basic, request));
                    all.put(SearchType.ASSOCIATED_RELATION.getName(), associatedRelation(request));
                    all.put(SearchType.RISK_INFO.getName(), riskInfo(basic, request));
                    all.put(SearchType.MARKET_INFO.getName(), marketInfo(basic, request));
                    all.put(SearchType.PUBLIC_SENTIMENT.getName(), publicSentiment(request));
                    data = all;
                    break;
                case PROFILE_INFO:
                    data = profileInfo(basic, request); //概览
                    break;
                case BASIC_INFO:
                    data = basicInfo(basic, request); //基本信息
                    break;
                case ASSOCIATED_RELATION:
                    data = associatedRelation(request); //关联关系
                    break;
                case RISK_INFO:
                    data = riskInfo(basic, request); //风险信息
                    break;
                case MARKET_INFO:
                    data = marketInfo(basic, request); //
                    break;
                case PUBLIC_SENTIMENT:
                    data = publicSentiment(request);
                    break;
                default:
                    data = null;
            }
        }
        return data;
    }

    @Override
    public Object searchV3(SearchRequest request) {
        Object data = null;
        SearchType searchType = SearchType.get(request.getType());
        if (request.getType() == null) {
            searchType = SearchType.ALL;
        }
        if (request.getName() != null) {
            Document basic = enterpriseRepo.getBasic(request.getName());
            if (basic == null) {
                throw new ServiceAccessException(SearchException.UN_CRAW_DATA);
            } else {
                createSearchHistory(request.getName());
            }
            switch (searchType) {
                case ALL: //当选择查看所有模块时，就只能看到每个模块的信息数据量(即只有数字)
                    request.setSubType(null);
                    Map<String, Object> all = Maps.newHashMap();
                    all.put(SearchType.PROFILE_INFO.getName(), profileInfo(basic, request));
                    all.put(SearchType.BASIC_INFO.getName(), basicInfo(basic, request));
                    all.put(SearchType.ASSOCIATED_RELATION.getName(), associatedRelation(request));
                    all.put(SearchType.RISK_INFO.getName(), riskInfo(basic, request));
                    all.put(SearchType.MARKET_INFO.getName(), marketInfo(basic, request));
                    all.put(SearchType.PUBLIC_SENTIMENT.getName(), publicSentiment(request));
                    data = all;
                    break;
                case PROFILE_INFO:
                    data = profileInfo(basic, request); //概览
                    break;
                case BASIC_INFO:
                    data = basicInfo(basic, request); //基本信息
                    break;
                case ASSOCIATED_RELATION:
                    data = associatedRelation(request); //关联关系
                    break;
                case RISK_INFO:
                    data = riskInfo(basic, request); //风险信息
                    break;
                case MARKET_INFO:
                    data = marketInfo(basic, request); //
                    break;
                case PUBLIC_SENTIMENT:
                    data = publicSentiment(request);
                    break;
                default:
                    data = null;
            }
        }
        return data;
    }

    private void createSearchHistory(String companyName) {
        if (companyName == null) {
            return;
        }

        Long userId = DefaultSecurityContext.getUserId();
        if (userId != null) {
            BrowsingHistory history = browsingHistoryRepo.findByUserAndCompany(userId, companyName);
            if (history == null) {
                history = new BrowsingHistory();
                history.setUserId(userId);
                history.setCompany(companyName);
                browsingHistoryRepo.create(history);
            } else {
                browsingHistoryRepo.updateHistory(history.getId());
            }
        }
    }

    public boolean isExists(String companyName) {
        Map basic = enterpriseRepo.getBasic(companyName);
        return basic != null;
    }

    /**
     * 概览信息
     * @param companyBasic 公司工商信息
     * @param request 查询请求
     * @return
     */
    private ProfileInfo profileInfo(Map<String, Object> companyBasic, SearchRequest request){
        ProfileInfo profileInfo= null;
        if(!Strings.isNullOrEmpty(request.getSubType()) && !Strings.isNullOrEmpty(request.getThirdType())){
            profileInfo = profileInfoService.getPageByThirdType(request); //查询三级模块内容
        } else if (!Strings.isNullOrEmpty(request.getSubType())) {
            profileInfo = profileInfoService.getPageBySubType(companyBasic, request); //查询二级模块内容
        } else { //如果没有指定二三级模块，就查看当前模块下的所有二级默认内容
            profileInfo = new ProfileInfo();
            profileInfo.setSelfInfo(profileInfoService.getSelfInfo(companyBasic, request.getOnlyCount()));
            profileInfo.setAssociatedInfo(profileInfoService.getAssociatedInfo(request.getName(),
                    request.getOnlyCount(), request.getOffset(), request.getCount()));
            profileInfo.setDynamicInfo(profileInfoService.getDynamicInfo(request.getName(), null,
                    request.getOnlyCount(), request.getOffset(), request.getCount()));
        }

        //以下为处理DynamicInfo里的时间date，把时间date里的.0去掉
       try {
            if (null != profileInfo.getDynamicInfo()){
                Object data= profileInfo.getDynamicInfo().getData();
                if ((null!=data)&&(data instanceof List<?>)){
                    List<DynamicInfo> list=(List<DynamicInfo>)data;
                    if(!CollectionUtils.isEmpty(list)){
                        for (DynamicInfo dynamicInfo : list) {
                            if (!StringUtils.isEmpty(dynamicInfo.getDate())){
                                dynamicInfo.setDate(dynamicInfo.getDate().replace(".0", ""));
                            }
                        }
                    }

                }
                profileInfo.getDynamicInfo().setData(data);
            }

		} catch (Exception e) {
			log.error("强转报错，为了去掉date数据的.0："+e.getMessage());
		}
        return profileInfo;
    }

    /**
     * 基本信息
     * @param basic
     * @param request
     * @return
     */
    private BasicInfo basicInfo(Map<String, Object> basic, SearchRequest request) {
        BasicInfo basicInfo = null;
        if (!Strings.isNullOrEmpty(request.getSubType()) && !Strings.isNullOrEmpty(request.getThirdType())) {
            basicInfo = basicInfoService.getPageByThirdType(basic, request);
        } else if (!Strings.isNullOrEmpty(request.getSubType())) {
            basicInfo = basicInfoService.getPageBySubType(basic, request);
        } else {
            basicInfo = new BasicInfo();
            basicInfo.setCompanyInfo(basicInfoService.getCompanyInfo(basic, request)); //公司信息
            basicInfo.setAnnualReport(basicInfoService.getAnnualReport(request.getName(), request.getOnlyCount(),
                    request.getOffset(), request.getCount())); //企业年报
            basicInfo.setListInfo(basicInfoService.getListInfo(request));
            basicInfo.setCustomsInfo(basicInfoService.getCustomsInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount())); //海关信息
        }
        
        
        try{
        	//经营期限的开始时间大于当前，就不显示
        	Map<String, Object> data= new HashMap<String, Object>();
            Map<String, Object> map=  (Map<String, Object>)(basicInfo.getCompanyInfo().getGongshangBasic().getData());
            String registered_date=String.valueOf(map.get("registered_date"));
            String hezhun_date=String.valueOf(map.get("hezhun_date"));
            
            if(new SimpleDateFormat("yyyy-MM-dd").parse(registered_date).getTime()>new Date().getTime()){
            	map.put("registered_date", "");
            }
            if(new SimpleDateFormat("yyyy-MM-dd").parse(hezhun_date).getTime()>new Date().getTime()){
            	map.put("hezhun_date", "");
            }  
        }catch (Exception e) {
		}
        
        
        
        return basicInfo;
    }

    /**
     * 获取关联关系
     * @param request
     * @return
     */
    private AssociatedRelation associatedRelation(SearchRequest request) {
        AssociatedRelation relation = null;
        if (Strings.isNullOrEmpty(request.getSubType())) {
            relation = new AssociatedRelation();
            relation.setInvest(relationService.getInvest(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount())); //对外投资
            relation.setInvestEvents(relationService.getInvestEvents(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount())); //投资信息
            relation.setFinancialEvents(relationService.getFinancialEvents(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount())); //
            relation.setAcquirerEvents(relationService.getAcquirerEvents(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            relation.setExitEvents(relationService.getExitEvents(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            relation.setConcert(relationService.getConcert(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount())); //一致行动关系
            relation.setContributorInvestOffice(relationService.getContributorInvestOffice(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount())); //股东对外投资及任职
            relation.setKeyPersonInvestOffice(relationService.getKeyPersonInvestOffice(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount())); //高管对外投资及任职
        } else {
            relation = relationService.getPageBySubType(request.getName(), request.getOnlyCount(),
                    request.getSubType(), request.getOffset(), request.getCount());
        }

        return relation;
    }

    /**
     * 风险信息
     * @param basic
     * @param request
     * @return
     */
    private RiskInfo riskInfo(Map<String, Object> basic, SearchRequest request) {
        RiskInfo riskInfo = null;
        if (!Strings.isNullOrEmpty(request.getSubType()) && !Strings.isNullOrEmpty(request.getThirdType())) {
            riskInfo = riskInfoService.getPageByThirdType(request.getName(), request.getOnlyCount(),
                    request.getThirdType(), request.getOffset(), request.getCount());
        } else if (!Strings.isNullOrEmpty(request.getSubType())) {
            riskInfo = riskInfoService.getPageBySubType(basic, request);
        } else {
            riskInfo = new RiskInfo();
            riskInfo.setCourtKtgg(riskInfoService.getCourtKtgg(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setCourtFygg(riskInfoService.getCourtFygg(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setJudgement(riskInfoService.getJudgement(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setJudgeProcess(riskInfoService.getJudgeProcess(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setAllPenalty(riskInfoService.getAllPenalty(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setOwingTax(riskInfoService.getOwingTax(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setShixinInfo(riskInfoService.getShixinInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setZhixingInfo(riskInfoService.getZhixingInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setEnvInfo(riskInfoService.getEnvInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setChattelMortgageInfo(riskInfoService.getChattelMortgageInfo(basic, request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setEquityPledgedInfo(riskInfoService.getEquityPledgedInfo(basic, request.getOnlyCount(), request.getOffset(), request.getCount()));
            riskInfo.setAbnormalOperationInfo(riskInfoService.getAbnormalOperationInfo(basic, request.getOnlyCount(), request.getOffset(), request.getCount()));
        }
        return riskInfo;
    }

    /**
     * 资产/交易
     * @param basic
     * @param request
     * @return
     */
    private MarketInfo marketInfo(Map<String, Object> basic, SearchRequest request) {
        MarketInfo marketInfo = null;
        if (!Strings.isNullOrEmpty(request.getSubType())) {
            marketInfo = marketInfoService.getPageBySubType(request.getName(), request.getOnlyCount(),
                    request.getSubType(), basic, request.getOffset(), request.getCount());
        } else {
            marketInfo = new MarketInfo();
            marketInfo.setBidInfo(marketInfoService.getBidInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            marketInfo.setWinInfo(marketInfoService.getWinInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            marketInfo.setLandAuction(marketInfoService.getLandAuction(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            marketInfo.setPatent(marketInfoService.getPatent(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            marketInfo.setTaxPayerLevelA(marketInfoService.getTaxPayerLevelA(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
            marketInfo.setHiringInfo(marketInfoService.getHiringInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
        }
        return marketInfo;
    }

    private PublicSentiment publicSentiment(SearchRequest request) {
        PublicSentiment publicSentiment = null;
        if (!Strings.isNullOrEmpty(request.getSubType()) && !Strings.isNullOrEmpty(request.getThirdType())) {
            publicSentiment = sentimentService.getPageByThirdType(request.getName(), request.getOnlyCount(),
                    request.getSubType(), request.getThirdType(), request.getOffset(), request.getCount());
        } else if (!Strings.isNullOrEmpty(request.getSubType())) {
            publicSentiment = sentimentService.getPageBySubType(request.getName(), request.getOnlyCount(),
                    request.getSubType(), request.getOffset(), request.getCount());
        } else {
            publicSentiment = new PublicSentiment();
            publicSentiment.setBaiduNews(sentimentService.getBaiduNews(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
        }
        return publicSentiment;
    }
}
