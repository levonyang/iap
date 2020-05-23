package com.haizhi.iap.search.repo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.utils.LogicDeleteUtil;
import com.haizhi.iap.search.conf.AppDataCollections;
import com.haizhi.iap.search.conf.ListingConf;
import com.haizhi.iap.search.conf.PublicSectorConf;
import com.haizhi.iap.search.controller.model.*;
import com.haizhi.iap.search.enums.FinancialReportField;
import com.haizhi.iap.search.enums.SentimentType;
import com.haizhi.iap.search.utils.CompanyUtil;
import com.haizhi.iap.search.utils.NumberUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import lombok.Setter;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by chenbo on 17/2/15.
 */
@Repository
@Slf4j
public class EnterpriseRepo {

    @Qualifier("appMongoDatabase")
    @Autowired
    MongoDatabase appMongoDatabase;

//    @Setter
//    @Inject(ref = "appMongoDatabase")
//    MongoDatabase appMongoDatabase;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    private Map<String, String> sectorConfMap = PublicSectorConf.getSectorConfMap();

    private final static Integer DEFAULT_PAGE_SIZE = 10;

    public Document findByCollAndId(String coll, String recordId) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(coll);
        MongoCursor<Document> cursor = collection.find(new BasicDBObject("_record_id", recordId))
                .projection(new BasicDBObject("_id", 0)).iterator();
        if (cursor.hasNext()) {
            return cursor.next();
        } else {
            return null;
        }
    }

    public Document getBasic(String companyName) {
        Document doc = redisRepo.getBasic(companyName);
        if (doc != null) {
            return doc;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);

        BasicDBObject filter = new BasicDBObject();
        filter.put("company", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).projection(new BasicDBObject("source_url", 0)).iterator();

        if (cursor.hasNext()) {
            doc = cursor.next();
            redisRepo.pushBasic(doc, companyName);
            return doc;
        } else {
            return null;
        }
    }

    public List<Document> getBasicOfCompanies(List<String> companies) {
    	List<Document> documents = new ArrayList<Document>();
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        BasicDBList values = new BasicDBList();
        for (String company:companies) {
			values.add(company);
		}
        BasicDBObject in = new BasicDBObject("$in", values);
        BasicDBObject filter = new BasicDBObject();
        filter.put("company", in);
        LogicDeleteUtil.addDeleteFilter(filter);
        MongoCursor<Document> cursor = collection.find(filter).projection(BasicDBObject.parse("{\"registered_date\": 1,\"_record_id\": 1,\"enterprise_type\": 1,\"city\": 1,\"registered_capital\": 1,\"_in_time\": 1,\"business_status\": 1,\"company\": 1,\"shareholder_information\": 1,\"province\": 1,\"hezhun_date\": 1,\"business_scope\":1,\"registered_address\": 1,\"industry\": 1,\"legal_man\": 1,\"_utime\": 1,\"registered_code\": 1,\"stock_code\": 1,\"_id\": 0}")).iterator();

        Document doc = new Document();
        
        try {
        	while (cursor.hasNext()) {
                doc = cursor.next();
                documents.add(doc);
            }
		}finally {
			cursor.close();
		}
        log.info("documents:{}", documents);
        return documents;
    }
    
    public List<Map> getBranch(List<Map> branchList, Integer offset, Integer count) {
        List<Map> result = pageList(branchList, offset, count);
        List<Map> data = Lists.newArrayList();
        for (Map doc : result) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("name", doc.get("compay_name"));
            data.add(map);
        }
        return data;
    }

    public Long countBranch(List<Map> branchList) {
        return Long.valueOf(branchList.size());
    }

    public List<Map> getChangeRecords(List<Map> changeRecordList, Integer offset, Integer count) {
        changeRecordList.sort((branch1, branch2) -> {
            if (branch1 == null || branch2 == null || branch2.get("change_date") == null || branch1.get("change_date") == null) {
                return 1;
            } else {
                return branch2.get("change_date").toString().compareTo(branch1.get("change_date").toString());
            }
        });
        return pageList(changeRecordList, offset, count);
    }

    public Long countChangeRecords(List<Map> changeRecordList) {
        return Long.valueOf(changeRecordList.size());
    }

    public List<Map> getShareholderInformation(List<Map> holderList, Object registeredCapital, Integer offset, Integer count) {
        if (registeredCapital == null) {
            return Collections.EMPTY_LIST;
        }
        Double total = NumberUtil.tryParseDouble(registeredCapital.toString());
        if (total <= 0) {
            return Collections.EMPTY_LIST;
        }
        List<Map> result = pageList(holderList, offset, count);
        Double part;
        for (Map map : result) {
            //算持股比例
            if (map.get("subscription_amount") != null && !total.equals(0d)) {
                part = NumberUtil.tryParseDouble(map.get("subscription_amount").toString());
                double percent = part / total;
                BigDecimal decimal = new BigDecimal(percent * 100);
                percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                map.put("shareholding_ratio", String.format("%.2f", percent) + "%");
            } else {
                map.put("shareholding_ratio", "——");
            }
        }
        return result;
    }

    public List<Map> getContributorInformation(List<Map> contributorList, Object registeredCapital, Integer offset, Integer count) {
        if (registeredCapital == null) {
            return Collections.EMPTY_LIST;
        }
        Double total = NumberUtil.tryParseDouble(registeredCapital.toString());
        if (total <= 0) {
            return Collections.EMPTY_LIST;
        }
        List<Map> result = pageList(contributorList, offset, count);
        Double part;
        for (Map map : result) {
            //算持股比例
            if (map.get("subscription_amount") != null && !total.equals(0d)) {
                part = NumberUtil.tryParseDouble(map.get("subscription_amount").toString());
                double percent = part / total;
                BigDecimal decimal = new BigDecimal(percent * 100);
                percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                map.put("shareholding_ratio", String.format("%.2f", percent) + "%");
            } else {
                map.put("shareholding_ratio", "——");
            }
        }
        return result;
    }

    public Long countShareholderInformation(List<Map> holderList) {
        return Long.valueOf(holderList.size());
    }

    public Long countContributorInformation(List<Map> contributorList) {
        return Long.valueOf(contributorList.size());
    }

    public List<Map<String, Object>> getAnnualReport(String name) {
        return getAnnualReport(name, null, null);
    }

    public List<Map<String, Object>> getAnnualReport(String name, Integer offset, Integer count) {
        List<Map<String, Object>> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ANNUAL_REPORT);

        BasicDBObject filter = new BasicDBObject();
        filter.put("company", name);
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            result.add(doc);
        }

        return result;
    }

    /**
     * @author thomas
     * 根据AnnualReportQuery参数批量查询mongo，得到AnnualReport信息
     *
     * @param query
     * @return
     */
    public Map<String, List<Map<String, Object>>> getAnnualReport(AnnualReportQuery query) {
        Map<String, List<Map<String, Object>>> resultMap = Maps.newHashMap();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ANNUAL_REPORT);
        query.getNames().forEach(name -> {
            Bson filter = Filters.and(Filters.eq("company", name), Filters.ne("logic_delete", 1));
            FindIterable<Document> findIterable = collection.find(filter);
            if(query.getOffset() != null && query.getSize() != null)
                findIterable.skip(query.getOffset()).limit(query.getSize());
            if(query.getSort() != null)
            {
                switch (query.getSort().getOrder())
                {
                    case ASC:
                        findIterable.sort(Sorts.ascending(query.getSort().getField()));
                        break;
                    case DESC:
                        findIterable.sort(Sorts.descending(query.getSort().getField()));
                        break;
                    default:break;
                }
            }
            if(!CollectionUtils.isEmpty(query.getFields()))
                findIterable.projection(Projections.fields(Projections.include(query.getFields()), Projections.excludeId()));

            List<Map<String, Object>> result = Lists.newArrayList();
            for (Document doc : findIterable)
                result.add(doc);
            resultMap.put(name, result);
        });
        return resultMap;
    }

    public Document getListingInfo(String stockCode) {
        Document document = redisRepo.getListingInfo(stockCode);
        if (document != null) {
            return document;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_SSGS_BASEINFO);
        BasicDBObject filter = new BasicDBObject("stock_code", stockCode);
//        if (stockType != null) {
//            filter.put("stock_type", stockType);
//        }
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter)
                .projection(new BasicDBObject("_id", 0)).iterator();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            Map<String, String> confMap = ListingConf.getSsgsBaseInfoMap();
            if (doc.get("brief") != null) {
                Map<String, Object> brief = (Map<String, Object>) doc.get("brief");
                brief.keySet().stream().filter(key -> confMap.keySet().contains(key)).forEach(key -> {
                    doc.put(confMap.get(key), brief.get(key));
                });
            }

            if (doc.get("latest") != null) {
                Map<String, Object> latest = (Map<String, Object>) doc.get("latest");
                latest.keySet().stream().filter(key -> confMap.keySet().contains(key)).forEach(key -> {
                    doc.put(confMap.get(key), latest.get(key));
                });
            }
            doc.remove("brief");
            doc.remove("latest");

            redisRepo.pushListingInfo(doc, stockCode);
            return doc;
        } else {
            return null;
        }
    }
    
    
    public Document getListingInfoByCompany(String company) {

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_SSGS_BASEINFO);
        BasicDBObject filter = new BasicDBObject("company_full_name", company);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter)
                .projection(new BasicDBObject("_id", 0)).iterator();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            Map<String, String> confMap = ListingConf.getSsgsBaseInfoMap();
            if (doc.get("brief") != null) {
                Map<String, Object> brief = (Map<String, Object>) doc.get("brief");
                brief.keySet().stream().filter(key -> confMap.keySet().contains(key)).forEach(key -> {
                    doc.put(confMap.get(key), brief.get(key));
                });
            }

            if (doc.get("latest") != null) {
                Map<String, Object> latest = (Map<String, Object>) doc.get("latest");
                latest.keySet().stream().filter(key -> confMap.keySet().contains(key)).forEach(key -> {
                    doc.put(confMap.get(key), latest.get(key));
                });
            }
            doc.remove("brief");
            doc.remove("latest");
            return doc;
        } else {
            return null;
        }
    }

    public DataItem getTopTenShareholders(String stockCode, String yearQuarter, Integer offset, Integer count) {
        Document doc = getTopTenSH(stockCode);
        if (doc != null && doc.get("top10_shareholders") != null
                && doc.get("top10_shareholders") instanceof List) {
            List<Map> topTen = (List<Map>) doc.get("top10_shareholders");
            List<String> tabList = Lists.newArrayList();
            List<Map> topTenFiltered = Lists.newArrayList();
            for (Map<String, Object> map : topTen) {
                if (!tabList.contains(String.valueOf(map.get("year_quarter")))) {
                    tabList.add(String.valueOf(map.get("year_quarter")));
                }
                if (!Strings.isNullOrEmpty(yearQuarter) && yearQuarter.equals(map.get("year_quarter"))) {
                    topTenFiltered.add(map);
                }
            }
            if (Strings.isNullOrEmpty(yearQuarter)) {
                topTenFiltered = topTen;
            }
            List<Map> data = pageList(topTenFiltered, offset, count);
            return new DataItem(data, tabList, (long) topTenFiltered.size(), (long) topTen.size());
        }

        return null;
    }

    public DataItem getTopTenTradableShareholders(String stockCode, String yearQuarter, Integer offset, Integer count) {
        Document doc = getTopTenSH(stockCode);
        if (doc != null && doc.get("ten_outstanding_shares") != null
                && doc.get("ten_outstanding_shares") instanceof List) {
            List<Map> topTen = (List<Map>) doc.get("ten_outstanding_shares");
            List<String> tabList = Lists.newArrayList();
            List<Map> topTenFiltered = Lists.newArrayList();
            for (Map<String, Object> map : topTen) {
                if (!tabList.contains(String.valueOf(map.get("year_quarter")))) {
                    tabList.add(String.valueOf(map.get("year_quarter")));
                }
                if (!Strings.isNullOrEmpty(yearQuarter) && yearQuarter.equals(map.get("year_quarter"))) {
                    topTenFiltered.add(map);
                }
            }
            if (Strings.isNullOrEmpty(yearQuarter)) {
                topTenFiltered = topTen;
            }
            List<Map> data = pageList(topTenFiltered, offset, count);
            return new DataItem(data, tabList, (long) topTenFiltered.size(), (long) topTen.size());
        }

        return null;
    }

    public DataItem getFundTable(String stockCode, String yearQuarter, Integer offset, Integer count) {
        Document doc = getTopTenSH(stockCode);
        if (doc != null && doc.get("fund_table") != null
                && doc.get("fund_table") instanceof List) {
            List<Map> fundTableList = (List<Map>) doc.get("fund_table");
            List<String> tabList = Lists.newArrayList();
            List<Map> filtered = Lists.newArrayList();
            Map<String, List<Map>> sortMap = Maps.newTreeMap();
            for (Map map : fundTableList) {
                List<Map> list;
                if (sortMap.get(String.valueOf(map.get("year_quarter"))) != null) {
                    list = sortMap.get(String.valueOf(map.get("year_quarter")));
                } else {
                    list = Lists.newArrayList();
                }
                list.add(map);
                sortMap.put(String.valueOf(map.get("year_quarter")), list);
            }
            tabList.addAll(sortMap.keySet());
            Collections.reverse(tabList);
            if (Strings.isNullOrEmpty(yearQuarter)) {
                if (tabList.size() > 0) {
                    filtered = sortMap.get(tabList.get(0));
                }
            } else {
                filtered = sortMap.get(yearQuarter);
            }
            List<Map> data = pageList(filtered, offset, count);

            return new DataItem(data, tabList, (long) filtered.size(), (long) fundTableList.size());
        }

        return null;
    }

    public Document getTopTenSH(String stockCode) {
        Document result = redisRepo.getTopTenSH(stockCode);
        if (result != null) {
            return result;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_TOP_TEN_SH);
        BasicDBObject filter = new BasicDBObject("stock_code", stockCode);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter)
                .projection(new BasicDBObject("_id", 0)).iterator();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            redisRepo.pushTopTenSH(doc, stockCode);
            return doc;
        } else {
            return null;
        }
    }

    public List<Map> getRules(String stockCode) {
        return getRules(stockCode, 0, 10);
    }

    public List<Map> getRules(String stockCode, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_RULES);
        BasicDBObject filter = new BasicDBObject("code", stockCode);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject projection = new BasicDBObject();
        projection.put("_in_time", 0);
        projection.put("_src", 0);
        projection.put("_record_id", 0);

        FindIterable<Document> find = collection.find(filter)
                .sort(new BasicDBObject("publish_time", -1))
                .projection(projection);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countRules(String stockCode) {
        Long result = redisRepo.getRulesCount(stockCode);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_RULES);
        BasicDBObject filter = new BasicDBObject("code", stockCode);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushRulesCount(result, stockCode);
        return result;
    }

    public List<Map> getNotice(String stockCode) {
        return getNotice(stockCode, null, null);
    }

    public List<Map> getNotice(String stockCode, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_SSGS_NOTICE_CNINFO);
        BasicDBObject filter = new BasicDBObject("code", stockCode);
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter)
                .sort(new BasicDBObject("publish_time", -1));
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            if (doc.get("publish_time") != null) {
                String time = doc.get("publish_time").toString();
                if (time.endsWith("00:00:00")) {
                    doc.put("publish_time", time.substring(0, time.indexOf("00:00:00") - 1));
                }
            }
            result.add(doc);
        }

        return result;
    }

    public Long countNotice(String stockCode) {
        Long result = redisRepo.getNoticeCount(stockCode);
        if (result != null && result > 0) {
            return result;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_SSGS_NOTICE_CNINFO);
        BasicDBObject filter = new BasicDBObject("code", stockCode);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushNoticeCount(result, stockCode);
        return result;
    }

    public RegularReport getRegularReport(String stockCode) {
        RegularReport regularReport = redisRepo.getRegularReport(stockCode);
        if (regularReport != null) {
            return regularReport;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_REGULAR_REPORT);
        BasicDBObject filter = new BasicDBObject("code", stockCode);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject projection = new BasicDBObject();
        projection.put("_in_time", 0);
        projection.put("_src", 0);
        projection.put("_record_id", 0);
        MongoCursor<Document> cursor = collection.find(filter)
                .sort(new BasicDBObject("publish_time", -1))
                .projection(projection)
                .iterator();

        regularReport = new RegularReport();
        regularReport.setFirst(Lists.newArrayList());
        regularReport.setSecond(Lists.newArrayList());
        regularReport.setThird(Lists.newArrayList());
        regularReport.setFourth(Lists.newArrayList());
        Integer count = 0;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String title = doc.getString("title");
            if (!Strings.isNullOrEmpty(title)) {
                if (title.contains("一季度")) {
                    regularReport.getFirst().add(doc);
                    count += 1;
                } else if (title.contains("半年度")) {
                    regularReport.getSecond().add(doc);
                    count += 1;
                } else if (title.contains("三季度")) {
                    regularReport.getThird().add(doc);
                    count += 1;
                } else if (title.contains("年度")) {
                    regularReport.getFourth().add(doc);
                    count += 1;
                }
            }
        }
        regularReport.setTotalCount(Long.valueOf(count.toString()));

        redisRepo.pushRegularReport(regularReport, stockCode);
        return regularReport;
    }

    public List<Map> getManagers(List<Map> managerList, Integer offset, Integer count) {
        return pageList(managerList, offset, count);
    }

    public Long countManagers(List<Map> managerList) {
        return Long.valueOf(managerList.size());
    }

    public FinancialReport getFinancialReport(String stockCode) {
        FinancialReport report = redisRepo.getFinancialReport(stockCode);
        if (report != null) {
            return report;
        }

        Map<Integer, Map<String, Object>> companyAbility = getReport(stockCode, FinancialReportField.COMPANY_ABILITY);
        Map<Integer, Map<String, Object>> cashFlow = getReport(stockCode, FinancialReportField.CASH_FLOW);
        Map<Integer, Map<String, Object>> profit = getReport(stockCode, FinancialReportField.PROFIT);
        Map<Integer, Map<String, Object>> assetsLiability = getReport(stockCode, FinancialReportField.ASSETS_LIABILITY);

        report = new FinancialReport();
        report.setCompany_ability(companyAbility);
        report.setCashFlow(cashFlow);
        report.setProfit(profit);
        report.setAssetsLiability(assetsLiability);

        redisRepo.pushFinancialReport(report, stockCode);
        return report;
    }

    private Map<Integer, Map<String, Object>> getReport(String stockCode, FinancialReportField field) {
        String coll = null;

        switch (field) {
            case COMPANY_ABILITY:
                coll = AppDataCollections.COLL_COMPANY_ABILITY;
                break;
            case CASH_FLOW:
                coll = AppDataCollections.COLL_CASH_FLOW;
                break;
            case PROFIT:
                coll = AppDataCollections.COLL_PROFIT;
                break;
            case ASSETS_LIABILITY:
                coll = AppDataCollections.COLL_ASSETS_LIABILITY;
                break;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(coll);
        BasicDBObject filter = new BasicDBObject("code", stockCode);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter)
                .sort(new BasicDBObject("publish_time", 1)).iterator();
        if (!cursor.hasNext()) {
            return null;
        }
        Map<Integer, Map<String, Object>> result = Maps.newHashMap();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String year = doc.getString("year_month");
            if (year == null) {
                continue;
            } else {
                //抹平数据源中文key对前端的影响
                Map<String, String> caiBaoConfMap = ListingConf.getCaiBaoMap();
                trickCaiBaoDoc(doc, caiBaoConfMap);
                if (year.length() > 4) {
                    Integer yearNumber = Integer.valueOf(year.substring(0, 4));
                    String reportType = doc.getString("caibao_type");
                    if (reportType != null && reportTypeMap.get(reportType) != null) {
                        Map<String, Object> map = result.get(yearNumber) == null ?
                                Maps.newHashMap() : result.get(yearNumber);
                        map.put(reportTypeMap.get(reportType), doc);
                        result.put(yearNumber, map);
                    }
                }
            }
        }
        return result;
    }

    public Document getFinancialReportBasic(String companyName) {
        Document doc = redisRepo.getFinancialReportBasic(companyName);
        if (doc != null) {
            return doc;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_SSGS_BASEINFO);
        BasicDBObject filter = new BasicDBObject();
        filter.put("company_full_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter)
                .projection(new BasicDBObject("_id", 0)).iterator();

        if (cursor.hasNext()) {
            doc = cursor.next();
            redisRepo.pushFinancialReportBasic(doc, companyName);
        } else {
            return null;
        }
        return doc;
    }

    public List<Map> getInvest(List<Map> investList, Integer offset, Integer count) {
        return pageList(investList, offset, count);
    }

    public Long countInvest(List<Map> investList) {
        return Long.valueOf(investList.size());
    }

    public List<Map> getBeingInvested(String companyName) {
        return getBeingInvested(companyName, null, null);
    }

    public List<Map> getBeingInvested(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        BasicDBObject filter = new BasicDBObject("invested_companies.name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject projection = new BasicDBObject();
        projection.put("_id", 1);
        projection.put("name", 1);
        projection.put("company", 1);

        FindIterable<Document> find = collection.find(filter)
                .projection(projection);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    public Long countBeingInvested(String companyName) {
        Long result = redisRepo.getBeingInvestedCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        BasicDBObject filter = new BasicDBObject("invested_companies.name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushBeingInvestedCount(result, companyName);
        return result;
    }

    public List<Map> getInvestEvents(String companyName) {
        return getInvestEvents(companyName, null, null);
    }

    public List<Map> getInvestEvents(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_INVESTMENT_EVENTS);
        BasicDBObject filter = new BasicDBObject("push_full_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject projection = new BasicDBObject();
        projection.put("_id", 0);
        projection.put("_src", 0);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    public Long countInvestEvents(String companyName) {
        Long result = redisRepo.getInvestEventsCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_INVESTMENT_EVENTS);
        BasicDBObject filter = new BasicDBObject("push_full_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushInvestEventsCount(result, companyName);
        return result;
    }

    /*******
     * 融资事件
     *******/
    public List<Map> getFinancialEvents(String companyName) {
        return getFinancialEvents(companyName, null, null);
    }

    public List<Map> getFinancialEvents(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_FINANCING_EVENTS);
        BasicDBObject projection = new BasicDBObject();
        projection.put("_id", 0);
        projection.put("_src", 0);

        BasicDBObject filter = new BasicDBObject();
        filter.put("enterprise_full_name", companyName);
        filter.put("source_site", new BasicDBObject("$ne", "因果树"));
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    public Long countFinancialEvents(String companyName) {
        Long result = redisRepo.getFinancialEventsCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_FINANCING_EVENTS);
        BasicDBObject filter = new BasicDBObject();
        filter.put("enterprise_full_name", companyName);
        filter.put("source_site", new BasicDBObject("$ne", "因果树"));
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushFinancialEventsCount(result, companyName);
        return result;
    }

    /*******
     * 并购事件
     *******/
    public List<Map> getAcquirerEvents(String companyName) {
        return getAcquirerEvents(companyName, null, null);
    }

    public List<Map> getAcquirerEvents(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ACQUIRER_EVENT);
        BasicDBObject projection = new BasicDBObject();
        projection.put("_id", 0);
        projection.put("_src", 0);

        BasicDBObject filter = new BasicDBObject();
        filter.put("acquirer_full_name", companyName);
        filter.put("source_site", new BasicDBObject("$ne", "投资界"));
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    public Long countAcquirerEvents(String companyName) {
        Long result = redisRepo.getAcquirerEventsCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ACQUIRER_EVENT);
        BasicDBObject filter = new BasicDBObject();
        filter.put("acquirer_full_name", companyName);
        filter.put("source_site", new BasicDBObject("$ne", "投资界"));
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushAcquirerEventsCount(result, companyName);
        return result;
    }

    /*******
     * 被并购事件
     *******/
    public List<Map> getAcquireredEvents(String companyName) {
        return getAcquireredEvents(companyName, null, null);
    }

    public List<Map> getAcquireredEvents(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ACQUIRER_EVENT);
        BasicDBObject projection = new BasicDBObject();
        projection.put("_id", 0);
        projection.put("_src", 0);

        BasicDBObject filter = new BasicDBObject();
        filter.put("acquirered_full_name", companyName);
        filter.put("source_site", new BasicDBObject("$ne", "投资界"));
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    public Long countAcquireredEvents(String companyName) {
        Long result = redisRepo.getAcquireredEventsCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ACQUIRER_EVENT);
        //TODO 投资界的数据不准,以后要不要去掉这个过滤
        BasicDBObject filter = new BasicDBObject();
        filter.put("acquirered_full_name", companyName);
        filter.put("source_site", new BasicDBObject("$ne", "投资界"));
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushAcquireredEventsCount(result, companyName);
        return result;
    }

    /*******
     * 退出事件
     *******/
    public List<Map> getExitEvents(String companyName) {
        return getExitEvents(companyName, null, null);
    }

    public List<Map> getExitEvents(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_EXIT_EVENT);
        BasicDBObject projection = new BasicDBObject();
        projection.put("_id", 0);
        projection.put("_src", 0);

        BasicDBObject filter = new BasicDBObject("enterprise_short_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    public Long countExitEvents(String companyName) {
        Long result = redisRepo.getExitEventsCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_EXIT_EVENT);
        BasicDBObject filter = new BasicDBObject("enterprise_short_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushExitEventsCount(result, companyName);
        return result;
    }


    public List<Map> getPatent(String companyName) {
        return getPatent(companyName, null, null);
    }

    public List<Map> getPatent(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_PATENT);
        BasicDBObject filter = new BasicDBObject("submitter", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("publish_date", -1);
        sort.put("submit_date", -1);

        FindIterable<Document> find = collection.find(filter).sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countPatent(String companyName) {
        Long result = redisRepo.getPatentCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection collection = appMongoDatabase.getCollection(AppDataCollections.COLL_PATENT);
        BasicDBObject filter = new BasicDBObject("submitter", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushPatentCount(result, companyName);
        return result;
    }

    public List<Map> getTrademark(List<Map> tradeMarkList, Integer offset, Integer count) {
        return pageList(tradeMarkList, offset, count);
    }

    public Long countTrademark(List<Map> tradeMarkList) {
        return Long.valueOf(tradeMarkList.size());
    }

    public List<Map> getCopyright(List<Map> copyrightList, Integer offset, Integer count) {
        return pageList(copyrightList, offset, count);
    }

    public Long countCopyright(List<Map> copyrightList) {
        return Long.valueOf(copyrightList.size());
    }

    public List<Map> getSoftwareCopyright(List<Map> softwareCopyrightList, Integer offset, Integer count) {
        return pageList(softwareCopyrightList, offset, count);
    }

    public Long countSoftwareCopyright(List<Map> softwareCopyrightList) {
        return Long.valueOf(softwareCopyrightList.size());
    }

    public List<Map> getBidInfo(String companyName) {
        return getBidInfo(companyName, null, null);
    }

    public List<Map> getBidInfo(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BID_DETAIL);
        BasicDBObject filter = new BasicDBObject();
        filter.put("public_bid_company", companyName);
        //filter.put("bid_type", "招标"); //以后可能加上
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter)
                .sort(new BasicDBObject("publish_time", -1));
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }

        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countBidInfo(String companyName) {
        Long result = redisRepo.getBidInfoCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BID_DETAIL);
        BasicDBObject filter = new BasicDBObject();
        filter.put("public_bid_company", companyName);
        //filter.put("bid_type", "招标"); //以后可能加上
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushBidInfoCount(result, companyName);
        return result;
    }

    public List<Map> getWinInfo(String companyName) {
        return getWinInfo(companyName, null, null);
    }

    public List<Map> getWinInfo(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BID_DETAIL);
        BasicDBObject filter = new BasicDBObject();
        filter.put("win_bid_company", companyName);
        //filter.put("bid_type", "中标"); //以后可能加上
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter)
                .sort(new BasicDBObject("publish_time", -1));
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countWinInfo(String companyName) {
        Long result = redisRepo.getWinBidCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BID_DETAIL);
        BasicDBObject filter = new BasicDBObject();
        filter.put("win_bid_company", companyName);
        //filter.put("bid_type", "中标"); //以后可能加上
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushWinBidCount(result, companyName);
        return result;
    }

    /*******
     * 土地招拍挂
     *********/
    public List<Map> getLandAuction(String companyName) {
        return getLandAuction(companyName, null, null);
    }

    public List<Map> getLandAuction(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_LAND_AUCTION);
        BasicDBObject filter = new BasicDBObject();
        filter.put("bid_organization", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countLandAuction(String companyName) {
        Long result = redisRepo.getLandAuctionCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_LAND_AUCTION);
        BasicDBObject filter = new BasicDBObject();
        filter.put("bid_organization", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushLandAuctionCount(result, companyName);
        return result;
    }

    public List<Map> getCourtKtgg(String companyName) {
        return getCourtKtgg(companyName, null, null);
    }

    public List<Map> getCourtKtgg(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_COURT_KTGG);
        BasicDBObject filter = new BasicDBObject("litigant_list", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("court_time", -1);
        sort.put("bulletin_date", -1);
        FindIterable<Document> find = collection.find(filter).sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countCourtKtgg(String companyName) {
        Long result = redisRepo.getCourtSessionAnnCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_COURT_KTGG);
        BasicDBObject filter = new BasicDBObject("litigant_list", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushCourtSessionAnnCount(result, companyName);
        return result;
    }

    public List<Map> getCourtFygg(String companyName) {
        return getCourtFygg(companyName, null, null);
    }

    public List<Map> getCourtFygg(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BULLETIN);
        BasicDBObject filter = new BasicDBObject("litigant_list", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("bulletin_date", -1);
        FindIterable<Document> find = collection.find(filter).sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countCourtFygg(String companyName) {
        Long result = redisRepo.getCourtAnnCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BULLETIN);
        BasicDBObject filter = new BasicDBObject("litigant_list", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushCourtAnnCount(result, companyName);
        return result;
    }

    public List<Map> getTaxRank(String companyName) {
        return getTaxRank(companyName, null, null);
    }

    public List<Map> getTaxRank(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_TAX_RANK);
        BasicDBObject filter = new BasicDBObject("taxpayer_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countTaxRank(String companyName) {
        Long result = redisRepo.getTaxRankCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_TAX_RANK);
        BasicDBObject filter = new BasicDBObject("taxpayer_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushTaxRankCount(result, companyName);
        return result;
    }

    public List<Map> getJudgeProcess(String companyName) {
        return getJudgeProcess(companyName, null, null);
    }

    public List<Map> getJudgeProcess(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_JUDGE_PROCESS);
        BasicDBObject filter = new BasicDBObject("litigant_list", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("filing_date", -1);
        sort.put("court_time", -1);
        sort.put("close_date", -1);

        FindIterable<Document> find = collection.find(filter);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countJudgeProcess(String companyName) {
        Long result = redisRepo.getJudgeProcessCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_JUDGE_PROCESS);
        BasicDBObject filter = new BasicDBObject("litigant_list", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushJudgeProcessCount(result, companyName);
        return result;
    }

    public List<Map> getJudgement(String companyName) {
        return getJudgement(companyName, null, null);
    }

    public List<Map> getJudgement(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_JUDGEMENT);
        BasicDBObject filter = new BasicDBObject();
        filter.put("litigant_list", companyName);
//        filter.put("is_legal", 1);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("case_date", -1);
//        sort.put("bulletin_date", -1);

        FindIterable<Document> find = collection.find(filter)
//                .projection(new BasicDBObject("doc_content", 0))
                .sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }

        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countJudgement(String companyName) {
        Long result = redisRepo.getJudgementCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_JUDGEMENT);
        BasicDBObject filter = new BasicDBObject();
        filter.put("litigant_list", companyName);
//        filter.put("is_legal", 1);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushJudgementCount(result, companyName);
        return result;
    }

    public List<Map> getOwingTax(String companyName) {
        return getOwingTax(companyName, null, null);
    }

    public List<Map> getOwingTax(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_OWING_TAX);
        BasicDBObject filter = new BasicDBObject("company", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        FindIterable<Document> find = collection.find(filter)
                .sort(new BasicDBObject("bulletin_date", -1));
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countOwingTax(String companyName) {
        Long result = redisRepo.getOwingTaxCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_OWING_TAX);
        BasicDBObject filter = new BasicDBObject("company", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushOwingTaxCount(result, companyName);
        return result;
    }

    public List<Map> getPenalty(String companyName) {
        return getPenalty(companyName, null, null);
    }

    public List<Map> getPenalty(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_PENALTY);
        BasicDBObject filter = new BasicDBObject("accused_people", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("penalty_time", -1);
        sort.put("publish_time", -1);

        FindIterable<Document> find = collection.find(filter).sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countPenalty(String companyName) {
        Long result = redisRepo.getPenaltyCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_PENALTY);
        BasicDBObject filter = new BasicDBObject("accused_people", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushPenaltyCount(result, companyName);
        return result;
    }

    public List<Map> getShixinInfo(String companyName) {
        return getShixinInfo(companyName, null, null);
    }

    public List<Map> getShixinInfo(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_DISHONEST_INFO);
        BasicDBObject filter = new BasicDBObject("i_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("publish_date", -1);//发布时间
        sort.put("reg_date", -1);//立案时间
        FindIterable<Document> find = collection.find(filter).sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countShixinInfo(String companyName) {
        Long result = redisRepo.getDishonestCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_DISHONEST_INFO);
        BasicDBObject filter = new BasicDBObject("i_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushDishonestCount(result, companyName);
        return result;
    }

    public List<Map> getZhixingInfo(String companyName) {
        return getZhixingInfo(companyName, null, null);
    }

    public List<Map> getZhixingInfo(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_EXECUTION_INFO);
        BasicDBObject filter = new BasicDBObject("i_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("case_date", -1);//开庭时间
        FindIterable<Document> find = collection.find(filter).sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countZhixingInfo(String companyName) {
        Long result = redisRepo.getExecutionCount(companyName);
        if (result != null && result > 0) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_EXECUTION_INFO);
        BasicDBObject filter = new BasicDBObject("i_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        result = collection.count(filter);
        redisRepo.pushExecutionCount(result, companyName);
        return result;
    }

    public SentimentDataItem getSentiment(String companyName, String companyField, String type, String sortKey,
                                          SentimentType sentimentType, boolean onlyCount, Integer offset, Integer count) {
        SentimentDataItem item;

        String coll;
        List<Map> result = Lists.newArrayList();
        Map<String, Number> countMap = Maps.newHashMap();
        switch (sentimentType) {
            case NEWS:
                coll = AppDataCollections.COLL_BAIDU_NEWS;
                if (!onlyCount) {
                    result = getBaiduNews(coll, companyName, companyField, type, sortKey, offset, count);
                }
                countMap = countBaiduNews(companyName, companyField);
                break;
            case TIEBA:
                coll = AppDataCollections.COLL_TIEBA;
                break;
            case WECHAT:
                coll = AppDataCollections.COLL_WECHAT;
                break;
            case WEIBO:
                coll = AppDataCollections.COLL_SINA_WEIBO;
                break;
        }

        item = new SentimentDataItem();
        Long pos = countMap.get("positive") == null ? 0L : Long.valueOf(countMap.get("positive").toString());
        Long neg = countMap.get("negative") == null ? 0L : Long.valueOf(countMap.get("negative").toString());
        Long net = countMap.get("neutral") == null ? 0L : Long.valueOf(countMap.get("neutral").toString());
        if (!onlyCount) {
            item.setData(result);
        }
        Long total = countMap.get("all") == null ? 0L : Long.valueOf(countMap.get("all").toString());
        item.setTotalCount(total);
        item.setPositive(pos);
        item.setNegative(neg);
        item.setNeutral(net);

        return item;
    }

    public List<Map> getBaiduNews(String coll, String companyName, String companyField, String type, String sortKey, Integer offset, Integer count) {
        return getSentiment(coll, companyName, companyField, type, sortKey, offset, count);
    }

    public Map<String, Number> countBaiduNews(String companyName, String companyField) {
        Map<String, Number> result = redisRepo.getBaiduNewsCountMap(companyName);
        if (result != null) {
            return result;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BAIDU_NEWS);
        BasicDBObject filter = new BasicDBObject(companyField, companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        Long all = collection.count(filter);

        filter.put("sentiment", "正面");
        Long positive = collection.count(filter);

        filter.put("sentiment", "负面");
        Long negative = collection.count(filter);

        result = Maps.newHashMap();
        result.put("all", all);
        result.put("positive", positive);
        result.put("negative", negative);
        result.put("neutral", all - negative - positive);
        redisRepo.pushBaiduNewsCountMap(result, companyName);
        return result;
    }

    public Long countBaiduNewsWithType(String companyName, String newsTypeInZh) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BAIDU_NEWS);
        BasicDBObject filter = new BasicDBObject("keyword", companyName);
        if (newsTypeInZh != null) {
            if (newsTypeInZh.equals("中性")) {
                filter.put("sentiment", new BasicDBObject("$nin", Arrays.asList("正面", "负面")));
            } else {
                filter.put("sentiment", newsTypeInZh);
            }
        }
        LogicDeleteUtil.addDeleteFilter(filter);
        return collection.count(filter);
    }

    public Document getInvestInstitution(String companyName) {
        Document doc = redisRepo.getInvestInstitution(companyName);
        if (doc != null) {
            return doc;
        }

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_INVESTMENT_INSTITUTIONS_INNOTREE);

        BasicDBObject filter = new BasicDBObject();
        filter.put("lp_full_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject projection = new BasicDBObject();
        projection.put("_id", 0);
        projection.put("_src", 0);
        MongoCursor<Document> cursor = collection.find(filter).projection(projection).iterator();

        if (cursor.hasNext()) {
            doc = cursor.next();
            redisRepo.pushInvestInstitution(doc, companyName);
            return doc;
        } else {
            return null;
        }
    }

    private List<Map> getSentiment(String coll, String companyName, String companyField, String type, String sortKey) {
        return getSentiment(coll, companyName, companyField, type, sortKey, null, null);
    }

    private List<Map> getSentiment(String coll, String companyName, String companyField, String type, String sortKey, Integer offset, Integer count) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(coll);
        BasicDBObject filter = new BasicDBObject(companyField, companyName);

        //TODO 其他类型是否要这个
        if (coll.equals(AppDataCollections.COLL_BAIDU_NEWS)) {
            if (type == null || type.equals("中性")) {
                filter.put("sentiment", new BasicDBObject("$nin", Arrays.asList("正面", "负面")));
            } else {
                filter.put("sentiment", type);
            }
        }
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject(sortKey, -1);
        FindIterable<Document> find = collection.find(filter)
                .projection(new BasicDBObject("_id", 0))
                .sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        List<Map> result = Lists.newArrayList();
        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    public List<Map> getBaiduNews(String companyName, String newsTypeInZh, Integer offset, Integer count) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BAIDU_NEWS);
        BasicDBObject filter = new BasicDBObject("keyword", companyName);

        //其他类型是否要这个
        if (newsTypeInZh.equals("中性")) {
            filter.put("sentiment", new BasicDBObject("$nin", Arrays.asList("正面", "负面")));
        } else {
            filter.put("sentiment", newsTypeInZh);
        }
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject("publish_time", -1);
        FindIterable<Document> find = collection.find(filter)
                .projection(new BasicDBObject("_id", 0))
                .sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        List<Map> result = Lists.newArrayList();
        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

    private static Map<String, String> reportTypeMap = Maps.newHashMap();

    static {
        reportTypeMap.put("一季", "first");
        reportTypeMap.put("一季度", "first");
        reportTypeMap.put("二季", "second");
        reportTypeMap.put("中期", "second");
        reportTypeMap.put("三季", "third");
        reportTypeMap.put("三季度", "third");
        reportTypeMap.put("年度", "fourth");
        reportTypeMap.put("四季", "fourth");
    }

    public TreeMap<String, String> getSectorMap(String companyName) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_SSGS_BASEINFO);

        List<String> names = CompanyUtil.trickName(companyName);
        BasicDBObject filter = new BasicDBObject("company_full_name", new BasicDBObject("$in", names));
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject projection = new BasicDBObject();
        projection.put("stock_code", 1); //返回字段只要股票代码stock_code

        TreeMap<String, String> result = Maps.newTreeMap();
        MongoCursor<Document> cursor = collection.find(filter)
                .projection(projection).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            if (doc.get("stock_code") != null) {
                for (String key : sectorConfMap.keySet()) {
                    if (doc.get("stock_code").toString().startsWith(key)) {
                        result.put(sectorConfMap.get(key), String.valueOf(doc.get("stock_code")));
                        break;
                    }
                }
            }
        }
        return result;
    }

    private List<Map> pageList(List<Map> list, Integer offset, Integer count) {
        if (list == null) {
            return Lists.newArrayList();
        }
        if (list.size() >= (offset + count)) {
            Integer toIndex = offset + count;
            if (toIndex > list.size()) {
                toIndex = list.size();
            }
            return list.subList(offset, toIndex);
        } else if (list.size() > offset && list.size() < (offset + count)) {
            return list.subList(offset, list.size());
        } else {
            //越界
            return Collections.emptyList();
        }
    }

    public Map<String, Object> trickCaiBaoDoc(Document doc, Map<String, String> trickConf) {
        if (doc.get("data_info") != null) {
            Map<String, Object> data = (Map<String, Object>) doc.get("data_info");
            data.keySet().stream().filter(key -> trickConf.keySet().contains(key.trim())).forEach(key -> {
                doc.put(trickConf.get(key.trim()), data.get(key));
            });
            doc.remove("data_info");
        }
        return doc;
    }

    public List<Map> getHiringInfo(String companyName, Integer offset, Integer count) {
        List<Map> result = Lists.newArrayList();

        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BAIDU_BAIPIN_JOB);

        BasicDBObject filter = new BasicDBObject("company_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        BasicDBObject sort = new BasicDBObject();
        sort.put("publish_date", -1);
        FindIterable<Document> find = collection.find(filter).sort(sort);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next());
        }

        return result;
    }

    public Long countHiringInfo(String companyName) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BAIDU_BAIPIN_JOB);
        BasicDBObject filter = new BasicDBObject("company_name", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        return collection.count(filter);
    }

    public Document getCustomsInformation(String companyName) {
        Document doc = redisRepo.getCustomsInformation(companyName);
        if (doc == null) {
            MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_CUSTOMS_INFORMATION);
            BasicDBObject filter = new BasicDBObject();
            filter.put("company", companyName);
            LogicDeleteUtil.addDeleteFilter(filter);

            MongoCursor<Document> cursor = collection.find(filter).iterator();
            if (cursor.hasNext()) {
                doc = cursor.next();
                redisRepo.pushCustomsInformation(doc, companyName);
            } else {
                return null;
            }
        }
        return doc;
    }

    public List<Map> getCustomsPenalty(List<Map> customsPenaltyList, Integer offset, Integer count) {
        return pageList(customsPenaltyList, offset, count);
    }

    public Long countCustomsPenalty(List<Map> allCustomsPenaltyList) {
        return (long) allCustomsPenaltyList.size();
    }

    public Map<Integer, Map<String, Object>> getEnvironmentProtection(String companyName) {
        Map<Integer, Map<String, Object>> result = redisRepo.getEnvironmentProtection(companyName);
        if (result == null) {
            MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENVIRONMENT_PROTECTION_INFORMATION);
            BasicDBObject filter = new BasicDBObject("company", companyName);
            LogicDeleteUtil.addDeleteFilter(filter);

            MongoCursor<Document> cursor = collection.find(filter).iterator();

            result = Maps.newHashMap();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String yearQuarter = doc.getString("year_quarter");
                if (yearQuarter == null) {
                    continue;
                } else {
                    if (yearQuarter.length() > 4) {
                        Integer yearNumber = Integer.valueOf(yearQuarter.substring(0, 4));
                        String reportType = null;
                        if (yearQuarter.contains("一季")) {
                            reportType = "一季";
                        } else if (yearQuarter.contains("二季")) {
                            reportType = "二季";
                        } else if (yearQuarter.contains("三季")) {
                            reportType = "三季";
                        } else if (yearQuarter.contains("四季")) {
                            reportType = "四季";
                        }
                        if (reportType != null && reportTypeMap.get(reportType) != null) {
                            Map<String, Object> map = result.get(yearNumber) == null ?
                                    Maps.newHashMap() : result.get(yearNumber);
                            map.put(reportTypeMap.get(reportType), doc);
                            result.put(yearNumber, map);
                            redisRepo.pushEnvironmentProtection(result, companyName);
                        }
                    }
                }
            }
        }
        return result;
    }

    public Long countEnvironmentProtection(String companyName) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENVIRONMENT_PROTECTION_INFORMATION);
        BasicDBObject filter = new BasicDBObject("company", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        return collection.count(filter);
    }
}
