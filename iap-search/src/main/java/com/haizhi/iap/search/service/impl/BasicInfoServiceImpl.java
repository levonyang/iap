package com.haizhi.iap.search.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.BeanUtil;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.controller.model.AnnualReport;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model.FinancialReport;
import com.haizhi.iap.search.controller.model.RegularReport;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.Sector;
import com.haizhi.iap.search.controller.model2.tab.first.BasicInfo;
import com.haizhi.iap.search.controller.model2.tab.second.CompanyInfo;
import com.haizhi.iap.search.controller.model2.tab.second.CustomsInfo;
import com.haizhi.iap.search.controller.model2.tab.second.ListInfo;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.repo.DetailGraphRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.FollowItemRepo;
import com.haizhi.iap.search.service.BasicInfoService;
import com.haizhi.iap.search.utils.PageUtil;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by chenbo on 2017/11/8.
 */
@Service
public class BasicInfoServiceImpl implements BasicInfoService {
    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Setter
    @Autowired
    FollowItemRepo followItemRepo;

    @Setter
    @Autowired
    DetailGraphRepo detailGraphRepo;

    @Override
    public BasicInfo getPageBySubType(Map<String, Object> companyBasic, SearchRequest request) {
        BasicInfo.BasicSubType subType = BasicInfo.BasicSubType.get(request.getSubType());
        if (subType == null) {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        } else {
            BasicInfo basicInfo = new BasicInfo();
            switch (subType) {
                case COMPANY_INFO:
                    CompanyInfo companyInfo = getCompanyInfo(companyBasic, request); //工商基本信息
                    basicInfo.setCompanyInfo(companyInfo);
                    break;
                case ANNUAL_REPORT:
                    basicInfo.setAnnualReport(getAnnualReport(request.getName(), request.getOnlyCount(),
                            request.getOffset(), request.getCount())); //
                    break;
                case LIST_INFO:
                    ListInfo listInfo = getListInfo(request);
                    basicInfo.setListInfo(listInfo);
                    break;
                case CUSTOMS_INFO:
                    CustomsInfo customsInfo = getCustomsInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount());
                    basicInfo.setCustomsInfo(customsInfo);
                    break;
            }
            return basicInfo;
        }
    }

    @Override
    public BasicInfo getPageByThirdType(Map<String, Object> companyBasic, SearchRequest request) {
        BasicInfo.BasicSubType subType = BasicInfo.BasicSubType.get(request.getSubType());
        if (subType == null) {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        } else {
            BasicInfo basicInfo = new BasicInfo();
            switch (subType) {
                case COMPANY_INFO:
                    CompanyInfo.CompanyInfoSubType companyInfoSubType = CompanyInfo.CompanyInfoSubType.get(request.getThirdType());
                    if (companyInfoSubType == null) {
                        throw new ServiceAccessException(SearchException.UNSUPPORTED_THIRD_TYPE);
                    } else {
                        CompanyInfo companyInfo = new CompanyInfo();
                        switch (companyInfoSubType) {
                            case GONGSHANG_BASIC:
                                companyInfo.setGongshangBasic(getGongshangBasic(companyBasic, request.getName(),
                                        request.getOnlyCount()));
                                break;
                            case SHAREHOLDER_INFORMATION:
                                companyInfo.setShareholderInfo(getShareholderInfo(request.getName(),
                                        request.getOnlyCount(), request.getOffset(), request.getCount()));
                                break;
                            case KEY_PERSON:
                                companyInfo.setKeyPerson(getKeyPerson(companyBasic, request.getOnlyCount()));
                                break;
                            case BRANCH:
                                companyInfo.setBranches(getBranches(companyBasic, request.getOnlyCount(),
                                        request.getOffset(), request.getCount()));
                                break;
                            case CHANGE_RECORDS:
                                companyInfo.setChangeRecords(getChangeRecords(companyBasic, request.getOnlyCount(),
                                        request.getOffset(), request.getCount()));
                                break;
                            case CHANGE_SHAREHOLDING_INFO:
                                companyInfo.setChangeShareholdingInfo(queryChangeShareholdingInfo(companyBasic, request.getOnlyCount(),
                                        request.getOffset(), request.getCount()));
                                break;
                        }
                        basicInfo.setCompanyInfo(companyInfo);
                    }
                    break;
                case LIST_INFO:
                    ListInfo.ListInfoSubType listInfoSubType = ListInfo.ListInfoSubType.get(request.getThirdType());
                    if (listInfoSubType == null) {
                        throw new ServiceAccessException(SearchException.UNSUPPORTED_THIRD_TYPE);
                    } else {
                        ListInfo listInfo = new ListInfo();
                        Pair<String, List<Sector>> stockCodeAndSectors = getStockCodeAndSectors(request);
                        if (stockCodeAndSectors != null) {
                            String stockCode = Strings.isNullOrEmpty(request.getStockCode()) ?
                                    stockCodeAndSectors.getLeft() : request.getStockCode();
                            Map<String, Object> listBasic = enterpriseRepo.getListingInfo(stockCode);
                            switch (listInfoSubType) {
                                case LISTING:
                                    listInfo.setListBasic(getListBasic(listBasic, request.getOnlyCount()));
                                    break;
                                case MANAGERS:
                                    listInfo.setManagers(getManagers(listBasic, request.getOnlyCount(),
                                            request.getOffset(), request.getCount()));
                                    break;
                                case RULES:
                                    listInfo.setRules(getRules(stockCode, request.getOnlyCount(),
                                            request.getOffset(), request.getCount()));
                                    break;
                                case SSGS_REGULAR_REPORT:
                                    listInfo.setSsgsRegularReport(getSsgsRegularReport(stockCode, request.getOnlyCount()));
                                    break;
                                case NOTICE:
                                    listInfo.setNotice(getNotice(stockCode, request.getOnlyCount(),
                                            request.getOffset(), request.getCount()));
                                    break;
                                case FINANCIAL_REPORT:
                                    listInfo.setFinancialReport(getFinancialReport(stockCode, request.getOnlyCount()));
                                    break;
                                case TOP_TEN_SHAREHOLDERS:
                                    listInfo.setTopTenShareholders(getTopTenShareholders(stockCode, request.getYearQuarter(),
                                            request.getOnlyCount(), request.getOffset(), request.getCount()));
                                    break;
                                case TOP_TEN_TRADABLE_SHAREHOLDERS:
                                    listInfo.setTopTenTradableShareholders(getTopTenTradableShareholers(stockCode, request.getYearQuarter(),
                                            request.getOnlyCount(), request.getOffset(), request.getCount()));
                                    break;
                                case FUND_TABLE:
                                    listInfo.setFundTable(getFundTable(stockCode, request.getYearQuarter(), request.getOnlyCount(),
                                            request.getOffset(), request.getCount()));
                                    break;
                            }
                        }
                        basicInfo.setListInfo(listInfo);
                    }
                    break;
                case CUSTOMS_INFO:
                    CustomsInfo.CustomsInfoSubType customsInfoSubType = CustomsInfo.CustomsInfoSubType.get(request.getThirdType());
                    if (customsInfoSubType == null) {
                        throw new ServiceAccessException(SearchException.UNSUPPORTED_THIRD_TYPE);
                    } else {
                        CustomsInfo customsInfo = new CustomsInfo();
                        Map<String, Object> customs = enterpriseRepo.getCustomsInformation(request.getName());
                        switch (customsInfoSubType) {
                            case CREDIT_RANK:
                                customsInfo.setCreditRank(getCreditRank(customs, request.getOnlyCount(),
                                        request.getOffset(), request.getCount()));
                                break;
                            case REGISTER_INFO:
                                customsInfo.setRegisterInfo(getRegisterInfo(customs, request.getOnlyCount()));
                                break;
                        }
                        basicInfo.setCustomsInfo(customsInfo);
                    }
                    break;
            }
            return basicInfo;
        }
    }

    /**
     * 工商信息
     * @param basic
     * @param request
     * @return
     */
    @Override
    public CompanyInfo getCompanyInfo(Map<String, Object> basic, SearchRequest request) {
        DataItem gongshangBasic = getGongshangBasic(basic, request.getName(), request.getOnlyCount()); //工商基本信息
        DataItem shareholderInfo = getShareholderInfo(request.getName(), request.getOnlyCount(), request.getOffset(), request.getCount()); //股东信息
        DataItem keyPerson = getKeyPerson(basic, request.getOnlyCount()); //高管信息
        DataItem branches = getBranches(basic, request.getOnlyCount(), request.getOffset(), request.getCount()); //分支机构
        DataItem changeRecords = getChangeRecords(basic, request.getOnlyCount(), request.getOffset(), request.getCount()); //工商变更
        DataItem changeShareholdingInfo = queryChangeShareholdingInfo(basic, request.getOnlyCount(), request.getOffset(), request.getCount()); //股权变更

        return new CompanyInfo(gongshangBasic, shareholderInfo, keyPerson, branches, changeRecords, changeShareholdingInfo);
    }

    /**
     * 企业年报
     * @param companyName 公司名称
     * @param onlyCount 是否只要计数
     * @param offset 分页
     * @param count 分页每页条数
     * @return
     */
    @Override
    public DataItem getAnnualReport(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem annualReport = new DataItem();

        List<AnnualReport> reportList = Lists.newArrayList();
        List<Map<String, Object>> reports = enterpriseRepo.getAnnualReport(companyName, offset, count);

        reports.stream().filter(doc -> doc.get("year") != null && !Strings.isNullOrEmpty(doc.get("year").toString())).forEach(doc -> {
            AnnualReport item = new AnnualReport();
            item.setYear(Integer.parseInt(doc.get("year").toString()));
            item.setCount(1);
            if (onlyCount != null && !onlyCount) {
                item.setReport(doc);
            }
            reportList.add(item);
        });

        Collections.sort(reportList, (report1, report2) -> report2.getYear().compareTo(report1.getYear()));
        if (onlyCount != null && !onlyCount) {
            annualReport.setData(reportList);
        }
        annualReport.setTotalCount((long) reportList.size());
        return annualReport;
    }

    /**
     * 工商基本信息
     * @param basic
     * @param companyName
     * @param onlyCount
     * @return
     */
    private DataItem getGongshangBasic(Map<String, Object> basic, String companyName, Boolean onlyCount) {
        DataItem gongshangBasic = new DataItem();
        if (onlyCount != null && !onlyCount) {
            //上市板块的处理
            Map<String, Object> cloneBasic = (Map<String, Object>) BeanUtil.deepClone(basic);
            cloneBasic.remove("branch");
            cloneBasic.remove("changerecords");
            cloneBasic.remove("key_person");
            Set<String> sectorSet = Sets.newHashSet();
            Map<String, String> sectorMap = enterpriseRepo.getSectorMap(companyName);
            if (sectorMap != null) {
                sectorSet = sectorMap.keySet();
            }
            cloneBasic.put("public_sector", String.join(",", sectorSet));
            cloneBasic.put("_followed_", followItemRepo.checkCompanyFollowed(companyName));
            cloneBasic.put("graph_id", "Company/" + SecretUtil.md5(companyName));

            // arangodb
            List<Map<String, Object>> actualControlManList = detailGraphRepo.getActualControlMan(companyName);
            cloneBasic.put("actual_control_man_list", actualControlManList);
            gongshangBasic.setData(cloneBasic);
        }
        gongshangBasic.setTotalCount(basic == null ? 0 : 1L);
        return gongshangBasic;
    }

    /**
     * 获取高管信息
     * @param basic
     * @param onlyCount
     * @return
     */
    private DataItem getKeyPerson(Map<String, Object> basic, Boolean onlyCount) {
        DataItem keyPerson = new DataItem();
        List keyPersonList = Lists.newArrayList();
        if (basic.get("key_person") != null && basic.get("key_person") instanceof List) {
            keyPersonList = (List) basic.get("key_person");
        }
        if (onlyCount != null && !onlyCount) {
            List<Map<String, Object>> keyPersonData = Lists.newArrayList();
            for (Object doc : keyPersonList) {
                if (doc instanceof Map) {
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("name", ((Map) doc).get("key_person_name"));
                    map.put("job", ((Map) doc).get("key_person_position"));
                    keyPersonData.add(map);
                }
            }
            keyPerson.setData(keyPersonData);
        }
        keyPerson.setTotalCount((long) keyPersonList.size());
        return keyPerson;
    }

    /**
     * 获取分支机构信息
     * @param basic
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    private DataItem getBranches(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem branches = new DataItem();
        List branchList = Lists.newArrayList();
        if (basic.get("branch") != null && basic.get("branch") instanceof List) {
            branchList = (List) basic.get("branch");
        }
        if (onlyCount != null && !onlyCount) {
            branches.setData(PageUtil.pageList(branchList, offset, count));
        }
        branches.setTotalCount((long) branchList.size());
        return branches;
    }

    /**
     * 工商变更
     * @param basic
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    private DataItem getChangeRecords(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem changeRecords = new DataItem();
        List changeList = Lists.newArrayList();
        if (basic.get("changerecords") != null && basic.get("changerecords") instanceof List) {
            changeList = (List) basic.get("changerecords");
        }
        if (onlyCount != null && !onlyCount) {
            changeRecords.setData(PageUtil.pageList(changeList, offset, count));
        }
        changeRecords.setTotalCount((long) changeList.size());
        return changeRecords;
    }

    /**
     * 获取股权变更信息
     * @param basic
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    private DataItem queryChangeShareholdingInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem changeShareholdingInfo = new DataItem();
        List changeList = Lists.newArrayList();
        Object changeShareholdingInfoObj = basic.get("change_shareholding_info");
        if ( changeShareholdingInfoObj != null && changeShareholdingInfoObj instanceof List) {
            changeList = (List) changeShareholdingInfoObj;
        }
        if (onlyCount != null && !onlyCount) {
            changeShareholdingInfo.setData(PageUtil.pageList(changeList, offset, count));
        }
        changeShareholdingInfo.setTotalCount((long) changeList.size());
        return changeShareholdingInfo;
    }


    //查arango的版本

    /**
     * 股东信息
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    private DataItem getShareholderInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem shareholderInfo = new DataItem();
        List<Map> holderList = detailGraphRepo.getInvest(companyName);
        if (onlyCount != null && !onlyCount) {
            shareholderInfo.setData(PageUtil.pageList(holderList, offset, count));
        }
        shareholderInfo.setTotalCount((long) holderList.size());
        return shareholderInfo;
    }

    //查mongo的版本
    /*private DataItem getShareholderInfo(Map<String, Object> basic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem shareholderInfo = new DataItem();
        List<Map> holderList = Lists.newArrayList();
        if (basic.get("shareholder_information") != null && basic.get("shareholder_information") instanceof List) {
            holderList = (List<Map>) basic.get("shareholder_information");
        }

        if (holderList.size() < 1 && basic.get("contributor_information") != null
                && basic.get("contributor_information") instanceof List) {
            holderList = (List<Map>) basic.get("contributor_information");
        }
        if (onlyCount != null && !onlyCount) {
            shareholderInfo.setData(enterpriseRepo.getShareholderInformation(holderList,
                    basic.get("registered_capital"), offset, count));
        }
        shareholderInfo.setTotalCount((long) holderList.size());
        return shareholderInfo;
    }*/


    @Override
    public ListInfo getListInfo(SearchRequest request) {

        ListInfo listInfo = new ListInfo();
        Pair<String, List<Sector>> stockCodeAndSectors = getStockCodeAndSectors(request);
        if (stockCodeAndSectors != null) {
            String stockCode = Strings.isNullOrEmpty(request.getStockCode()) ?
                    stockCodeAndSectors.getLeft() : request.getStockCode();
            Map<String, Object> listBasic = enterpriseRepo.getListingInfo(stockCode);
            listInfo.setSectorList(stockCodeAndSectors.getRight());
            listInfo.setListBasic(getListBasic(listBasic, request.getOnlyCount()));
            listInfo.setFinancialReport(getFinancialReport(stockCode, request.getOnlyCount()));
            listInfo.setManagers(getManagers(listBasic, request.getOnlyCount(), request.getOffset(), request.getCount()));
            listInfo.setNotice(getNotice(stockCode, request.getOnlyCount(), request.getOffset(), request.getCount()));
            listInfo.setFundTable(getFundTable(stockCode, request.getYearQuarter(),
                    request.getOnlyCount(), request.getOffset(), request.getCount()));
            listInfo.setRules(getRules(stockCode, request.getOnlyCount(), request.getOffset(), request.getCount()));
            listInfo.setTopTenShareholders(getTopTenShareholders(stockCode, request.getYearQuarter(),
                    request.getOnlyCount(), request.getOffset(), request.getCount()));
            listInfo.setTopTenTradableShareholders(getTopTenTradableShareholers(stockCode, request.getYearQuarter(),
                    request.getOnlyCount(), request.getOffset(), request.getCount()));
            listInfo.setSsgsRegularReport(getSsgsRegularReport(stockCode, request.getOnlyCount()));

        }
        return listInfo;
    }

    private Pair<String, List<Sector>> getStockCodeAndSectors(SearchRequest request) {
        TreeMap<String, String> sectorMap = enterpriseRepo.getSectorMap(request.getName());
        List<String> stockCodeList = Lists.newArrayList(sectorMap.values());
        List<Sector> sectors = Lists.newArrayList();
        if (stockCodeList.size() < 1) {
            return null;
        } else if (!Strings.isNullOrEmpty(request.getStockCode())
                && !stockCodeList.contains(request.getStockCode())) {
            throw new ServiceAccessException(SearchException.UN_SUPPORTED_STOCK_CODE);
        } else {
            for (Map.Entry<String, String> entry : sectorMap.entrySet()) {
                sectors.add(new Sector(entry.getKey(), entry.getValue()));
            }
        }
        String stockCode;
        if (Strings.isNullOrEmpty(request.getStockCode())) {
            stockCode = sectorMap.firstEntry().getValue();
        } else {
            stockCode = request.getStockCode();
        }
        return Pair.of(stockCode, sectors);
    }

    private DataItem getListBasic(Map<String, Object> listBasic, Boolean onlyCount) {
        DataItem basicInfo = new DataItem();
        if (onlyCount != null && !onlyCount) {
            Map<String, Object> cloneBasic = (Map<String, Object>) BeanUtil.deepClone(listBasic);
            cloneBasic.remove("manager");
            basicInfo.setData(cloneBasic);
        }
        basicInfo.setTotalCount(listBasic == null ? 0 : 1L);
        return basicInfo;
    }

    private DataItem getFinancialReport(String stockCode, Boolean onlyCount) {
        DataItem financialReport = new DataItem();

        FinancialReport financialReportData = enterpriseRepo.getFinancialReport(stockCode);
        if (onlyCount != null && !onlyCount) {
            financialReport.setData(financialReportData);
        }
        financialReport.setTotalCount(financialReportData == null ? 0 : 1L);
        return financialReport;
    }

    private DataItem getFundTable(String stockCode, String yearQuarter, Boolean onlyCount, Integer offset, Integer count) {
        DataItem fundTable = enterpriseRepo.getFundTable(stockCode, yearQuarter, offset, count);
        if(fundTable==null){
        	fundTable = new DataItem(Collections.emptyList(),0L);
        }
        if (onlyCount != null && onlyCount) {
            fundTable.setData(null);
        }
        return fundTable;
    }

    private DataItem getManagers(Map<String, Object> listBasic, Boolean onlyCount, Integer offset, Integer count) {
        DataItem managers = new DataItem();
        List<Map> managerList = listBasic.get("manager") == null ? Lists.newArrayList() : (List<Map>) listBasic.get("manager");
        if (onlyCount != null && !onlyCount) {
            managers.setData(PageUtil.pageList(managerList, offset, count));
        }
        managers.setTotalCount((long) managerList.size());
        return managers;
    }

    private DataItem getNotice(String stockCode, Boolean onlyCount, Integer offset, Integer count) {
        //获取notice
        DataItem notice = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> noticeList = enterpriseRepo.getNotice(stockCode, offset, count);
            notice.setData(noticeList);
        }
        notice.setTotalCount(enterpriseRepo.countNotice(stockCode));
        return notice;
    }

    private DataItem getRules(String stockCode, Boolean onlyCount, Integer offset, Integer count) {
        DataItem rules = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> ruleList = enterpriseRepo.getRules(stockCode, offset, count);
            rules.setData(ruleList);
        }
        rules.setTotalCount(enterpriseRepo.countRules(stockCode));
        return rules;
    }

    private DataItem getSsgsRegularReport(String stockCode, Boolean onlyCount) {
        DataItem ssgsRegularReport = new DataItem();
        RegularReport regularReport = enterpriseRepo.getRegularReport(stockCode);
        if (onlyCount != null && !onlyCount) {
            ssgsRegularReport.setData(regularReport);
        }
        ssgsRegularReport.setTotalCount(regularReport.getTotalCount());
        return ssgsRegularReport;
    }

    private DataItem getTopTenShareholders(String stockCode, String yearQuarter, Boolean onlyCount, Integer offset, Integer count) {
        DataItem topTenSH = enterpriseRepo.getTopTenShareholders(stockCode, yearQuarter, offset, count);

        if (onlyCount != null && onlyCount && topTenSH !=null) {
            topTenSH.setData(null);
        }
        return topTenSH;
    }

    private DataItem getTopTenTradableShareholers(String stockCode, String yearQuarter, Boolean onlyCount, Integer offset, Integer count) {
        DataItem topTenTSH = enterpriseRepo.getTopTenTradableShareholders(stockCode, yearQuarter, offset, count);
        if (onlyCount != null && onlyCount&& topTenTSH !=null) {
            topTenTSH.setData(null);
        }
        return topTenTSH;
    }

    /**
     * 获取海关信息
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public CustomsInfo getCustomsInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        CustomsInfo customsInfo = new CustomsInfo();
        Document customs = enterpriseRepo.getCustomsInformation(companyName);
        customsInfo.setCreditRank(getCreditRank(customs, onlyCount, offset, count)); //信用等级
        customsInfo.setRegisterInfo(getRegisterInfo(customs, onlyCount)); //注册信息
        return customsInfo;
    }

    private DataItem getCreditRank(Map<String, Object> customsData, Boolean onlyCount, Integer offset, Integer count) {
        DataItem creditRank = new DataItem();
        List creditList = Lists.newArrayList();
        if (customsData != null && customsData.get("credit_rating_list") != null) {
            creditList = (List) customsData.get("credit_rating_list");
        }
        if (onlyCount != null && !onlyCount) {
            creditRank.setData(PageUtil.pageList(creditList, offset, count));
        }
        creditRank.setTotalCount((long) creditList.size());
        return creditRank;
    }

    private DataItem getRegisterInfo(Map<String, Object> customsData, Boolean onlyCount) {
        DataItem registerInfo = new DataItem();
        if (onlyCount != null && !onlyCount && customsData != null) {
            Map<String, Object> cloneData = (Map<String, Object>) BeanUtil.deepClone(customsData);
            cloneData.remove("credit_rating_list");
            registerInfo.setData(cloneData);
        }
        registerInfo.setTotalCount(customsData != null && customsData.size() > 0 ? 1L : 0);
        return registerInfo;
    }
}
