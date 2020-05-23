package com.haizhi.iap.follow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.enums.TimeOption;
import com.haizhi.iap.follow.model.FollowItem;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.model.TypeQuery;
import com.haizhi.iap.follow.repo.*;
import com.haizhi.iap.follow.utils.*;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFSDBFile;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.BSONObject;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by chenbo on 17/1/16.
 *
 * @desc excel 导出服务
 */
@Slf4j
@Service
public class ExcelExportProcess {
    MongoDatabase mongoDatabase;

    @Setter
    @Value("${app.mongodb.database}")
    String dbName;

    @Setter
    @Autowired
    @Qualifier(value = "appMongo")
    MongoClient mongo;

    @Setter
    @Autowired
    TaskRepo taskRepo;

    @Setter
    @Autowired
    TypeQueryRepo typeQueryRepo;

    @Setter
    @Autowired
    FollowItemRepo followItemRepo;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    FileUploadService fileUploadService;

    @Setter
    @Value("${index.enterprise_overview}")
    String indexOfEnterpriseOverview;

    @Setter
    @Value("${type.enterprise_overview}")
    String typeOfEnterpriseOverview;

    @Setter
    @Autowired
    ElasticSearchRepo elasticSearchRepo;

    private static Integer BYTE_BUF_SIZE = 1024;

    private static String UPLOAD_DIR = "bigdata";

    public static String ENTERPRISE_DATA_GOV = "enterprise_data_gov";

