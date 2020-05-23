package com.haizhi.iap.search.repo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.utils.LogicDeleteUtil;
import com.haizhi.iap.search.conf.AppDataCollections;
import com.haizhi.iap.search.model.DynamicInfo;
import com.haizhi.iap.search.model.DynamicInfo.DynamicInfoBuilder;
import com.haizhi.iap.search.utils.DateUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/6/22.
 */
@Repository
public class DynamicInfoRepo {

    @Qualifier("appMongoDatabase")
    @Autowired
    MongoDatabase appMongoDatabase;

    List<String> infoTypeList = Lists.newArrayList("changerecords", "court_ktgg", "judge_process",
            "judgement_wenshu", "bulletin", "bid_detail", "ssgs_notice_cninfo", "annual_reports",
            "news", "patent", "shixin_info", "zhixing_info", "owing_tax", "tax_payer_level_a", "penalty");

    public List<DynamicInfo> getChangeRecords(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        BasicDBObject filter = new BasicDBObject();
        filter.put("company", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter)
                .projection(new BasicDBObject("changerecords", 1)).iterator();

        List<Map<String, Object>> changerecords = Lists.newArrayList();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            if (doc.get("changerecords") instanceof List) {
                changerecords = (List<Map<String, Object>>) doc.get("changerecords");
            }
        }
        return process(changerecords, infoTypeList.get(0), startTime);
    }

    public List<DynamicInfo> getCourtKtgg(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_COURT_KTGG);
        BasicDBObject filter = new BasicDBObject();
        filter.put("litigant_list", companyName);
        filterTime(filter, "court_time", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> ktggList = Lists.newArrayList();
        while (cursor.hasNext()) {
            ktggList.add(cursor.next());
        }
        return process(ktggList, infoTypeList.get(1));
    }

    public List<DynamicInfo> getJudgeProcess(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_JUDGE_PROCESS);
        BasicDBObject filter = new BasicDBObject();
        filter.put("litigant_list", companyName);
        filterTime(filter, "filing_date", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> processList = Lists.newArrayList();
        while (cursor.hasNext()) {
            processList.add(cursor.next());
        }
        return process(processList, infoTypeList.get(2));
    }

    public List<DynamicInfo> getJudgementWenshu(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_JUDGEMENT);
        BasicDBObject filter = new BasicDBObject();
        filter.put("litigant_list", companyName);
