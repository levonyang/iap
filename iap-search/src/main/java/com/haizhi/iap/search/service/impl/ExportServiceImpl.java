package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.ConfUtil;
import com.haizhi.iap.search.conf.*;
import com.haizhi.iap.search.enums.FinancialReportField;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.service.ExportService;
import com.haizhi.iap.search.service.FRPageEventHelper;
import com.haizhi.iap.common.utils.LogicDeleteUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.Map;

/**
 * Created by chenbo on 17/2/18.
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {
    @Setter
    @Value("${logo}")
    String logoName;

    @Qualifier("appMongoDatabase")
    @Autowired
    MongoDatabase appMongoDatabase;

    @Override
    public OutputStream exportFinancialReport(OutputStream outputStream, String type,
                                              String stockCode, String yearQuarter, String financialTypeInZh, String company) {

        //处理下财报类型
        String year = "";
        // if(yearQuarter.length() > 4){
        //    year = yearQuarter.substring(0, 4);
        //}

        BasicDBObject filter = new BasicDBObject();
        filter.put("code", stockCode);
        BasicDBList yearMonths = new BasicDBList();
        //兼容数据源year_month不同处理,公司综合能力指标里面是'2016三季',而资产负债表里面是2016-09-30
        yearMonths.add(yearQuarter);
        yearMonths.add(year + financialTypeInZh);

        //filter.put("year_month", new BasicDBObject("$in", yearMonths));
        filter.put("publish_time", yearQuarter + " 00:00:00");
        filter.put("caibao_type", financialTypeInZh);
        LogicDeleteUtil.addDeleteFilter(filter);

        String coll;
        FinancialReportItem exportItem;
        if (type.equals(FinancialReportField.COMPANY_ABILITY.getName())) {
            coll = AppDataCollections.COLL_COMPANY_ABILITY;
            exportItem = ListedCompanyFormat.getCompanyAbility();
        } else if (type.equals(FinancialReportField.CASH_FLOW.getName())) {
            coll = AppDataCollections.COLL_CASH_FLOW;
            exportItem = ListedCompanyFormat.getCashFlow();
        } else if (type.equals(FinancialReportField.ASSETS_LIABILITY.getName())) {
            coll = AppDataCollections.COLL_ASSETS_LIABILITY;
            exportItem = ListedCompanyFormat.getAssetsLiability();
        } else if (type.equals(FinancialReportField.PROFIT.getName())) {
            coll = AppDataCollections.COLL_PROFIT;
            exportItem = ListedCompanyFormat.getProfit();
        } else {
            throw new ServiceAccessException(SearchException.WRONG_FINANCIAL_REPORT_TYPE);
        }
        MongoCollection<org.bson.Document> collection = appMongoDatabase.getCollection(coll);
        MongoCursor<org.bson.Document> cursor = collection.find(filter).iterator();
        org.bson.Document doc;
        if (cursor.hasNext()) {
            doc = cursor.next();
            //抹平数据源中文key对前端的影响
            Map<String, String> caiBaoConfMap = ListingConf.getCaiBaoMap();
            trickCaiBaoDoc(doc, caiBaoConfMap);
        } else {
            throw new ServiceAccessException(SearchException.UN_CRAW_DATA);
        }

        exportItem.setCompany(company);
        exportItem.setYearQuarter(year + financialTypeInZh);

        try {
            Document document = new Document(PageSize.A4);

            //add Chinese font
            BaseFont bfChinese = BaseFont.createFont("/fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            //Create Writer associated with document
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setPageEvent(new FRPageEventHelper(ConfUtil.getAbsolutePath("/images/" + logoName), company, exportItem));
            document.open();

            //报表
            for (FRItemData data : exportItem.getData()) {
                //标题
                Paragraph title = new Paragraph(data.getTitle(), new Font(bfChinese, 10, Font.NORMAL, new BaseColor(0.45f, 0.45f, 0.45f)));
                title.setSpacingBefore(1);
                title.setSpacingAfter(9);
                document.add(title);

                //表格
                PdfPTable table = new PdfPTable(2);
                table.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
                table.setSpacingBefore(1);
                table.setSpacingAfter(30);
                table.setWidthPercentage(100);

                for (FRItemDataListData listData : data.getList()) {
                    Float cellHeight = 16f;
                    if (listData.getChineseName().length() > 20) {
                        cellHeight = 28f;
                    }

                    if (doc.get(listData.getName()) != null) {
                        if (doc.get(listData.getName()) instanceof Float) {
                            listData.setValue(String.format("%.2f", doc.get(listData.getName())));
                        } else {
                            listData.setValue(doc.get(listData.getName()));
                        }
                    }

                    Paragraph leftText = new Paragraph(listData.getChineseName(), new Font(bfChinese, 8, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f)));
                    leftText.setIndentationRight(9);
                    PdfPCell leftCell = new PdfPCell(leftText);
                    leftCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    leftCell.setFixedHeight(cellHeight);
                    leftCell.setRightIndent(9);
                    leftCell.setBorderColor(new BaseColor(0.82f, 0.82f, 0.82f));
                    table.addCell(leftCell);

                    Paragraph rightText = new Paragraph(listData.getValue().toString(), new Font(bfChinese, 8, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f)));
                    rightText.setIndentationRight(9);
                    PdfPCell rightCell = new PdfPCell(rightText);
                    rightCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    rightCell.setFixedHeight(cellHeight);
                    rightCell.setIndent(9);
                    rightCell.setBorderColor(new BaseColor(0.82f, 0.82f, 0.82f));
                    table.addCell(rightCell);
                }
                document.add(table);
            }

            document.close();
        } catch (Exception e) {
            log.error("{}", e);
            throw new ServiceAccessException(SearchException.PDF_GENERATE_ERROR);
        }

        return outputStream;
    }

    public Map<String, Object> trickCaiBaoDoc(org.bson.Document doc, Map<String, String> trickConf) {
        if (doc.get("data_info") != null) {
            Map<String, Object> data = (Map<String, Object>) doc.get("data_info");
            data.keySet().stream().filter(key -> trickConf.keySet().contains(key.trim())).forEach(key -> {
                doc.put(trickConf.get(key.trim()), data.get(key));
            });
            doc.remove("data_info");
        }
        return doc;
    }
}
