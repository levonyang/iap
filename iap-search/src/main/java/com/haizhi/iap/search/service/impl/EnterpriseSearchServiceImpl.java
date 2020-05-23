package com.haizhi.iap.search.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.PageUtil;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.conf.PublicSectorConf;
import com.haizhi.iap.search.controller.model.AcquirerEvents;
import com.haizhi.iap.search.controller.model.AnnualReport;
import com.haizhi.iap.search.controller.model.Basic;
import com.haizhi.iap.search.controller.model.Bidding;
import com.haizhi.iap.search.controller.model.Contact;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model.EnterpriseReq;
import com.haizhi.iap.search.controller.model.FinancialReport;
import com.haizhi.iap.search.controller.model.IntellectualProperty;
import com.haizhi.iap.search.controller.model.InvestInstitution;
import com.haizhi.iap.search.controller.model.Investment;
import com.haizhi.iap.search.controller.model.Listing;
import com.haizhi.iap.search.controller.model.RegularReport;
import com.haizhi.iap.search.controller.model.Risk;
import com.haizhi.iap.search.controller.model.Sentiment;
import com.haizhi.iap.search.controller.model.SentimentDataItem;
import com.haizhi.iap.search.enums.EnterpriseSearchType;
import com.haizhi.iap.search.enums.SentimentType;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.BrowsingHistory;
import com.haizhi.iap.search.model.DynamicInfo;
import com.haizhi.iap.search.repo.BrowsingHistoryRepo;
import com.haizhi.iap.search.repo.DetailGraphRepo;
import com.haizhi.iap.search.repo.DynamicInfoRepo;
import com.haizhi.iap.search.repo.ESEnterpriseSearchRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.EnterpriseRepoUtil;
import com.haizhi.iap.search.repo.ExchangeRateRepo;
import com.haizhi.iap.search.repo.GraphRepo;
import com.haizhi.iap.search.service.EnterpriseSearchService;
import com.haizhi.iap.search.utils.NumberUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by chenbo on 17/2/13.
 */
@Slf4j
@Service
public class EnterpriseSearchServiceImpl implements EnterpriseSearchService {

    @Setter
    @Autowired
    private EnterpriseRepo enterpriseRepo;

    @Setter
    @Autowired
    ESEnterpriseSearchRepo esEnterpriseSearchRepo;

    @Setter
    @Autowired
    private GraphRepo graphRepo;

    @Setter
    @Autowired
    private DetailGraphRepo detailGraphRepo;

    @Setter
    @Autowired
    private DynamicInfoRepo dynamicInfoRepo;

    @Setter
    @Autowired
    private ExchangeRateRepo exchangeRateRepo;

    @Setter
    @Autowired
    BrowsingHistoryRepo browsingHistoryRepo;

    private DateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Object search(EnterpriseReq req) {
        String type = req.getType();
        Boolean isCompanyExists = isStored(req.getName());

        if (isCompanyExists) {
            Long userId = DefaultSecurityContext.getUserId();
            if (userId != null) {
                BrowsingHistory history = browsingHistoryRepo.findByUserAndCompany(userId, req.getName());
                if (history == null) {
                    history = new BrowsingHistory();
                    history.setUserId(userId);
                    history.setCompany(req.getName());
                    browsingHistoryRepo.create(history);
                } else {
                    browsingHistoryRepo.updateHistory(history.getId());
                }
            }
        }

        Object data = null;
        if (type.equals(EnterpriseSearchType.ALL.getName())) {
            req.setSubType(null);
            Map<String, Object> all = Maps.newHashMap();
            all.put(EnterpriseSearchType.BASIC.getName(), basic(req));
            all.put(EnterpriseSearchType.ANNUAL_REPORT.getName(), annualReport(req));
            all.put(EnterpriseSearchType.LIST.getName(), listing(req));
            all.put(EnterpriseSearchType.INVESTMENT.getName(), invest(req));
            all.put(EnterpriseSearchType.INTELLECTUAL_PROPERTY.getName(), intellectualProperty(req));
            all.put(EnterpriseSearchType.BIDDING.getName(), bidding(req));
            all.put(EnterpriseSearchType.RISK.getName(), risk(req));
            all.put(EnterpriseSearchType.PUBLIC_SENTIMENT.getName(), publicSentiment(req));
            all.put(EnterpriseSearchType.INVESTMENT_INSTITUTION.getName(), investInstitution(req));
            data = all;

        } else if (type.equals(EnterpriseSearchType.BASIC.getName())) {
            data = basic(req);
        } else if (type.equals(EnterpriseSearchType.ANNUAL_REPORT.getName())) {
            data = annualReport(req);
        } else if (type.equals(EnterpriseSearchType.LIST.getName())) {
            data = listing(req);
        } else if (type.equals(EnterpriseSearchType.INVESTMENT.getName())) {
            data = invest(req);
        } else if (type.equals(EnterpriseSearchType.INTELLECTUAL_PROPERTY.getName())) {
            data = intellectualProperty(req);
        } else if (type.equals(EnterpriseSearchType.BIDDING.getName())) {
            data = bidding(req);
        } else if (type.equals(EnterpriseSearchType.RISK.getName())) {
            data = risk(req);
        } else if (type.equals(EnterpriseSearchType.PUBLIC_SENTIMENT.getName())) {
            data = publicSentiment(req);
        } else if (type.equals(EnterpriseSearchType.INVESTMENT_INSTITUTION.getName())) {
            data = investInstitution(req);
        }
        return data;
    }

