package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.tab.first.RiskInfo;
import com.haizhi.iap.search.controller.model2.tab.second.AllPenalty;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.service.RiskInfoService;
import com.haizhi.iap.search.utils.PageUtil;
import lombok.Setter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/11/9.
 */
@Service
public class RiskInfoServiceImpl implements RiskInfoService {
    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Override
    public RiskInfo getPageByThirdType(String companyName, Boolean onlyCount, String thirdTypeName, Integer offset, Integer count) {
        RiskInfo.RiskThirdType thirdType = RiskInfo.RiskThirdType.get(thirdTypeName);
        if (thirdType == null) {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_THIRD_TYPE);
        } else {
            RiskInfo riskInfo = new RiskInfo();
            AllPenalty allPenalty = new AllPenalty();
            switch (thirdType) {
                case PENALTY:
                    allPenalty.setPenalty(getPenalty(companyName, onlyCount, offset, count));
                    break;
                case CUSTOMS_PENALTY:
                    allPenalty.setCustomsPenalty(getCustomsPenalty(companyName, onlyCount));
                    break;
            }
            riskInfo.setAllPenalty(allPenalty);
            return riskInfo;
        }
    }

    @Override
    public RiskInfo getPageBySubType(Map<String, Object> basic, SearchRequest request) {
        RiskInfo.RiskSubType subType = RiskInfo.RiskSubType.get(request.getSubType());
        if (subType == null) {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        } else {
            RiskInfo riskInfo = new RiskInfo();

            switch (subType) {
                case COURT_KTGG:
                    riskInfo.setCourtKtgg(getCourtKtgg(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case COURT_FYGG:
                    riskInfo.setCourtFygg(getCourtFygg(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case JUDGEMENT_WENSHU:
                    riskInfo.setJudgement(getJudgement(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case JUDGE_PROCESS:
                    riskInfo.setJudgeProcess(getJudgeProcess(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case ALL_PENALTY:
                    riskInfo.setAllPenalty(getAllPenalty(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case OWING_TAX:
                    riskInfo.setOwingTax(getOwingTax(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case SHIXIN_INFO:
                    riskInfo.setShixinInfo(getShixinInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case ZHIXING_INFO:
                    riskInfo.setZhixingInfo(getZhixingInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case ENVIRONMENT_PROTECTION:
                    riskInfo.setEnvInfo(getEnvInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case EQUITY_PLEDGED_INFO:
                    riskInfo.setEquityPledgedInfo(getEquityPledgedInfo(basic, request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case CHATTEL_MORTGAGE_INFO:
                    riskInfo.setChattelMortgageInfo(getChattelMortgageInfo(basic, request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
                case ABNORMAL_OPERATION_INFO:
                    riskInfo.setAbnormalOperationInfo(getAbnormalOperationInfo(basic, request.getOnlyCount(), request.getOffset(), request.getCount()));
                    break;
            }
            return riskInfo;
        }
    }

    /**
     * 开庭公告
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getCourtKtgg(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem courtKtgg = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> courtKtggData = enterpriseRepo.getCourtKtgg(companyName, offset, count);
            courtKtgg.setData(courtKtggData);
        }
        courtKtgg.setTotalCount(enterpriseRepo.countCourtKtgg(companyName));
        return courtKtgg;
    }

    /**
     * 法院公告
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getCourtFygg(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem courtFygg = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> courtFyggData = enterpriseRepo.getCourtFygg(companyName, offset, count);
            courtFygg.setData(courtFyggData);
        }
        courtFygg.setTotalCount(enterpriseRepo.countCourtFygg(companyName));
        return courtFygg;
    }

    /**
     * 裁判文书
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getJudgement(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem judgement = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> judgementData = enterpriseRepo.getJudgement(companyName, offset, count);
            judgement.setData(judgementData);
        }
        judgement.setTotalCount(enterpriseRepo.countJudgement(companyName));
        return judgement;
    }

    /**
     * 审断流程
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getJudgeProcess(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem judgeProcess = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> judgeProcessData = enterpriseRepo.getJudgeProcess(companyName, offset, count);
            judgeProcess.setData(judgeProcessData);
        }
        judgeProcess.setTotalCount(enterpriseRepo.countJudgeProcess(companyName));
        return judgeProcess;
    }

    /**
     * 行政处罚
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public AllPenalty getAllPenalty(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        AllPenalty allPenalty = new AllPenalty();
        allPenalty.setPenalty(getPenalty(companyName, onlyCounting, offset, count));
        allPenalty.setCustomsPenalty(getCustomsPenalty(companyName, onlyCounting));
        return allPenalty;
    }

    /**
     * 原有行政处罚
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    private DataItem getPenalty(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem penalty = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> penaltyData = enterpriseRepo.getPenalty(companyName, offset, count);
            penalty.setData(penaltyData);
        }
        penalty.setTotalCount(enterpriseRepo.countPenalty(companyName));
        return penalty;
    }

    /**
     * 海关行政处罚
     * @param companyName
     * @param onlyCounting
     * @return
     */
    private DataItem getCustomsPenalty(String companyName, Boolean onlyCounting) {
        Document customsInfo = enterpriseRepo.getCustomsInformation(companyName);
        List<Map> customsPenaltyList = customsInfo == null || customsInfo.get("penalty_list") == null ?
                Collections.EMPTY_LIST : (List<Map>) customsInfo.get("penalty_list");
        DataItem customsPenalty = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            customsPenalty.setData(customsPenaltyList);
        }
        customsPenalty.setTotalCount((long) customsPenaltyList.size());
        return customsPenalty;
    }

    /**
     * 欠税公告
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getOwingTax(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem owingTax = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> owingTaxData = enterpriseRepo.getOwingTax(companyName, offset, count);
            owingTax.setData(owingTaxData);
        }
        owingTax.setTotalCount(enterpriseRepo.countOwingTax(companyName));
        return owingTax;
    }

    /**
     * 失信被执行人信息
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getShixinInfo(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem shixinInfo = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> shixinInfoData = enterpriseRepo.getShixinInfo(companyName, offset, count);
            shixinInfo.setData(shixinInfoData);
        }
        shixinInfo.setTotalCount(enterpriseRepo.countShixinInfo(companyName));
        return shixinInfo;
    }

    /**
     * 被执行人信息
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getZhixingInfo(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem zhixingInfo = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            List<Map> zhixingInfoData = enterpriseRepo.getZhixingInfo(companyName, offset, count);
            zhixingInfo.setData(zhixingInfoData);
        }
        zhixingInfo.setTotalCount(enterpriseRepo.countZhixingInfo(companyName));
        return zhixingInfo;
    }

    /**
     * 重点监控企业排污费征收公告
     * @param companyName
     * @param onlyCounting
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getEnvInfo(String companyName, Boolean onlyCounting, Integer offset, Integer count) {
        DataItem envInfo = new DataItem();
        if (onlyCounting != null && !onlyCounting) {
            Map envInfoData = enterpriseRepo.getEnvironmentProtection(companyName);
            envInfo.setData(envInfoData);
        }
        envInfo.setTotalCount(enterpriseRepo.countEnvironmentProtection(companyName));
        return envInfo;
    }

    /**
     * 动产抵押登记信息
     * @param basic
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getChattelMortgageInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem chattelMortgage = new DataItem();
        List<Map> chattelMortgageList = basic == null || basic.get("chattel_mortgage_info") == null ?
                Collections.EMPTY_LIST : (List<Map>) basic.get("chattel_mortgage_info");
        if(onlyCount != null && !onlyCount){
            chattelMortgage.setData(PageUtil.pageList(chattelMortgageList, offset, count));
        }
        chattelMortgage.setTotalCount((long) chattelMortgageList.size());
        return chattelMortgage;
    }

    /**
     * 股权出质登记信息
     * @param basic
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getEquityPledgedInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem equityPledged = new DataItem();
        List<Map> equityPledgedList = basic == null || basic.get("equity_pledged_info") == null ?
                Collections.EMPTY_LIST : (List<Map>) basic.get("equity_pledged_info");
        if(onlyCount != null && !onlyCount){
            equityPledged.setData(PageUtil.pageList(equityPledgedList, offset, count));
        }
        equityPledged.setTotalCount((long) equityPledgedList.size());
        return equityPledged;
    }

    /**
     * 列入经营异常名录信息
     * @param basic
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getAbnormalOperationInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem abnormalOperation = new DataItem();
        List<Map> abnormalOperationList = basic == null || basic.get("abnormal_operation_info") == null ?
                Collections.EMPTY_LIST : (List<Map>) basic.get("abnormal_operation_info");
        if(onlyCount != null && !onlyCount){
            abnormalOperation.setData(PageUtil.pageList(abnormalOperationList, offset, count));
        }
        abnormalOperation.setTotalCount((long) abnormalOperationList.size());
        return abnormalOperation;
    }
}