    Pattern specialPattern = Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*——+|{}；。，、？]");

    @PostConstruct
    public void init() {
        mongoDatabase = mongo.getDatabase(dbName);
    }

    public String process(Long taskId) {

        Task task = taskRepo.findById(taskId);
        task.setPercent(0d);
        taskRepo.update(task);
        taskRepo.updateStatus(task.getId(), TaskStatus.RUNNING.getCode());
        log.info("task {} start ...", task.getId());

        List<String> typeList = Lists.newArrayList(task.getDataType().split(","));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(BYTE_BUF_SIZE);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bos);

        List<FollowItem> followItems;
        if (task.getFollowListId() == null || task.getFollowListId() < 0
                || !Strings.isNullOrEmpty(task.getCompanyNames())) {
            //导出多家公司
            followItems = Lists.newArrayList();
            for (String company : task.getCompanyNames().split(",")) {
                followItems.add(new FollowItem(company));
            }
        } else {
            //导出某个关注列表
            followItems = followItemRepo.findByList(task.getFollowListId());
        }
        log.info("follow items size {}", followItems.size());

        for (int i = 0; i < typeList.size(); i++) {
            String typeName = typeList.get(i);
            double outPercent = (double) i / typeList.size();

            TypeQuery typeQuery = typeQueryRepo.findByCondition(null, typeName);
            if (typeQuery == null) {
                continue;
            }
            List<Map<String, Object>> querys = Lists.newArrayList();
            List<String> fieldNames = Lists.newArrayList();
            try {
                querys = objectMapper.readValue(typeQuery.getQuery(), List.class);
                fieldNames.addAll(querys.stream().map(map -> String.valueOf(map.get("key"))).collect(Collectors.toList()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (typeName.equals("融资事件")) {
                fieldNames.add("是否为公司");
            }
            MongoCollection<Document> collection = mongoDatabase.getCollection(typeQuery.getCollection());
            log.info(" get mongo collection {}", typeQuery.getCollection());
            BasicDBObject baseQuery = basicQuery(task, typeQuery);

            //后缀注意,HSSFWorkBook用xls
            String excelName = typeName + "_" + DateUtils.FORMAT_DAY.format(new Date()) + ".xlsx";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BYTE_BUF_SIZE);
            log.info("excelName {}", excelName);

            /**注意这里不能用HSSFWorkbook,它是03的版本,最多只支持65535行**/
            Workbook wb = new XSSFWorkbook();
            String sheetName = WorkbookUtil.createSafeSheetName(excelName);
            Sheet sheet = wb.createSheet(sheetName);
            log.info(" sheet created...");
            //写表头
            Row title = sheet.createRow(0);
            for (int col = 0; col < fieldNames.size(); col++) {
                title.createCell(col).setCellValue(fieldNames.get(col));
            }

            Set<String> companySet = Sets.newHashSet();

            int rowIndex = 1;

            log.info("start for (int j = 0; j < followItems.size(); j++) {");
            for (int j = 0; j < followItems.size(); j++) {
                FollowItem item = followItems.get(j);

                //实时更新进度(粗粒度)百分比
                double percent = outPercent + 1.0 / (typeList.size() * followItems.size()) * (j + 1);
                BigDecimal decimal = new BigDecimal(percent * 100);
                percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                task.setPercent(percent);
                log.info("任务 {} 进度: {}", task.getId(), percent);
                taskRepo.update(task);

                String companyName = CompanyUtil.ignoreENBrackets(item.getCompanyName()).trim();
                log.info("start  MongoCursor<Document> cursor = null; {}", companyName);
                MongoCursor<Document> cursor = null;
                if (!Strings.isNullOrEmpty(companyName)
                        && !Strings.isNullOrEmpty(typeQuery.getCompanyParam())) {
                    if (typeQuery.getCollection().equals(ENTERPRISE_DATA_GOV)) {
                        // 工商要求精确匹配
                        baseQuery.put(typeQuery.getCompanyParam(), companyName);
                    }

                    //工商基本信息和上市公告基本信息 特殊处理，只导出基本信息
                    if (typeName.equals("工商基本信息") && !Strings.isNullOrEmpty(typeQuery.getCompanyParam())) {
                        BasicDBObject mongQuery = new BasicDBObject(typeQuery.getCompanyParam(),
                                baseQuery.get(typeQuery.getCompanyParam()));
                        LogicDeleteUtil.addDeleteFilter(mongQuery);
                        cursor = collection.find(mongQuery).sort(new BasicDBObject("_utime", -1)).limit(1).iterator();

                    } else if (typeQuery.getCollection().startsWith("ssgs")) {
                        //先获取股票代码
                        log.info("typeName.startsWith(\"上市\") before... {}", companyName);
                        MongoCollection<Document> basicColl = mongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
                        BasicDBObject filter = new BasicDBObject("company", CompanyUtil.ignoreENBrackets(companyName));
                        LogicDeleteUtil.addDeleteFilter(filter);
                        MongoCursor<Document> basicCursor = basicColl.find(filter)
                                .projection(new BasicDBObject("stock_code", 1))
                                .iterator();
                        log.info("find stock codes of {}", companyName);
                        if (basicCursor.hasNext()) {
                            String codes = basicCursor.next().getString("stock_code");
                            if (codes != null) {
                                BasicDBObject filter1 = new BasicDBObject(typeQuery.getCompanyParam(), new BasicDBObject("$in", codes.split(",")));
                                LogicDeleteUtil.addDeleteFilter(filter1);
                                cursor = collection.find(filter1)
                                        .sort(new BasicDBObject("_utime", -1))
                                        .iterator();
                            }
                        }
                        log.info("typeName.startsWith(\"上市\") after... {}", companyName);
                    } else {
                        if (companyName != null) {
                            //这里匹配有大坑,英文括号算正则特殊字符,导致程序出错,主动退出
                            Matcher matcher = specialPattern.matcher(companyName);
                            if (matcher.find()) {
                                log.warn("company name {} contains special char sequences, skip!", companyName);
                                continue;
                            }
//                            baseQuery.put(typeQuery.getCompanyParam(), Pattern.compile("^" + companyName + ""));
                            //需求变更 前缀匹配->完全匹配
                            baseQuery.put(typeQuery.getCompanyParam(), companyName);
                        }
                        log.info("query batchSize before...");
                        log.info(baseQuery.toString());
                        cursor = collection.find(baseQuery).iterator();
                        log.info("query batchSize after...");
                    }
                }

                log.info("enter while before .");
                int cursorSize = 0;
                while (cursor != null && cursor.hasNext()) {
                    //数据源部分改成了中文key,需要做处理
                    Document document = trickDoc(typeQuery.getCollection(), cursor.next());
                    if (!companySet.contains(companyName)) {
                        companySet.add(companyName);
                    }
                    //写内容

                    if (typeName.equals("工商变更") || typeName.equals("分支机构")
                            || typeName.equals("商标") || typeName.equals("著作权") || typeName.equals("软件著作权")
                            || typeName.equals("高管人员")) {
                        List<List<String>> datas = prepareDatas(querys, document, typeName, companyName);
                        for (List<String> rowData : datas) {
                            Row row = sheet.createRow(rowIndex);
                            fillCell(rowData, row);
                            rowIndex += 1;
                        }
                    } else {
                        List<String> rowData = prepareData(typeName, querys, document, companyName);
                        Row row = sheet.createRow(rowIndex);
                        fillCell(rowData, row);
                        rowIndex += 1;
                    }
                    log.info("rowIndex {} ", rowIndex);
                    cursorSize += 1;
                    if (cursorSize >= 1000) {
                        //防止查询出来的数据太大
                        log.warn("cursor contains so many rows, please check this company {}", companyName);
                        break;
                    }
                }
                log.info("enter while after .");
            }

            log.info("wb.write(outputStream);");
            try {
                wb.write(outputStream);
                //将多个excel压缩
                zipOutputStream.putNextEntry(new ZipEntry(excelName));
                zipOutputStream.write(outputStream.toByteArray());
            } catch (IOException e) {
                log.error("{}", e);
                taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
            } finally {
                try {
                    wb.close();
                } catch (IOException e) {
                    log.error("{}", e);
                }
            }
            log.info("wb.write(outputStream) after");
        }
        try {
            zipOutputStream.close();
        } catch (IOException e) {
            log.error("{}", e);
        }

        log.info("String fileName = genFileName(task);");
        String fileName = genFileName(task);
        String fileDownLoadUrl = fileUploadService.saveFile(bos.toByteArray(), UPLOAD_DIR, fileName);

        log.info("String fileName = genFileName(task); after");

        //更新job
        GridFSDBFile file = fileUploadService.getFile(fileDownLoadUrl);

        taskRepo.finish(task.getId(), fileDownLoadUrl, file == null ? 0 : file.getLength());

        return fileDownLoadUrl;
    }

    private Document trickDoc(String collection, Document document) {
        if (collection.equals(AppDataCollections.COLL_SSGS_BASEINFO)) {
            Map<String, String> confMap = ListingConf.getSsgsBaseInfoMap();
            if (document.get("brief") != null) {
                Map<String, Object> brief = (Map<String, Object>) document.get("brief");
                brief.keySet().stream().filter(key -> confMap.keySet().contains(key)).forEach(key -> {
                    document.put(confMap.get(key), brief.get(key));
                });
            }

            if (document.get("latest") != null) {
                Map<String, Object> latest = (Map<String, Object>) document.get("latest");
                latest.keySet().stream().filter(key -> confMap.keySet().contains(key)).forEach(key -> {
                    document.put(confMap.get(key), latest.get(key));
                });
            }
            document.remove("brief");
            document.remove("latest");
        } else if (collection.equals(AppDataCollections.COLL_COMPANY_ABILITY) ||
                collection.equals(AppDataCollections.COLL_CASH_FLOW) ||
                collection.equals(AppDataCollections.COLL_PROFIT) ||
                collection.equals(AppDataCollections.COLL_ASSETS_LIABILITY)) {
            Map<String, String> caiBaoConfMap = ListingConf.getCaiBaoMap();
            if (document.get("data_info") != null) {
                Map<String, Object> data = (Map<String, Object>) document.get("data_info");
                data.keySet().stream().filter(key -> caiBaoConfMap.keySet().contains(key.trim())).forEach(key -> {
                    document.put(caiBaoConfMap.get(key.trim()), data.get(key));
                });
                document.remove("data_info");
            }
        }
        return document;
    }

    private void fillCell(List<String> rowData, Row row) {
        if (rowData != null) {
            for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                if (rowData.get(colIndex) != null) {
                    if (rowData.get(colIndex).trim().length() >= 32765) {
                        row.createCell(colIndex).setCellValue(rowData.get(colIndex).trim().substring(0, 32764));
                        log.info(rowData.get(colIndex).trim());
                    } else {
                        row.createCell(colIndex).setCellValue(rowData.get(colIndex).trim());
                    }
                }
            }
        }
    }

    BasicDBObject basicQuery(Task task, TypeQuery typeQuery) {
        BasicDBObject filter;
        String timeParam;
        if (Strings.isNullOrEmpty(typeQuery.getExtraFilter())){
            filter = new BasicDBObject();
        } else{
            //有额外附加条件，则将附加条件先加上
            filter = BasicDBObject.parse(typeQuery.getExtraFilter());
        }
        if (task.getTimeOption().equals(TimeOption.DATA_TIME.getCode())) {
            if (!Strings.isNullOrEmpty(typeQuery.getTimeParam())) {
                timeParam = typeQuery.getTimeParam();
            } else {
                return filter;
            }
        } else if (task.getTimeOption().equals(TimeOption.IN_TIME.getCode())) {
            timeParam = "_in_time";
        } else {
            return filter;
        }
        filter.put(timeParam, new BasicDBObject(MongoOptions.GTE, DateUtils.FORMAT_DAY.format(task.getBeginDate()))
                .append(MongoOptions.LT, DateUtils.FORMAT_DAY.format(task.getEndDate())));
        LogicDeleteUtil.addDeleteFilter(filter);
        return filter;
    }

    private List<String> prepareData(String typeName, List<Map<String, Object>> querys, Document mongoData, String company) {
        List<String> result = Lists.newArrayList();
        Pair<String, String> riskMarket = null;
        if (mongoData.get("company") != null) {
            //TODO 暂时只有工商的需要风险等级这个字段,而公司名对应的是company,其他的话看公司名的字段有没有改
            riskMarket = getRiskAndMarketCoefficient(mongoData.get("company").toString());
        }
        for (Map<String, Object> query : querys) {
            Object finalData = null;
            Object value = query.get("value");
            if (query.get("key").equals("关联企业") && !Strings.isNullOrEmpty(company)) {
                finalData = company;
            } else if (query.get("key").equals("风险等级")) {
                if (riskMarket != null) {
                    finalData = riskMarket.getLeft();
                }
            } else if (query.get("key").equals("营销等级")) {
                if (riskMarket != null) {
                    finalData = riskMarket.getRight();
                }
            } else {
                if (value instanceof List && ((List) value).size() > 0) {
                    List<Object> values = ((List) value);
                    if (values.get(0).equals("changerecords")) {
                        continue;
                    } else if (values.size() == 1) {
                        if (mongoData.keySet().contains(values.get(0))) {
                            finalData = mongoData.get(values.get(0));
                        } else {
                            finalData = "";
                        }
                    } else if (mongoData.keySet().contains(values.get(0))) {
                        finalData = mongoData.get(values.get(0));
                        for (Object val : values) {
                            if (val instanceof Integer && finalData instanceof List && ((List) finalData).size() > 1) {
                                finalData = ((List) finalData).get((Integer) val);
                            } else if (finalData instanceof Map && ((Map) finalData).keySet().contains(val)) {
                                finalData = ((Map) finalData).get(val);
                            } else if (val instanceof String && finalData instanceof List) {
                                for (int i = 0; i < ((List) finalData).size(); i++) {
                                    if (((List) finalData).get(i) instanceof Map && ((Map) ((List) finalData).get(i)).keySet().contains(val)) {
                                        finalData = ((Map) ((List) finalData).get(i)).get(val);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    finalData = value;
                }

                if (finalData instanceof List) {
                    finalData = dataFormat((List) finalData, query);
                }

                if (finalData instanceof String) {
                    finalData = ((String) finalData).replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ");
                }

                if (typeName.equals("裁判文书") && finalData instanceof String) {
                    if (query.get("key").equals("url")) {
                        finalData = replaceJudgementUrl((String) finalData);
                    }
                }

            }
            result.add(finalData == null ? "" : finalData.toString());
        }
        if (typeName.equals("融资事件")) {
            if (mongoData.keySet().contains("enterprise_full_name") && mongoData.keySet().contains("project_name")) {
                if (mongoData.get("enterprise_full_name").equals(mongoData.get("project_name"))) {
                    result.add("是");
                } else {
                    result.add("否");
                }
            }
        }
        return result;
    }

    private List<List<String>> prepareDatas(List<Map<String, Object>> querys, Document mongoData, String typeName, String company) {
        Map<String, String> base = Maps.newHashMap();
        List<List<String>> result = Lists.newArrayList();
        Pair<String, String> riskMarket = null;
        if (mongoData.get("company") != null) {
            //TODO 暂时只有工商的需要风险等级这个字段,而公司名对应的是company,其他的话看公司名的字段有没有改
            riskMarket = getRiskAndMarketCoefficient(mongoData.get("company").toString());
        }

        for (Map<String, Object> query : querys) {
            Object finalData = null;
            Object value = query.get("value");
            if (query.get("key").equals("关联企业") && !Strings.isNullOrEmpty(company)) {
                finalData = company;
            } else if (query.get("key").equals("风险等级")) {
                if (riskMarket != null) {
                    finalData = riskMarket.getLeft();
                }
            } else if (query.get("key").equals("营销等级")) {
                if (riskMarket != null) {
                    finalData = riskMarket.getRight();
                }
            } else {
                if (value instanceof List && ((List) value).size() > 0) {
                    List<Object> values = ((List) value);
                    if (values.get(0).equals("changerecords")
                            || values.get(0).equals("branch") || values.get(0).equals("trademark")
                            || values.get(0).equals("copyright") || values.get(0).equals("software_copyright")
                            || values.get(0).equals("executives")) {
                        continue;
                    } else if (values.size() == 1) {
                        if (mongoData.keySet().contains(values.get(0))) {
                            finalData = mongoData.get(values.get(0));
                        } else {
                            finalData = "";
                        }
                    } else if (mongoData.keySet().contains(values.get(0))) {
                        finalData = mongoData.get(values.get(0));
                        for (Object val : values) {
                            if (val instanceof Integer && finalData instanceof List && ((List) finalData).size() > 1) {
                                finalData = ((List) finalData).get((Integer) val);
                            } else if (finalData instanceof Map && ((Map) finalData).keySet().contains(val)) {
                                finalData = ((Map) finalData).get(val);
                            } else if (val instanceof String && finalData instanceof List) {
                                for (int i = 0; i < ((List) finalData).size(); i++) {
                                    if (((List) finalData).get(i) instanceof Map && ((Map) ((List) finalData).get(i)).keySet().contains(val)) {
                                        finalData = ((Map) ((List) finalData).get(i)).get(val);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    finalData = value;
                }

                if (finalData instanceof List) {
                    finalData = dataFormat((List) finalData, query);
                }

                if (finalData instanceof String) {
                    finalData = ((String) finalData).replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ");
                }
            }
            base.put((String) query.get("key"), finalData == null ? "" : finalData.toString());
        }

        if (typeName.equals("工商变更") || typeName.equals("商标") || typeName.equals("著作权") || typeName.equals("软件著作权")
                || typeName.equals("高管人员") || typeName.equals("高管人员")) {
            List<Map<String, Object>> subDataList = Lists.newArrayList();
            for (Map<String, Object> query : querys) {
                if (query.get("value") != null && query.get("value") instanceof List) {
                    List<Object> valueList = ((List) query.get("value"));
                    if (valueList.size() == 2) {
                        if (mongoData.get(valueList.get(0).toString()) != null) {
                            subDataList = (List<Map<String, Object>>) mongoData.get(valueList.get(0));
                            break;
                        }
                    }
                }
            }
            if (subDataList.size() > 0) {
                for (Map<String, Object> map : subDataList) {
                    List<String> formatResultList = Lists.newArrayList();
                    for (Map<String, Object> query : querys) {
                        if (query.get("value") != null && query.get("value") instanceof List) {
                            List<Object> valueList = ((List) query.get("value"));
                            if (valueList.size() == 2) {
                                base.put(query.get("key").toString(), blankFormat(String.valueOf(map.get(valueList.get(1)))));
                            }
                        }
                    }
                    for (Map<String, Object> query : querys) {
                        if (base.get(query.get("key")) != null) {
                            formatResultList.add(String.valueOf(base.get(query.get("key"))));
                        }
                    }
                    result.add(formatResultList);

                }
            } else {
                List<String> formatResultList = Lists.newArrayList();

                for (Map<String, Object> query : querys) {
                    base.put(query.get("key").toString(), "");
                }
                for (Map<String, Object> query : querys) {
                    if (base.get(query.get("key")) != null) {
                        formatResultList.add(String.valueOf(base.get(query.get("key"))));
                    }
                }
                result.add(formatResultList);
            }
        } else if (typeName.equals("分支机构")) {
            if (mongoData.get("branch") == null) {
                List<String> formatResultList = Lists.newArrayList();
                base.put("分支机构名称", "");
                base.put("注册号", "");
                base.put("风险等级", "");
                base.put("营销等级", "");
                for (Map<String, Object> query : querys) {
                    if (base.get(String.valueOf(query.get("key"))) != null) {
                        formatResultList.add(String.valueOf(base.get(query.get("key"))));
                    }
                }
                result.add(formatResultList);
            } else {
                List<Map<String, String>> branches = (List<Map<String, String>>) mongoData.get("branch");
                for (Map<String, String> branch : branches) {
                    List<String> formatResultList = Lists.newArrayList();
                    String companyName = Strings.isNullOrEmpty(blankFormat(branch.get("company_name"))) ? blankFormat(branch.get("compay_name")) : "";
                    Pair<String, String> riskMarketSub = Pair.of("", "");
                    if (!Strings.isNullOrEmpty(companyName)) {
                        riskMarketSub = getRiskAndMarketCoefficient(companyName);
                    }
                    base.put("分支机构名称", companyName);
                    base.put("注册号", blankFormat(branch.get("code")));
                    base.put("风险等级", riskMarketSub.getLeft());
                    base.put("营销等级", riskMarketSub.getRight());
                    for (Map<String, Object> query : querys) {
                        if (base.get(String.valueOf(query.get("key"))) != null) {
                            formatResultList.add(String.valueOf(base.get(query.get("key"))));
                        }
                    }
                    result.add(formatResultList);
                }
            }
        }

        return result;
    }

    private String replaceJudgementUrl(String url) {
        return url.replaceAll("http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx",
                "controller://wenshu.court.gov.cn/content/content");
    }

    private String dataFormat(List finalData, Map<String, Object> query) {
        try {
            String dataStr = objectMapper.writeValueAsString(finalData);
            String formatDataStr = dataStr.replaceAll("\"", "").replaceAll(" ", "").replaceAll("},\\{", ";").replaceAll(":", "/")
                    .replaceAll("\\{", "").replaceAll("}", "");
            if (query.keySet().contains("map")) {
                if (query.get("map") instanceof Map) {
                    for (Object key : ((Map) query.get("map")).keySet()) {
                        formatDataStr = formatDataStr.replaceAll(((Map) query.get("map")).get(key).toString(), key.toString());
                    }
                }
            }
            return formatDataStr.substring(1, formatDataStr.length() - 1);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String blankFormat(String str) {
        return str == null ? null : str.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ");
    }

    public String genFileName(Task task) {
        return String.valueOf(task.getUserId()) + "/" + String.valueOf(task.getId()) + "/" + task.getName() + ".zip";
    }

    private Map<String, Object> getEnterpriseOverview(String companyName) {
        Map<String, Object> company = Maps.newHashMap();
        if (!Strings.isNullOrEmpty(companyName)) {
            String id = SecretUtil.md5(companyName);
            company = elasticSearchRepo.getById(indexOfEnterpriseOverview, typeOfEnterpriseOverview, id);
        }
        return company;
    }

    private Pair<String, String> getRiskAndMarketCoefficient(String company) {
        Map<String, Object> overview = getEnterpriseOverview(company);
        String riskCoefficient = "";
        String marketCoefficient = "";
        if (overview != null && overview.get("risk_coefficient") != null && overview.get("risk_coefficient") instanceof Map) {
            if (((Map) overview.get("risk_coefficient")).get("value") != null) {
                riskCoefficient = ((Map) overview.get("risk_coefficient")).get("value").toString();
            }
        }
        if (overview != null && overview.get("market_coefficient") != null && overview.get("market_coefficient") instanceof Map) {
            if (((Map) overview.get("market_coefficient")).get("value") != null) {
                marketCoefficient = ((Map) overview.get("market_coefficient")).get("value").toString();
            }
        }
        return Pair.of(riskCoefficient, marketCoefficient);
    }
}
