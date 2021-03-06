package com.haizhi.iap.follow.service;

import com.haizhi.iap.common.utils.ConfUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by zhutianpeng on 17/9/24.
 */
@Slf4j
public class CustdigPageEventHelper extends PdfPageEventHelper {
    public static final String BACK_IMAGE = ConfUtil.getAbsolutePath("/images/cover_image.png");
    public static final String LOGO = ConfUtil.getAbsolutePath("/images/logo_ht.png");

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        int pagenumber = writer.getPageNumber();
        document.getPageNumber();
        // 第一页即封面加背景，logo，时间,其他页
        if (pagenumber == 1 || pagenumber < 2) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Image image = null;
            try {
                image = Image.getInstance(BACK_IMAGE);
            } catch (BadElementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            image.scaleAbsolute(PageSize.A4);
            image.setAbsolutePosition(0, 0);
            try {
                canvas.addImage(image);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        } else {
            // 预留空白
            document.setMargins(30,30,30,50);
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(new BaseColor(0.93f, 0.93f, 0.93f));
            cb.moveTo(23, 35);
            cb.lineTo(571, 35);
            cb.stroke();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        int pagenumber = writer.getPageNumber();
        if (pagenumber > 1) {
            try {
                //add Chinese font
                BaseFont bfChinese = BaseFont.createFont("/fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                PdfContentByte cb = writer.getDirectContent();

                cb.beginText();
                cb.setColorFill(new BaseColor(0.41f, 0.41f, 0.41f));
                cb.setFontAndSize(bfChinese, 11);
                cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "华泰证券", 25, 20, 0);
                cb.endText();

                cb.beginText();
                cb.setColorFill(new BaseColor(0.41f, 0.41f, 0.41f));
                cb.setFontAndSize(bfChinese, 11);
                String pageNumString;
                Integer pageNum = writer.getPageNumber() - 1;
                if (pageNum < 10) {
                    pageNumString = "0" + pageNum;
                } else {
                    pageNumString = "" + pageNum;
                }
                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, pageNumString, 570, 20, 0);
                cb.endText();
            } catch (Exception ex) {
                log.error("{}", ex);
            }
        }
    }
}