    @Override
    public Basic basic(EnterpriseReq req) {
        Document doc = enterpriseRepo.getBasic(req.getName());
        if (doc == null) {
            throw new ServiceAccessException(SearchException.UN_CRAW_DATA);
        }

        //分页的部分
        if (!Strings.isNullOrEmpty(req.getSubType())) {
            //list分页处理
            List<Map> list;
            if (req.getSubType().equals("change_records")) {
                String docKey = req.getSubType().equals("change_records") ? "changerecords" : req.getSubType();
                list = doc.get(docKey) == null ? Lists.newArrayList() : (List<Map>) doc.get(docKey);
            } else {
                list = doc.get(req.getSubType()) == null ? Lists.newArrayList() : (List<Map>) doc.get(req.getSubType());
            }
            DataItem item = null;
            if (req.getSubType().equals("shareholder_information")) {
//                List<Map> resultList = enterpriseRepo.getShareholderInformation(list, doc.get("registered_capital"),
//                        req.getOffset(), req.getCount());
                List<Map> resultList = getShareholderInfo(req.getName(),req.getOnlyCounting(),req.getOffset(),req.getCount());
                item = new DataItem(resultList, enterpriseRepo.countShareholderInformation(list));
            } else if (req.getSubType().equals("contributor_information")) {
                List<Map> resultList = enterpriseRepo.getContributorInformation(list, doc.get("registered_capital"),
                        req.getOffset(), req.getCount());
                item = new DataItem(resultList, enterpriseRepo.countContributorInformation(list));
            } else if (req.getSubType().equals("chattel_mortgage_info")) { //几个sb要的三个sub_type
                List<Map> resultList = doc.get("chattel_mortgage_info") == null ? Collections.EMPTY_LIST :
                        (List<Map>) doc.get("chattel_mortgage_info");
                item = new DataItem(resultList, (long) resultList.size());
            } else if (req.getSubType().equals("abnormal_operation_info")) {
                List<Map> resultList = doc.get("abnormal_operation_info") == null ? Collections.EMPTY_LIST :
                        (List<Map>) doc.get("abnormal_operation_info");
                item = new DataItem(resultList, (long) resultList.size());
            } else if (req.getSubType().equals("equity_pledged_info")) {
                List<Map> resultList = doc.get("equity_pledged_info") == null ? Collections.EMPTY_LIST :
                        (List<Map>) doc.get("equity_pledged_info");
                item = new DataItem(resultList, (long) resultList.size());
            } else {
                item = EnterpriseRepoUtil.getFromList(Basic.class, req.getSubType(), enterpriseRepo, list,
                        req.getOffset(), req.getCount());
            }
            return EnterpriseRepoUtil.set(Basic.class, req.getSubType(), item);
        } else {
            Basic basic = new Basic();
            List<Map> branchList = doc.get("branch") == null ? Lists.newArrayList() : (List<Map>) doc.get("branch");
            List<Map> changeRecordList = doc.get("changerecords") == null ? Lists.newArrayList() : (List<Map>) doc.get("changerecords");
            List<Map> keyPersonList = doc.get("key_person") == null ? Lists.newArrayList() : (List<Map>) doc.get("key_person");
            List<Map> holderList = doc.get("shareholder_information") == null ? Lists.newArrayList() : (List<Map>) doc.get("shareholder_information");
            List<Map> contributorList = doc.get("contributor_information") == null ? Lists.newArrayList() : (List<Map>) doc.get("contributor_information");


            DataItem branch = new DataItem();
            DataItem changeRecords = new DataItem();
            DataItem gongshangBasic = new DataItem();
            DataItem keyPerson = new DataItem();
            DataItem holderInfo = new DataItem();
            DataItem contributorInfo = new DataItem();
            DataItem customsInformation = new DataItem();
            Document customs = enterpriseRepo.getCustomsInformation(req.getName());

            if (!req.getOnlyCounting()) {
                doc.remove("branch");
                doc.remove("changerecords");
                doc.remove("key_person");
                doc.remove("shareholder_information");
                doc.remove("contributor_information");
                branch.setData(enterpriseRepo.getBranch(branchList, req.getOffset(), req.getCount()));
                changeRecords.setData(enterpriseRepo.getChangeRecords(changeRecordList, req.getOffset(), req.getCount()));
                keyPerson.setData(extractKeyPerson(keyPersonList));
               // holderInfo.setData(enterpriseRepo.getShareholderInformation(holderList, doc.get("registered_capital"), req.getOffset(), req.getCount()));
                holderInfo.setData(getShareholderInfo(req.getName(),req.getOnlyCounting(),req.getOffset(),req.getCount()));
                contributorInfo.setData(enterpriseRepo.getContributorInformation(contributorList, doc.get("registered_capital"), req.getOffset(), req.getCount()));
                customsInformation.setData(customs == null ? Collections.EMPTY_MAP : customs);
                //上市板块的处理
                Set<String> sectorSet = Sets.newHashSet();
                if (doc.get("stock_code") != null && doc.get("stock_code") instanceof String) {
                    Map<String, String> sectorConfMap = PublicSectorConf.getSectorConfMap();
                    List<String> stockCodes = Arrays.asList(((String) doc.get("stock_code")).split(","));
                    stockCodes.stream().filter(stockCode -> !Strings.isNullOrEmpty(stockCode)).forEach(stockCode -> {
                        sectorSet.addAll(sectorConfMap.keySet().stream().filter(key -> stockCode.startsWith(key)).map(sectorConfMap::get).collect(Collectors.toList()));
                    });
                }
                doc.put("public_sector", String.join(",", sectorSet));
                doc.put("_followed_", esEnterpriseSearchRepo.checkCompanyFollowed(req.getName()));
                doc.put("graph_id", "Company/" + SecretUtil.md5(req.getName()));

                // arangodb
                List<Map<String, Object>> manList = graphRepo.getActualControlMan(req.getName());
                List<Map<String, Object>> actualControlManList = Lists.newArrayList();

                for (Map<String, Object> data : manList) {
                    Map<String, Object> actualControlRes = null;
                    Map<String, Object> actualControlEdge = null;
                    if (data != null) {
                        if (data.get("controller") != null && data.get("controller") instanceof Map) {
                            actualControlRes = (Map<String, Object>) data.get("controller");
                        }
                        if (data.get("edge") != null && data.get("edge") instanceof Map) {
                            actualControlEdge = (Map<String, Object>) data.get("edge");
                        }
                    }

                    if (actualControlRes != null) {
                        //TODO arango继续不稳定的话,也要切出去,arango挂了之后基本信息不能加载
                        Map<String, Object> man = Maps.newHashMap();
                        man.put("name", actualControlRes.get("name"));  // Person / Company都是name属性
                        man.put("id", actualControlRes.get("_id"));
                        if (actualControlEdge.get("rule") != null) {
                            man.put("rule", processRuleMap(actualControlEdge.get("rule").toString()));
                            man.put("depth", actualControlEdge.get("depth"));
                        }
                        actualControlManList.add(man);
                    }

                }
                doc.put("actual_control_man_list", actualControlManList);

                gongshangBasic.setData(doc);
            }
            branch.setTotalCount(enterpriseRepo.countBranch(branchList));
            changeRecords.setTotalCount(enterpriseRepo.countChangeRecords(changeRecordList));
            gongshangBasic.setTotalCount(1L);
            keyPerson.setTotalCount((long) keyPersonList.size());
            holderInfo.setTotalCount(enterpriseRepo.countShareholderInformation(holderList));
            contributorInfo.setTotalCount(enterpriseRepo.countContributorInformation(contributorList));
            customsInformation.setTotalCount(customs != null && customs.size() > 0 ? 1L : 0);

            basic.setBranch(branch);
            basic.setChangeRecords(changeRecords);
            basic.setGongshangBasic(gongshangBasic);
            basic.setKeyPerson(keyPerson);
            basic.setShareholderInformation(holderInfo);
            basic.setContributorInformation(contributorInfo);
            basic.setCustomsInformation(customsInformation);
            return basic;
        }
    }