//        filter.put("is_legal", 1);
        filterTime(filter, "case_date", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> judgementList = Lists.newArrayList();
        while (cursor.hasNext()) {
            judgementList.add(cursor.next());
        }
        return process(judgementList, infoTypeList.get(3));
    }

    public List<DynamicInfo> getBulletin(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BULLETIN);
        BasicDBObject filter = new BasicDBObject();
        filter.put("litigant_list", companyName);
        filterTime(filter, "bulletin_date", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> bulletinList = Lists.newArrayList();
        while (cursor.hasNext()) {
            bulletinList.add(cursor.next());
        }
        return process(bulletinList, infoTypeList.get(4));
    }

    public List<DynamicInfo> getBid(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_BID_DETAIL);
        BasicDBObject filter1 = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        values.add(new BasicDBObject("public_bid_company", companyName));
        values.add(new BasicDBObject("win_bid_company", companyName));
        filter1.put("$or", values);

        BasicDBObject filter2 = new BasicDBObject();
        filterTime(filter2, "publish_time", startTime);
        LogicDeleteUtil.addDeleteFilter(filter2);

        BasicDBList list = new BasicDBList();
        list.add(filter1);
        list.add(filter2);
        BasicDBObject filter = new BasicDBObject("$and", list);
        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> bidList = Lists.newArrayList();
        while (cursor.hasNext()) {
            bidList.add(cursor.next());
        }
        return process(bidList, infoTypeList.get(5));
    }

    public List<DynamicInfo> getListingNotice(List<String> stockCodes, String startTime) {
        if (stockCodes == null || stockCodes.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_SSGS_NOTICE_CNINFO);
        BasicDBObject filter = new BasicDBObject();
        filter.put("code", new BasicDBObject("$in", stockCodes));
        filterTime(filter, "publish_time", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> noticeList = Lists.newArrayList();
        while (cursor.hasNext()) {
            noticeList.add(cursor.next());
        }
        return process(noticeList, infoTypeList.get(6));
    }

    public List<DynamicInfo> getAnnualReport(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ANNUAL_REPORT);
        BasicDBObject filter = new BasicDBObject();
        filter.put("company", companyName);
        Integer year = Integer.parseInt(startTime.substring(0, 4)) - 1;
        filterTime(filter, "year", year.toString());
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> reportList = Lists.newArrayList();
        while (cursor.hasNext()) {
            reportList.add(cursor.next());
        }
        return process(reportList, infoTypeList.get(7));
    }

    public List<DynamicInfo> getNews(String companyName) {
        //TODO 没法查
        return Collections.emptyList();
    }

    public List<DynamicInfo> getPatent(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_PATENT);
        BasicDBObject filter = new BasicDBObject();
        filter.put("submitter", companyName);
        filterTime(filter, "publish_date", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> patentList = Lists.newArrayList();
        while (cursor.hasNext()) {
            patentList.add(cursor.next());
        }
        return process(patentList, infoTypeList.get(9));
    }

    // "shixin_info", "zhixing_info"
    public List<DynamicInfo> getShixinInfo(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_DISHONEST_INFO);
        BasicDBObject filter = new BasicDBObject();
        filter.put("business_entity", companyName);
        filterTime(filter, "publish_date", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> shixinList = Lists.newArrayList();
        while (cursor.hasNext()) {
            shixinList.add(cursor.next());
        }
        return process(shixinList, infoTypeList.get(10));
    }

    public List<DynamicInfo> getZhixingInfo(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_EXECUTION_INFO);
        BasicDBObject filter = new BasicDBObject();
        filter.put("i_name", companyName);
        filterTime(filter, "case_date", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> zhixingList = Lists.newArrayList();
        while (cursor.hasNext()) {
            zhixingList.add(cursor.next());
        }
        return process(zhixingList, infoTypeList.get(11));
    }

    //"enterprise_owing_tax", "tax_payer_level_a"
    public List<DynamicInfo> getOwingTax(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_OWING_TAX);
        BasicDBObject filter = new BasicDBObject();
        filter.put("company", companyName);
        filterTime(filter, "_utime", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> owingTaxList = Lists.newArrayList();
        while (cursor.hasNext()) {
            owingTaxList.add(cursor.next());
        }
        return process(owingTaxList, infoTypeList.get(12));
    }

    public List<DynamicInfo> getTaxPayerLevelA(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_TAX_RANK);
        BasicDBObject filter = new BasicDBObject();
        filter.put("taxpayer_name", companyName);
        Integer year = Integer.parseInt(startTime.substring(0, 4)) - 1;
        filterTime(filter, "year", year.toString());
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> shixinList = Lists.newArrayList();
        while (cursor.hasNext()) {
            shixinList.add(cursor.next());
        }
        return process(shixinList, infoTypeList.get(13));
    }

    public List<DynamicInfo> getPenalty(String companyName, String startTime) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_PENALTY);
        BasicDBObject filter = new BasicDBObject();
        filter.put("accused_people", companyName);
        filterTime(filter, "publish_time", startTime);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).iterator();

        List<Map<String, Object>> penaltyList = Lists.newArrayList();
        while (cursor.hasNext()) {
            penaltyList.add(cursor.next());
        }
        return process(penaltyList, infoTypeList.get(14));
    }

    private List<DynamicInfo> process(List<Map<String, Object>> maps, String type) {
        List<DynamicInfo> result = Lists.newArrayList();
        for (Map<String, Object> map : maps) {
            DynamicInfo info = process(map, type);
            if (info != null) {
                info.setSubTypeEn(type);
                setRuleType(info, type);

                if ("bid_detail".equals(type)) {
                    info.setSubTypeEn("bid_info");
                }

                result.add(info);
            }
        }
        return result;
    }

    private List<DynamicInfo> process(List<Map<String, Object>> maps, String type, String startTime) {
        List<DynamicInfo> result = Lists.newArrayList();
        for (Map<String, Object> map : maps) {
            DynamicInfo info = process(map, type);
            if (info != null && startTime.compareTo(info.getDate()) < 0) {

                info.setSubTypeEn(type);
                setRuleType(info, type);

                if ("bid_detail".equals(type)) {
                    info.setSubTypeEn("bid_info");
                }

                result.add(info);
            }
        }
        return result;
    }

    private void setRuleType(DynamicInfo info, String type) {
        if (type.equals(infoTypeList.get(0))) {
            info.setTypeEn("unknown");
        } else if (type.equals(infoTypeList.get(1))) {
            info.setTypeEn("risk");
        } else if (type.equals(infoTypeList.get(2))) {
            info.setTypeEn("risk");
        } else if (type.equals(infoTypeList.get(3))) {
            info.setTypeEn("risk");
        } else if (type.equals(infoTypeList.get(4))) {
            info.setTypeEn("risk");
        } else if (type.equals(infoTypeList.get(5))) {
            info.setTypeEn("marketing");
        } else if (type.equals(infoTypeList.get(6))) {
            info.setTypeEn("unknown");
        } else if (type.equals(infoTypeList.get(7))) {
            info.setTypeEn("unknown");
        } else if (type.equals(infoTypeList.get(9))) {
            info.setTypeEn("marketing");
        } else if (type.equals(infoTypeList.get(10))) {
            info.setTypeEn("risk");
        } else if (type.equals(infoTypeList.get(11))) {
            info.setTypeEn("risk");
        } else if (type.equals(infoTypeList.get(12))) {
            info.setTypeEn("risk");
        } else if (type.equals(infoTypeList.get(13))) {
            info.setTypeEn("marketing");
        } else if (type.equals(infoTypeList.get(14))) {
            info.setTypeEn("risk");
        } else {
            info.setTypeEn("unknown");
        }
    }

    private DynamicInfo process(Map<String, Object> map, String type) {
        DynamicInfoBuilder builder = DynamicInfo.builder();

        if (type.equals(infoTypeList.get(0))) {
            if (map.get("change_item") != null && !Strings.isNullOrEmpty(map.get("change_item").toString())) {
                builder.title(map.get("change_item").toString());
            } else {
                builder.title("一条新的工商变更");
            }
            builder.type("工商变更");

            processTime(builder, map, "change_date");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(1))) {
            if (map.get("content") != null && !Strings.isNullOrEmpty(map.get("content").toString())) {
                builder.title(map.get("content").toString());
            } else {
                builder.title("一条新的开庭公告");
            }
            builder.type("开庭公告");

            processTime(builder, map, "court_time");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(2))) {
            if (map.get("case_id") != null && !Strings.isNullOrEmpty(map.get("case_id").toString())) {
                builder.title(map.get("case_id").toString());
            } else {
                builder.title("一条新的审判流程");
            }
            builder.type("审判流程");

            processTime(builder, map, "filing_date");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(3))) {
            if (map.get("case_name") != null && !Strings.isNullOrEmpty(map.get("case_name").toString())) {
                builder.title(map.get("case_name").toString());
            } else {
                builder.title("一条新的裁判文书");
            }
            builder.type("裁判文书");

            processTime(builder, map, "case_date");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(4))) {
            if (map.get("bulletin_content") != null && !Strings.isNullOrEmpty(map.get("bulletin_content").toString())) {
                builder.title(map.get("bulletin_content").toString());
            } else {
                builder.title("一条新的法院公告");
            }
            builder.type("法院公告");

            processTime(builder, map, "bulletin_date");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(5))) {
            if (map.get("title") != null && !Strings.isNullOrEmpty(map.get("title").toString())) {
                builder.title(map.get("title").toString());
            } else {
                builder.title("一条新的招中标");
            }

//            if (map.get("bid_type") != null && !Strings.isNullOrEmpty(map.get("bid_type").toString())) {
//                builder.type(map.get("bid_type").toString());
//            } else {
            builder.type("标书");
//            }

            /*if (map.get("bid_date") != null && map.get("bid_date").toString().length() >= 10) {
                builder.date(map.get("bid_date").toString().substring(0, 10));
            } else */
            processTime(builder, map, "publish_time");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(6))) {
            if (map.get("title") != null && !Strings.isNullOrEmpty(map.get("title").toString())) {
                builder.title(map.get("title").toString());
            } else {
                builder.title("一条新的上市公告");
            }
            builder.type("上市公告");

            processTime(builder, map, "publish_time");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(7))) {
            if (map.get("year") != null && !Strings.isNullOrEmpty(map.get("year").toString())) {
                builder.title("企业发布" + map.get("year").toString() + "年报");
            } else {
                builder.title("一条新的企业年报");
            }
            builder.type("年报");

            processTime(builder, map, "year");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(8))) {
//            if (map.get("bulletin_content") != null) {
//                builder.title(map.get("bulletin_content").toString());
//            } else {
//                return null;
//            }
//            builder.type("新闻");
//
//            if (map.get("bulletin_date") != null && map.get("bulletin_date").toString().length() >= 10) {
//                builder.date(map.get("bulletin_date").toString().substring(0, 10));
//            }
//            builder.detail(map);
//            return builder.build();
            //TODO 没法查,暂时去掉
        } else if (type.equals(infoTypeList.get(9))) {
            if (map.get("title") != null && !Strings.isNullOrEmpty(map.get("title").toString())) {
                builder.title(map.get("title").toString());
            } else {
                builder.title("一条新的专利");
            }
            builder.type("专利");

            processTime(builder, map, "publish_date");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(10))) {
            if (map.get("case_id") != null && !Strings.isNullOrEmpty(map.get("case_id").toString())) {
                builder.title(map.get("case_id").toString());
            } else {
                builder.title("一条新的失信人信息");
            }
            builder.type("失信人信息");

            processTime(builder, map, "publish_date");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(11))) {
            if (map.get("case_id") != null && !Strings.isNullOrEmpty(map.get("case_id").toString())) {
                builder.title(map.get("case_id").toString());
            } else {
                builder.title("一条新的被执行人信息");
            }
            builder.type("被执行人信息");

            processTime(builder, map, "case_date");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(12))) {
            if (map.get("tax_item") != null && !Strings.isNullOrEmpty(map.get("tax_item").toString())) {
                builder.title(map.get("tax_item").toString() + "欠税公告");
            } else {
                builder.title("一条新的欠税公告");
            }
            builder.type("欠税公告");

            processTime(builder, map, "_utime");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(13))) {
            if (map.get("year") != null && !Strings.isNullOrEmpty(map.get("year").toString())) {
                builder.title(map.get("year").toString() + "纳税等级为A");
            } else {
                builder.title("一条新的纳税等级为A");
            }
            builder.type("纳税等级为A");

            processTime(builder, map, "year");
            builder.detail(map);
        } else if (type.equals(infoTypeList.get(14))) {
            if (map.get("title") != null && !Strings.isNullOrEmpty(map.get("title").toString())) {
                builder.title(map.get("title").toString());
            } else {
                builder.title("一条新的行政处罚");
            }
            builder.type("行政处罚");

            processTime(builder, map, "publish_time");
            builder.detail(map);
        }
        DynamicInfo dynamicInfo = builder.build();
        if (!Strings.isNullOrEmpty(dynamicInfo.getDate())) {
            return dynamicInfo;
        } else {
            return null;
        }
    }

    protected BasicDBObject filterTime(BasicDBObject filter, String timeField, String startTime) {
        if (Strings.isNullOrEmpty(timeField)) {
            return filter;
        }

        if (timeField.equals("_utime")) {
            filter.put("_utime", new BasicDBObject("$gte", startTime));
        } else {
            BasicDBList times = new BasicDBList();
            BasicDBList andList1 = new BasicDBList();
            andList1.add(new BasicDBObject(timeField, new BasicDBObject("$exists", true)));
            andList1.add(new BasicDBObject(timeField, new BasicDBObject("$gte", startTime)));
            BasicDBList andList2 = new BasicDBList();
            BasicDBList emptyValues = new BasicDBList();
            emptyValues.add(null);
            emptyValues.add("");
            andList2.add(new BasicDBObject(timeField, new BasicDBObject("$in", emptyValues)));
            andList2.add(new BasicDBObject("_utime", new BasicDBObject("$gte", startTime)));
            times.add(new BasicDBObject("$and", andList1));
            times.add(new BasicDBObject("$and", andList2));
            filter.put("$or", times);
        }
        return filter;
    }

    protected DynamicInfoBuilder processTime(DynamicInfoBuilder builder, Map<String, Object> data, String timeField){
        if(builder != null){
            if (data.get(timeField) != null) {
                if(DateUtils.isLegalTimeStr(data.get(timeField).toString())){
                    //合法日期格式
                    if(data.get(timeField).toString().length() >= 10){
                        builder.date(data.get(timeField).toString().substring(0, 10));
                    }else {
                        builder.date(data.get(timeField).toString());
                    }
                }
            } else if (data.get("_utime") != null) {
                if(DateUtils.isLegalTimeStr(data.get("_utime").toString())){
                    //合法日期格式
                    if(data.get("_utime").toString().length() >= 10){
                        builder.date(data.get("_utime").toString().substring(0, 10));
                    }else {
                        builder.date(data.get("_utime").toString());
                    }
                }
            }
        }
        return builder;
    }
}
