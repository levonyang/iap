package com.haizhi.iap.search.service;

import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.conf.FinancialReportItem;
import com.haizhi.iap.search.exception.SearchException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

/**
 * Created by chenbo on 17/2/20.
 */
@Slf4j
public class FRPageEventHelper extends PdfPageEventHelper {

    URL logo;

    String filename;

    String company;

    FinancialReportItem exportItem;

    public FRPageEventHelper(URL logo, String company, FinancialReportItem exportItem) {
        this.logo = logo;
        this.company = company;
        this.exportItem = exportItem;
    }

    public FRPageEventHelper(String filename, String company, FinancialReportItem exportItem) {
        this.filename = filename;
        this.company = company;
        this.exportItem = exportItem;
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try {
            //add Chinese font
            BaseFont bfChinese = BaseFont.createFont("/fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            PdfContentByte cb = writer.getDirectContent();

            //create title image
            Image jpg;
            if (logo == null) {
                jpg = Image.getInstance(filename);
            } else {
                jpg = Image.getInstance(logo);
            }
            jpg.setAlignment(Image.ALIGN_LEFT);
            jpg.setAbsolutePosition(25, 796);
            jpg.scaleAbsolute(82, 20);
            document.add(jpg);

            //rightString

            cb.beginText();
            cb.setFontAndSize(bfChinese, 11);
            cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, company + "--" + exportItem.getItem(), 570, 805, 0);
            cb.endText();

            cb.beginText();
            cb.setFontAndSize(bfChinese, 7);
            cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, exportItem.getYearQuarter() + "报告期(单位: 人民币元)", 570, 796, 0);
            cb.endText();

            //画线header line
            cb.setColorStroke(new BaseColor(0.93f, 0.93f, 0.93f));
            cb.moveTo(25, 785);
            cb.lineTo(570, 785);
            cb.stroke();
            //预留空白
            Paragraph paragraph = new Paragraph("");
            paragraph.setSpacingBefore(39);
            document.add(paragraph);

            cb.setColorStroke(new BaseColor(0.93f, 0.93f, 0.93f));
            cb.moveTo(25, 35);
            cb.lineTo(570, 35);
            cb.stroke();

        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.PDF_GENERATE_ERROR);
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            //footer
            //add Chinese font
            BaseFont bfChinese = BaseFont.createFont("/fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            PdfContentByte cb = writer.getDirectContent();

            cb.beginText();
            cb.setColorFill(new BaseColor(0.41f, 0.41f, 0.41f));
            cb.setFontAndSize(bfChinese, 7);
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "海致星图企业图谱", 25, 22, 0);
            cb.endText();

            cb.beginText();
            cb.setColorFill(new BaseColor(0.41f, 0.41f, 0.41f));
            cb.setFontAndSize(bfChinese, 7);
            cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, exportItem.getItem() + " " + writer.getPageNumber(), 570, 22, 0);
            cb.endText();
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.PDF_GENERATE_ERROR);
        }
    }
}