    private String processRuleMap(String rule) {
        if (rule.equals("个体企业以法人作为实际控制人")) {
            return "Rule1";
        } else if (rule.equals("有限公司或自然人持股超过一半，不使用亲属关系以及一致行动关系合并")) {
            return "Rule2";
        } else if (rule.equals("有限公司或自然人持股超过一半，使用亲属关系合并")) {
            return "Rule3";
        } else if (rule.equals("有限公司或自然人持股超过一半，使用一致行动关系合并")) {
            return "Rule4";
        }
        return rule;
    }

    /**
     * 年报暂时不用分页,年数很少
     *
     * @param req
     * @return
     */
    @Override
    public List<AnnualReport> annualReport(EnterpriseReq req) {
        List<AnnualReport> result = Lists.newArrayList();
        List<Map<String, Object>> reports = enterpriseRepo.getAnnualReport(req.getName(), req.getOffset(), req.getCount());

        reports.stream().filter(doc -> doc.get("year") != null && !Strings.isNullOrEmpty(doc.get("year").toString())).forEach(doc -> {
            AnnualReport annualReport = new AnnualReport();
            annualReport.setYear(Integer.valueOf(doc.get("year").toString()));
            annualReport.setCount(1);
            if (!req.getOnlyCounting()) {
                annualReport.setReport(doc);
            }
            result.add(annualReport);
        });
        Collections.sort(result, (report1, report2) -> report2.getYear().compareTo(report1.getYear()));
        return result;
    }

    /**
     * 上市信息中公告和章程会存在分页
     *
     * @param req
     * @return
     */
    public Listing listing(EnterpriseReq req) {
        TreeMap<String, String> stockTypeMap = enterpriseRepo.getSectorMap(req.getName());
        List<String> stockCodeList = Lists.newArrayList(stockTypeMap.values());

        if (!Strings.isNullOrEmpty(req.getStockCode())
                && !stockCodeList.contains(req.getStockCode())) {
            throw new ServiceAccessException(SearchException.UN_SUPPORTED_STOCK_TYPE);
        }

        if (stockCodeList.size() < 1) {
            return null;
        }
        String stockCode;
        if (Strings.isNullOrEmpty(req.getStockCode())) {
            stockCode = stockTypeMap.firstEntry().getValue();
        } else {
            stockCode = req.getStockCode();
        }
        Document doc = enterpriseRepo.getListingInfo(stockCode);
        if (doc == null) {
            return null;
        }

        if (!Strings.isNullOrEmpty(req.getSubType())) {
            DataItem item;
            if (req.getSubType().equals("managers")) {
                List<Map> list = doc.get("manager") == null ? Lists.newArrayList() : (List<Map>) doc.get("manager");
                item = EnterpriseRepoUtil.getFromList(Listing.class, req.getSubType(), enterpriseRepo, list,
                        req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(Listing.class, req.getSubType(), item);
            } else {
                item = EnterpriseRepoUtil.get(Listing.class, req.getSubType(), enterpriseRepo, stockCode,
                        req.getYearQuarter(), req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(Listing.class, req.getSubType(), item);
            }
        } else {
            DataItem financialReport = new DataItem();
            DataItem fundTable = new DataItem();
            DataItem list = new DataItem();
            DataItem managers = new DataItem();
            DataItem topTenSH = new DataItem();
            DataItem topTenTSH = new DataItem();
            DataItem notice = new DataItem();
            DataItem rules = new DataItem();
            DataItem ssgsRegularReport = new DataItem();
            Listing listing = new Listing();

            list.setTotalCount(1L);
            //获取financialReport
            financialReport.setTotalCount(0L);
            FinancialReport report = null;
            if (!Strings.isNullOrEmpty(stockCode)) {
                report = enterpriseRepo.getFinancialReport(stockCode);

                if (report != null) {
                    financialReport.setTotalCount(1L);
                }
            }
            //获取managers
            List<Map> managerList = doc.get("manager") == null ? Lists.newArrayList() : (List<Map>) doc.get("manager");

            //获取ssgs_regular_report
            RegularReport regularReport = enterpriseRepo.getRegularReport(stockCode);
            ssgsRegularReport.setTotalCount(regularReport.getTotalCount());

            if (!req.getOnlyCounting()) {
                //managers trick
                List<Map> managerData = enterpriseRepo.getManagers(managerList, req.getOffset(), req.getCount());

                //获取notice
                List<Map> noticeList = enterpriseRepo.getNotice(stockCode, req.getOffset(), req.getCount());
                //获取rules
                List<Map> ruleList = enterpriseRepo.getRules(stockCode, req.getOffset(), req.getCount());

                financialReport.setData(report);
                managers.setData(managerData);
                notice.setData(noticeList);
                rules.setData(ruleList);

                //获取topTenShareholder
                topTenSH = enterpriseRepo.getTopTenShareholders(stockCode, req.getYearQuarter(), req.getOffset(), req.getCount());
                //获取topTenTradableShareholders
                topTenTSH = enterpriseRepo.getTopTenTradableShareholders(stockCode, req.getYearQuarter(), req.getOffset(), req.getCount());
                //获取fundTable
                fundTable = enterpriseRepo.getFundTable(stockCode, req.getYearQuarter(), req.getOffset(), req.getCount());
                ssgsRegularReport.setData(regularReport);
                doc.remove("manager");
                list.setData(doc);
            }
            managers.setTotalCount(enterpriseRepo.countManagers(managerList));
            notice.setTotalCount(enterpriseRepo.countNotice(stockCode));
            rules.setTotalCount(enterpriseRepo.countRules(stockCode));

            listing.setStockCodeList(Lists.newArrayList(stockTypeMap.values()));
            listing.setListing(list);
            listing.setFinancialReport(financialReport);
            listing.setManagers(managers);
            listing.setNotice(notice);
            listing.setFundTable(fundTable);
            listing.setRules(rules);
            listing.setTopTenShareholders(topTenSH);
            listing.setTopTenTradableShareholders(topTenTSH);
            listing.setFundTable(fundTable);
            listing.setSsgsRegularReport(ssgsRegularReport);

            return listing;
        }
    }

    /**
     * 投资关系的被投资、投资事件、融资事件、并购事件、退出事件会存在分页
     *
     * @param req
     * @return
     */
    @Override
    public Investment invest(EnterpriseReq req) {
        //获取对外投资
        Document basic = enterpriseRepo.getBasic(req.getName());
        if (basic == null) {
            throw new ServiceAccessException(SearchException.UN_CRAW_DATA);
        }
        Investment investment = new Investment();
        AcquirerEvents acquirerEvents = new AcquirerEvents();

//        List<Map> investList = basic.get("invested_companies") == null ?
//                Collections.emptyList() : (List<Map>) basic.get("invested_companies");
        //需求变更-> 从图库中获取
        List<Map> investList = graphRepo.getOuterInvest("Company/" + SecretUtil.md5(req.getName()));

        if (!Strings.isNullOrEmpty(req.getSubType())) {
            if (req.getSubType().equals("acquirer_events")) {
                //获取并购与被并购事件
                List<Map> acquirerEventsList = enterpriseRepo.getAcquirerEvents(req.getName(), req.getOffset(), req.getCount());
                List<Map> acquireredEventsList = enterpriseRepo.getAcquireredEvents(req.getName(), req.getOffset(), req.getCount());
                acquirerEvents.setTotalCount(enterpriseRepo.countAcquirerEvents(req.getName()) + enterpriseRepo.countAcquireredEvents(req.getName()));
                acquirerEvents.setAcquirer(new DataItem(acquirerEventsList, enterpriseRepo.countAcquirerEvents(req.getName())));
                acquirerEvents.setAcquirered(new DataItem(acquireredEventsList, enterpriseRepo.countAcquireredEvents(req.getName())));
                investment.setAcquirerEvents(acquirerEvents);
            } else if (req.getSubType().equals("invest")) {

                DataItem item = EnterpriseRepoUtil.getFromList(Investment.class, req.getSubType(), enterpriseRepo, investList,
                        req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(Investment.class, req.getSubType(), item);
            } else {
                DataItem item = EnterpriseRepoUtil.get(Investment.class, req.getSubType(), enterpriseRepo, req.getName(),
                        req.getYearQuarter(), req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(Investment.class, req.getSubType(), item);
            }
        } else {
            DataItem invest = new DataItem();
            //DataItem beingInvested = new DataItem();
            DataItem investEvents = new DataItem();
            DataItem financialEvents = new DataItem();
            DataItem exitEvents = new DataItem();

            if (!req.getOnlyCounting()) {
                //投资trick
                List<Map> investData = enterpriseRepo.getInvest(investList, req.getOffset(), req.getCount());
                //获取被投资
                /**
                 * 需求：去掉被投资

                 List<Map> beingInvestedList = enterpriseRepo.getBeingInvested(req.getName());
                 */
                //获取投资事件
                List<Map> investEventsList = enterpriseRepo.getInvestEvents(req.getName());

                //获取融资事件
                List<Map> financialEventsList = enterpriseRepo.getFinancialEvents(req.getName());

                //获取并购与被并购事件
                List<Map> acquirerEventsList = enterpriseRepo.getAcquirerEvents(req.getName(), req.getOffset(), req.getCount());
                List<Map> acquireredEventsList = enterpriseRepo.getAcquireredEvents(req.getName(), req.getOffset(), req.getCount());
                //获取退出事件
                List<Map> exitEventsList = enterpriseRepo.getExitEvents(req.getName());

                invest.setData(investData);
                //beingInvested.setData(beingInvestedList);
                investEvents.setData(investEventsList);
                financialEvents.setData(financialEventsList);
                acquirerEvents.setAcquirer(new DataItem(acquirerEventsList, enterpriseRepo.countAcquirerEvents(req.getName())));
                acquirerEvents.setAcquirered(new DataItem(acquireredEventsList, enterpriseRepo.countAcquireredEvents(req.getName())));
                exitEvents.setData(exitEventsList);
            }

            invest.setTotalCount(enterpriseRepo.countInvest(investList));
            //beingInvested.setTotalCount(enterpriseRepo.countBeingInvested(req.getName()));
            investEvents.setTotalCount(enterpriseRepo.countInvestEvents(req.getName()));
            financialEvents.setTotalCount(enterpriseRepo.countFinancialEvents(req.getName()));
            acquirerEvents.setTotalCount(enterpriseRepo.countAcquirerEvents(req.getName()) + enterpriseRepo.countAcquireredEvents(req.getName()));
            exitEvents.setTotalCount(enterpriseRepo.countExitEvents(req.getName()));


            investment.setInvest(invest);
            //investment.setBeingInvested(beingInvested);
            investment.setInvestEvents(investEvents);
            investment.setFinancialEvents(financialEvents);
            investment.setAcquirerEvents(acquirerEvents);
            investment.setExitEvents(exitEvents);
        }
        return investment;
    }

    /**
     * 知识产权中专利信息会存在分页
     *
     * @param req
     * @return
     */
    @Override
    public IntellectualProperty intellectualProperty(EnterpriseReq req) {
        Document doc = enterpriseRepo.getBasic(req.getName());
        if (doc == null) {
            throw new ServiceAccessException(SearchException.UN_CRAW_DATA);
        }

        if (!Strings.isNullOrEmpty(req.getSubType())) {
            if (req.getSubType().equals("trademark") || req.getSubType().equals("copyright")
                    || req.getSubType().equals("softwarecopyright")) {
                List<Map> list = doc.get(req.getSubType()) == null ? Collections.emptyList() : (List<Map>) doc.get(req.getSubType());
                DataItem item = EnterpriseRepoUtil.getFromList(IntellectualProperty.class, req.getSubType(), enterpriseRepo, list,
                        req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(IntellectualProperty.class, req.getSubType(), item);
            } else {
                DataItem item = EnterpriseRepoUtil.get(IntellectualProperty.class, req.getSubType(), enterpriseRepo,
                        req.getName(), req.getYearQuarter(), req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(IntellectualProperty.class, req.getSubType(), item);
            }
        } else {
            List<Map> trademarkList = doc.get("trademark") == null ?
                    Collections.emptyList() : (List<Map>) doc.get("trademark");
            List<Map> copyrightList = doc.get("copyright") == null ?
                    Collections.emptyList() : (List<Map>) doc.get("copyright");
            List<Map> softwareCopyrightList = doc.get("softwarecopyright") == null ?
                    Collections.emptyList() : (List<Map>) doc.get("softwarecopyright");

            DataItem trademark = new DataItem();
            DataItem patent = new DataItem();
            DataItem copyright = new DataItem();
            DataItem softwareCopyright = new DataItem();

            if (!req.getOnlyCounting()) {
                List<Map> patentList = enterpriseRepo.getPatent(req.getName(), req.getOffset(), req.getCount());

                trademark.setData(enterpriseRepo.getTrademark(trademarkList, req.getOffset(), req.getCount()));
                patent.setData(patentList);
                copyright.setData(enterpriseRepo.getCopyright(copyrightList, req.getOffset(), req.getCount()));
                softwareCopyright.setData(enterpriseRepo.getSoftwareCopyright(softwareCopyrightList, req.getOffset(), req.getCount()));
            }
            trademark.setTotalCount(enterpriseRepo.countTrademark(trademarkList));
            patent.setTotalCount(enterpriseRepo.countPatent(req.getName()));
            copyright.setTotalCount(enterpriseRepo.countCopyright(copyrightList));
            softwareCopyright.setTotalCount(enterpriseRepo.countSoftwareCopyright(softwareCopyrightList));

            IntellectualProperty ip = new IntellectualProperty();
            ip.setTrademark(trademark);
            ip.setPatent(patent);
            ip.setCopyright(copyright);
            ip.setSoftwareCopyright(softwareCopyright);
            return ip;
        }
    }

    /**
     * 招投标信息中招标信息、中标信息、土地招拍挂都存在分页
     *
     * @param req
     * @return
     */
    @Override
    public Bidding bidding(EnterpriseReq req) {
        if (!Strings.isNullOrEmpty(req.getSubType())) {
            DataItem item = EnterpriseRepoUtil.get(Bidding.class, req.getSubType(), enterpriseRepo,
                    req.getName(), req.getYearQuarter(), req.getOffset(), req.getCount());
            return EnterpriseRepoUtil.set(Bidding.class, req.getSubType(), item);
        } else {
            DataItem bidInfo = new DataItem();
            DataItem winBid = new DataItem();
            DataItem landAuction = new DataItem();

            if (!req.getOnlyCounting()) {
                List<Map> bidList = enterpriseRepo.getBidInfo(req.getName(), req.getOffset(), req.getCount());
                List<Map> winBidList = enterpriseRepo.getWinInfo(req.getName(), req.getOffset(), req.getCount());
                List<Map> landAuctionList = enterpriseRepo.getLandAuction(req.getName(), req.getOffset(), req.getCount());

                bidInfo.setData(bidList);
                winBid.setData(winBidList);
                landAuction.setData(landAuctionList);
            }
            bidInfo.setTotalCount(enterpriseRepo.countBidInfo(req.getName()));
            winBid.setTotalCount(enterpriseRepo.countWinInfo(req.getName()));
            landAuction.setTotalCount(enterpriseRepo.countLandAuction(req.getName()));

            Bidding bidding = new Bidding();
            bidding.setBidInfo(bidInfo);
            bidding.setWinInfo(winBid);
            bidding.setLandAuction(landAuction);
            return bidding;
        }
    }

    /**
     * 风险信息中失信人信息、被执行人信息、法院判决、开庭公告、法院公告、审判流程、行政处罚、欠税公告分页
     * TODO A级纳税年份
     *
     * @param req
     * @return
     */
    @Override
    public Risk risk(EnterpriseReq req) {
        Document customsInfo = enterpriseRepo.getCustomsInformation(req.getName());
        List<Map> allCustomsPenaltyList = customsInfo == null || customsInfo.get("penalty_list") == null ?
                Collections.EMPTY_LIST : (List<Map>) customsInfo.get("penalty_list");

        if (!Strings.isNullOrEmpty(req.getSubType())) {
            if (req.getSubType().equals("customs_penalty")) {
                DataItem item = EnterpriseRepoUtil.getFromList(Risk.class, req.getSubType(), enterpriseRepo, allCustomsPenaltyList,
                        req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(Risk.class, req.getSubType(), item);
            } else {
                DataItem item = EnterpriseRepoUtil.get(Risk.class, req.getSubType(), enterpriseRepo,
                        req.getName(), req.getYearQuarter(), req.getOffset(), req.getCount());
                return EnterpriseRepoUtil.set(Risk.class, req.getSubType(), item);
            }
        } else {
            DataItem courtSessionAnn = new DataItem();
            DataItem courtAnn = new DataItem();
//            DataItem taxRank = new DataItem();
            DataItem judgeProcess = new DataItem();
            DataItem judgement = new DataItem();
            DataItem owingTax = new DataItem();
            DataItem govPenalty = new DataItem();
            DataItem dishonestInfo = new DataItem();
            DataItem executionInfo = new DataItem();
            DataItem customsPenalty = new DataItem();
            DataItem environmentProtection = new DataItem();

            Map<Integer, Map<String, Object>> environmentProtectionData = enterpriseRepo.getEnvironmentProtection(req.getName());

            if (!req.getOnlyCounting()) {
                List<Map> courtSessionAnnList = enterpriseRepo.getCourtKtgg(req.getName(), req.getOffset(), req.getCount());
                List<Map> courtAnnList = enterpriseRepo.getCourtFygg(req.getName(), req.getOffset(), req.getCount());
//                List<Map> taxList = enterpriseRepo.getTaxRank(req.getName(), req.getOffset(), req.getCount());
                List<Map> judgeProcessList = enterpriseRepo.getJudgeProcess(req.getName(), req.getOffset(), req.getCount());
                List<Map> judgementList = enterpriseRepo.getJudgement(req.getName(), req.getOffset(), req.getCount());
                List<Map> owingTaxList = enterpriseRepo.getOwingTax(req.getName(), req.getOffset(), req.getCount());
                List<Map> penaltyList = enterpriseRepo.getPenalty(req.getName(), req.getOffset(), req.getCount());
                List<Map> dihonestList = enterpriseRepo.getShixinInfo(req.getName(), req.getOffset(), req.getCount());
                List<Map> executionList = enterpriseRepo.getZhixingInfo(req.getName(), req.getOffset(), req.getCount());
                List<Map> customsPenaltyList = enterpriseRepo.getCustomsPenalty(allCustomsPenaltyList, req.getOffset(), req.getCount());

                courtSessionAnn.setData(courtSessionAnnList);
                courtAnn.setData(courtAnnList);
//                taxRank.setData(taxList);
                judgeProcess.setData(judgeProcessList);
                judgement.setData(judgementList);
                owingTax.setData(owingTaxList);
                govPenalty.setData(penaltyList);
                dishonestInfo.setData(dihonestList);
                executionInfo.setData(executionList);
                customsPenalty.setData(customsPenaltyList);
                environmentProtection.setData(environmentProtectionData);
            }
            courtSessionAnn.setTotalCount(enterpriseRepo.countCourtKtgg(req.getName()));
            courtAnn.setTotalCount(enterpriseRepo.countCourtFygg(req.getName()));
//            taxRank.setTotalCount(enterpriseRepo.countTaxRank(req.getName()));
            judgeProcess.setTotalCount(enterpriseRepo.countJudgeProcess(req.getName()));
            judgement.setTotalCount(enterpriseRepo.countJudgement(req.getName()));
            owingTax.setTotalCount(enterpriseRepo.countOwingTax(req.getName()));
            govPenalty.setTotalCount(enterpriseRepo.countPenalty(req.getName()));
            dishonestInfo.setTotalCount(enterpriseRepo.countShixinInfo(req.getName()));
            executionInfo.setTotalCount(enterpriseRepo.countZhixingInfo(req.getName()));
            customsPenalty.setTotalCount(enterpriseRepo.countCustomsPenalty(allCustomsPenaltyList));
            environmentProtection.setTotalCount(
                    environmentProtectionData != null && environmentProtectionData.size() > 0 ? 1L : 0);

            Risk risk = new Risk();
            risk.setCourtSessionAnn(courtSessionAnn);
            risk.setCourtAnn(courtAnn);
//            risk.setTaxRank(taxRank);
            risk.setJudgeProcess(judgeProcess);
            risk.setJudgement(judgement);
            risk.setOwingTax(owingTax);
            risk.setGovPenalty(govPenalty);
            risk.setDishonestInfo(dishonestInfo);
            risk.setExecutionInfo(executionInfo);
            risk.setCustomsPenalty(customsPenalty);
            risk.setEnvironmentProtection(environmentProtection);
            return risk;
        }
    }

    /**
     * 舆情信息中新闻存在分页
     *
     * @param req
     * @return
     */
    @Override
    public Sentiment publicSentiment(EnterpriseReq req) {

        Sentiment sentiment = new Sentiment();

        if (!Strings.isNullOrEmpty(req.getSubType())) {
            if (req.getSubType().equals("news")) {
                SentimentDataItem news = enterpriseRepo.getSentiment(req.getName(), "keyword", req.getThirdType(), "publish_time",
                        SentimentType.NEWS, req.getOnlyCounting(), req.getOffset(), req.getCount());
                sentiment.setNews(news);
            }
//            else if (req.getSubType().equals("hiring")) {
//                DataItem hiring = new DataItem();
//                List<Map> hiringList = enterpriseRepo.getHiringInfo(req.getName(), req.getOffset(), req.getCount());
//                hiring.setData(hiringList);
//                hiring.setTotalCount(enterpriseRepo.countHiringInfo(req.getName()));
//                sentiment.setHiring(hiring);
//            }
        } else {
            SentimentDataItem news = enterpriseRepo.getSentiment(req.getName(), "keyword", req.getThirdType(), "publish_time",
                    SentimentType.NEWS, req.getOnlyCounting(), req.getOffset(), req.getCount());
            sentiment.setNews(news);

//            DataItem hiring = new DataItem();
//            if (!req.getOnlyCount()) {
//                List<Map> hiringList = enterpriseRepo.getHiringInfo(req.getName(), req.getOffset(), req.getCount());
//                hiring.setData(hiringList);
//            }
//            hiring.setTotalCount(enterpriseRepo.countHiringInfo(req.getName()));
//            sentiment.setHiring(hiring);
        }
        return sentiment;
    }

    /**
     * 投资机构
     *
     * @param req
     * @return
     */
    @Override
    public InvestInstitution investInstitution(EnterpriseReq req) {
        Document doc = enterpriseRepo.getInvestInstitution(req.getName());
        if (doc == null) {
            return null;
        }

        InvestInstitution investInstitution = new InvestInstitution();
        if (!req.getOnlyCounting()) {
            Contact contact = new Contact(doc.getString("region"), doc.getString("phone"), doc.getString("legal_man"), doc.getString("fax"), doc.getString("email"));
            investInstitution.setBasic(new DataItem(doc, 1l));
            investInstitution.setContact(new DataItem(contact, 1l));
        } else {
            investInstitution.setBasic(new DataItem(null, 1l));
            investInstitution.setContact(new DataItem(null, 1l));
        }

        return investInstitution;
    }

    @Override
    public List<Map<String, Object>> getGraphConcert(String companyName) {
        return detailGraphRepo.getGraphConcert(companyName);
    }

    @Override
    public List<Map<String, Object>> getGraphKeyPerson(String companyName) {
        return detailGraphRepo.getGraphKeyPerson(companyName);
    }

    @Override
    public List<Map> getGraphContributor(String companyName) {
        return detailGraphRepo.getGraphContributor(companyName);
    }

    @Override
    public List<DynamicInfo> getDynamicInfo(String companyName, String type) {
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
        switch (type) {
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

    @Override
    public Pair<String, String> getFaction(String company) {
        Map<String, Object> companyEntity = graphRepo.getCompanyByName(company);
        String faction = "";
        String id = "";
        if (companyEntity != null && companyEntity.get("_id") != null) {
            List<Map<String, Object>> groups = graphRepo.getGroupOfCompanyById(companyEntity.get("_id").toString());
            //一个企业只属于一个系
            if (groups != null && groups.size() > 0 && groups.get(0).get("center") != null) {
                faction = groups.get(0).get("center").toString() + "•系";
                if (groups.get(0).get("center_company_id") != null) {
                    id = groups.get(0).get("center_company_id").toString();
                }
            }
        }
        return Pair.of(id, faction);
    }

    @Override
    public List<Map> getTaxRank(String company) {
        return enterpriseRepo.getTaxRank(company);
    }

    @Override
    public Boolean isStored(String company) {
        Map basic = enterpriseRepo.getBasic(company);
        return basic != null;
    }

    @Override
    public Map<String, Double> getExchangeRateMap() {
        return exchangeRateRepo.getAllExchangeRateMap();
    }


    private List<Map<String, Object>> extractKeyPerson(List<Map> keyPersonList) {
        List<Map<String, Object>> result = Lists.newArrayList();
        for (Map doc : keyPersonList) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("name", doc.get("key_person_name"));
            map.put("job", doc.get("key_person_position"));
            result.add(map);
        }
        return result;
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

    //查arango的版本
//    private DataItem getShareholderInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
//        DataItem shareholderInfo = new DataItem();
//        List<Map> holderList = detailGraphRepo.getInvest(companyName);
//        if (onlyCount != null && !onlyCount) {
//            shareholderInfo.setData(PageUtil.pageList(holderList, offset, count));
//        }
//        shareholderInfo.setTotalCount((long) holderList.size());
//        return shareholderInfo;
//    }

    //查arango的版本
    private List<Map> getShareholderInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        List<Map> holderList = detailGraphRepo.getInvest(companyName);
        if (onlyCount != null && !onlyCount) {
            holderList = PageUtil.pageList(holderList, offset, count);
        }
        String amountUnit = "";
        Double amountValue;
        String amountInfo = "";
        for (Map<String, Object> map:holderList) {
            if (null != map.get("invest_amount")){
                if (null != map.get("invest_amount_unit")){
                    amountUnit = map.get("invest_amount_unit").toString();
                }
                amountValue = NumberUtil.tryParseDouble(map.get("invest_amount").toString());
                amountInfo = String.format("%.2f%s%s", amountValue / 10000, "万",amountUnit);
                map.put("invest_amount",amountInfo);
            }
            if (null != map.get("paied_amount")){
                if (null != map.get("paied_amount_unit")){
                    amountUnit = map.get("paied_amount_unit").toString();
                }
                amountValue = NumberUtil.tryParseDouble(map.get("paied_amount").toString());
                amountInfo = String.format("%.2f%s%s", amountValue / 10000, "万",amountUnit);
                map.put("paied_amount",amountInfo);
            }
        }
        return holderList;
    }

    @Override
    public List<Map<String, Object>> briefBatch(List<String> companyNames) {
        if (CollectionUtils.isEmpty(companyNames)){
            return Collections.emptyList();
        }
        List<Document> basicOfCompanies = enterpriseRepo.getBasicOfCompanies(companyNames);
        if (!CollectionUtils.isEmpty(basicOfCompanies)) {
            List<Map<String, Object>> result = basicOfCompanies.stream().collect(Collectors.toList());
            return result;
        }
        return Collections.emptyList();
    }
}
