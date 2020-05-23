package com.haizhi.iap.follow.utils;

import com.haizhi.iap.common.utils.ConfUtil;
import com.haizhi.iap.follow.service.impl.CustdigServiceImpl;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.net.URLDecoder.decode;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/19 17:27
 */
public class PdfUtils {

    private static BaseFont fontSimhei = null;
    private static BaseFont fontPingfangRegular = null;
    public static final String LOGO = ConfUtil.getAbsolutePath("/images/logo_ht.png");
    public static final String BACK_COVER_BACKGROUND = ConfUtil.getAbsolutePath("/images/back_cover_image.png");
    public static final String SIMHEI_FONT_PATH = "/fonts/simhei.ttf";
    public static final String PINGFANG_FONT_PATH = "/fonts/PingFangRegular.ttf";
    public static final String FONT_ALIAS = "fontSimhei";
    public static Font DEFAULT_FONT = null;
    public static Font SIMHEIFONT39 = null;
    public static Font SIMHEIFONT30 = null;
    public static Font SIMHEIFONT20 = null;
    public static Font SIMHEIFONT18 = null;
    public static Font SIMHEIFONT15 = null;
    public static Font SIMHEIFONT13 = null;
    public static Font SIMHEIFONT11 = null;
    public static Font SIMHEIFONT10 = null;
    public static Font SIMHEIFONT9 = null;
    public static Font SIMHEIFONT8 = null;
    public static Font SIMHEIFONT7 = null;
    public static Font MENUSIMHEIFONT12 = null;
    public static Font MENUSIMHEIFONT20 = null;
    public static BaseColor TBHEADERBKCOLOR = null;

    static {
        init();
    }

    private static void init() {
        try {
            fontSimhei = BaseFont.createFont(SIMHEI_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            fontPingfangRegular = BaseFont.createFont(PINGFANG_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
        BaseColor rgbPointTwo = new BaseColor(0.2f, 0.2f, 0.2f);
        BaseColor rgbPointFF = new BaseColor(0.2f, 0.2f, 0.2f);
        XMLWorkerFontProvider fontImp = new XMLWorkerFontProvider(ConfUtil.getAbsolutePath(SIMHEI_FONT_PATH));
        FontFactory.setFontImp(fontImp);
        FontFactory.register(ConfUtil.getAbsolutePath(SIMHEI_FONT_PATH), FONT_ALIAS);

        TBHEADERBKCOLOR = new BaseColor(0.96f, 0.96f, 0.96f);

        DEFAULT_FONT = FontFactory.getFont(FONT_ALIAS,10,Font.NORMAL,rgbPointFF);
        SIMHEIFONT39 = FontFactory.getFont(FONT_ALIAS,39,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT30 = FontFactory.getFont(FONT_ALIAS,30,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT20 = FontFactory.getFont(FONT_ALIAS,20,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT18 = FontFactory.getFont(FONT_ALIAS,18,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT15 = FontFactory.getFont(FONT_ALIAS,15,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT13 = FontFactory.getFont(FONT_ALIAS,13,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT11 = FontFactory.getFont(FONT_ALIAS,11,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT10 = FontFactory.getFont(FONT_ALIAS,10,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT9 = FontFactory.getFont(FONT_ALIAS,9,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT8 = FontFactory.getFont(FONT_ALIAS,8,Font.NORMAL,rgbPointTwo);
        SIMHEIFONT7 = FontFactory.getFont(FONT_ALIAS,7,Font.NORMAL,rgbPointTwo);

        MENUSIMHEIFONT20 = FontFactory.getFont(FONT_ALIAS,20,Font.BOLDITALIC,rgbPointFF);

    }

    /**
     * 生成首页
     * @param document
     * @param inquirer
     * @param userId
     * @throws DocumentException
     */
    public static void generateHomePage(Document document, String inquirer, String userId) throws DocumentException {
        //占位，不显示
        Paragraph title0 = new Paragraph(" ");
        document.add(title0);

        Image img = null;
        try {
            img = Image.getInstance(LOGO);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadElementException e) {
            e.printStackTrace();
        }
        float pdf_width = PageSize.A4.getWidth();
        int img_width = 250;
        int img_height = 80;
        float img_x = (pdf_width - img_width) / 2;
        img.setAlignment(Image.ALIGN_MIDDLE);
        img.setWidthPercentage(70);
        img.setAbsolutePosition(img_x, 500);
        img.scaleAbsolute(img_width, img_height);
        try {
            document.add(img);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        Paragraph title1 = new Paragraph("供应商（投标人）关联\n关系查询对比报告", SIMHEIFONT30);
//        Paragraph title1 = new Paragraph("企业图谱系统\n企业关联关系查询报告", SIMHEIFONT30);
        title1.setAlignment(Element.ALIGN_CENTER);
        title1.setSpacingBefore(320f);
        document.add(title1);

//        Paragraph title2 = new Paragraph(companyName, simheiFont20);
//        title2.setAlignment(Element.ALIGN_CENTER);
//        title1.setSpacingBefore(28f);
//        document.add(title2);

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timecontent = "报告生成时间  " + format.format(date);
        Paragraph title3 = new Paragraph(timecontent, SIMHEIFONT18);
        title3.setAlignment(Element.ALIGN_RIGHT);
        title3.setSpacingBefore(180f);
        document.add(title3);

        StringBuilder userinfo = new StringBuilder();
        if(!inquirer.equals(CustdigServiceImpl.NONE_USER_NAME)){
            userinfo.append(inquirer);
        }

        if(!userId.equals(CustdigServiceImpl.DEFAULT_USER_ID)){
            userinfo.append(userId);
        }

        if(!userId.equals(CustdigServiceImpl.DEFAULT_USER_ID)){
            Paragraph title4 = new Paragraph("查询人：" + userinfo.toString(), SIMHEIFONT15);
            title4.setAlignment(Element.ALIGN_RIGHT);
            title4.setSpacingBefore(10f);
            document.add(title4);
        }

        String title5content = "（报告内容仅供参考，具体内容请以国家企业信用信息公示系统查询页面为准）";
        Paragraph title5 = new Paragraph(title5content, SIMHEIFONT13);
        title5.setAlignment(Element.ALIGN_RIGHT);
        title5.setSpacingBefore(10f);
        document.add(title5);
    }

    /**
     * 生成末页
     * @param document
     * @param writer
     * @throws IOException
     * @throws DocumentException
     */
    public static void generateLastPage(Document document,PdfWriter writer) throws IOException, DocumentException {
        document.newPage();
        PdfContentByte canvas = writer.getDirectContentUnder();
        Image backImage = null;
        try {
            backImage = Image.getInstance(BACK_COVER_BACKGROUND);
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        backImage.scaleAbsolute(PageSize.A4);
        backImage.setAbsolutePosition(0, 0);
        try {
            canvas.addImage(backImage);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Font pingfangFont14 = new Font(fontPingfangRegular, 14, Font.NORMAL, new BaseColor(0.4f, 0.4f, 0.4f));
        Font pingfangFont9 = new Font(fontPingfangRegular, 9, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        Image logo = Image.getInstance(LOGO);

        logo.setAlignment(Image.ALIGN_MIDDLE);
        logo.setAbsolutePosition(230, 456);
        logo.scaleAbsolute(60, 25);
        document.add(logo);
        Paragraph logoNameLastPage = new Paragraph("企业知识图谱开创者", pingfangFont14);
        logoNameLastPage.setAlignment(Element.ALIGN_CENTER);
        logoNameLastPage.setSpacingBefore(322f);
        logoNameLastPage.setIndentationLeft(131f);
        document.add(logoNameLastPage);

        Paragraph nameLastPage = new Paragraph("北京海致星图科技有限公司", pingfangFont9);
        nameLastPage.setAlignment(Element.ALIGN_CENTER);
        nameLastPage.setSpacingBefore(373f);
        document.add(nameLastPage);

        Paragraph addressLastPage = new Paragraph("北京市海淀区学院路甲5号768创意园B座8号门", pingfangFont9);
        addressLastPage.setAlignment(Element.ALIGN_CENTER);
        document.add(addressLastPage);

        Paragraph teleLastPage = new Paragraph("联系方式:010-61190338", pingfangFont9);
        teleLastPage.setAlignment(Element.ALIGN_CENTER);
        document.add(teleLastPage);
    }

    /**
     * 生成表格
     * @param data
     * @param keys
     * @param col_titles
     * @return
     */
    public static PdfPTable generateTable(List<Map> data, String[] keys, String[] col_titles,float[] colwidths) throws DocumentException {
        int colnum = col_titles.length;
        PdfPTable table = new PdfPTable(colnum);
        table.setWidthPercentage(colwidths,PageSize.A4);
        for (String col_title : col_titles) {
            PdfPCell cell = generateHeaderCell(col_title, PdfPCell.ALIGN_MIDDLE,PdfPCell.ALIGN_CENTER); //单元格垂直居中，水平居中
            table.addCell(cell);
        }
        table.completeRow();
        for (Map map : data) {
            for (String key : keys) {
                Object obj = map.get(key);
                String text = "";
                if(null != obj){
                    text = obj.toString();
                }
                PdfPCell cell = generateCell(text,  PdfPCell.ALIGN_MIDDLE,PdfPCell.ALIGN_LEFT,SIMHEIFONT7);
                table.addCell(cell);
            }
            table.completeRow();
        }
        return table;
    }

    /**
     * 生成表头
     * @param text
     * @param vertAlign
     * @param horiAlign
     * @return
     */
    private static PdfPCell generateHeaderCell(String text, int vertAlign, int horiAlign) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SIMHEIFONT11));
        cell.setPaddingLeft(3);
        cell.setPaddingBottom(4);
        cell.setVerticalAlignment(vertAlign);
        cell.setHorizontalAlignment(horiAlign); //设置水平定位(靠左，居中，靠右)
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setBackgroundColor(TBHEADERBKCOLOR); //背景色为亮灰色
        return cell;
    }

    /**
     * 生成表格
     * @param data
     * @return
     */
    public static PdfPTable generateTextList(List<String> data) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        for (String content : data) {
            PdfPCell cell = generateCell("    " + content,  PdfPCell.ALIGN_MIDDLE,PdfPCell.ALIGN_LEFT,SIMHEIFONT10);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPaddingTop(4);
            cell.setPaddingLeft(10);
            cell.setRightIndent(20f);
            table.addCell(cell);
            table.completeRow();
        }
        return table;
    }

    private static PdfPCell generateCell(String text, int vertAlign, int horiAlign,Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPaddingLeft(3);
//        cell.setPaddingBottom(4);
        cell.setVerticalAlignment(vertAlign);
        cell.setHorizontalAlignment(horiAlign); //设置水平定位(靠左，居中，靠右)
        cell.setBorder(PdfPCell.BOX);
        cell.setLeading(font.getSize(),1f);
        cell.setBorderColor(BaseColor.BLACK);
        return cell;
    }

    // 生成目录页
    public static void generateContentsPage(Document document, int page, ArrayList catalogTitle, ArrayList catalogPage, PdfWriter writer) throws DocumentException {
        document.newPage();
        Paragraph menu = new Paragraph("目录", MENUSIMHEIFONT20);
        menu.setSpacingBefore(20f);
        menu.setSpacingAfter(20f);
        menu.setAlignment(Element.ALIGN_CENTER);
        document.add(menu);
        document.add(new Paragraph(new Chunk("一.企业基本信息", SIMHEIFONT15)));
        for (int i = 0; i < catalogTitle.size(); i++) {
            Paragraph content = new Paragraph(new Chunk(i + 1 + "." + catalogTitle.get(i), SIMHEIFONT13));
            content.add(new Chunk(new DottedLineSeparator()));
            content.add(new Chunk(catalogPage.get(i).toString(), SIMHEIFONT13));
            document.add(content);
        }
        Paragraph pictureTitle = new Paragraph(new Chunk("二.企业知识图谱", SIMHEIFONT15));
        pictureTitle.add(new Chunk(new DottedLineSeparator()));
        pictureTitle.add(new Chunk("" + page, SIMHEIFONT13));
        pictureTitle.setSpacingBefore(20f);
        document.add(pictureTitle);
        PdfContentByte description = writer.getDirectContent();
        description.beginText();
        description.setFontAndSize(fontSimhei, 12);
        description.showTextAligned(Element.ALIGN_BOTTOM, "重要声明", 40, 110, 0);
        description.endText();

        description.beginText();
        description.setFontAndSize(fontSimhei, 9);
        description.showTextAligned(Element.ALIGN_BOTTOM, "1. 本报告内容是海致星图基于公开信息利用大数据分析引擎获取及分析的结果，仅供参考；", 40, 85, 0);
        description.showTextAligned(Element.ALIGN_BOTTOM, "2. 海致星图不对该查询结果的全面、准确、真实性负责，仅为您的决策提供参考；", 40, 70, 0);
        description.showTextAligned(Element.ALIGN_BOTTOM, "3. 因使用本报告而产生的任何后果，海致星图概不负责。", 40, 55, 0);
        description.endText();
        // 页眉和页脚增加
        description.beginText();
        description.setColorFill(new BaseColor(0.41f, 0.41f, 0.41f));
        description.setFontAndSize(fontSimhei, 11);
        description.showTextAligned(PdfContentByte.ALIGN_LEFT, "海致星图企业知识图谱", 25, 20, 0);
        description.endText();
//        description.beginText();
//        description.setFontAndSize(fontSimhei, 11);
//        description.showTextAligned(PdfContentByte.ALIGN_LEFT, "海致关系图谱报告", 25, 805, 0);
//        description.endText();
        //画线header line
//        description.setColorStroke(new BaseColor(0.93f, 0.93f, 0.93f));
//        description.moveTo(25, 798);
//        description.lineTo(570, 798);
//        description.stroke();
        //预留空白
        Paragraph paragraph = new Paragraph(" ");
        paragraph.setSpacingBefore(44);
        document.add(paragraph);
        description.setColorStroke(new BaseColor(0.93f, 0.93f, 0.93f));
        description.moveTo(25, 35);
        description.lineTo(570, 35);
        description.stroke();
    }

    public static void canvasAtlas(Document document, Image image) throws DocumentException, UnsupportedEncodingException {
        float[] cloum = {1};
        PdfPTable box = new PdfPTable(cloum);
        float imgWidth = image.getWidth();
        float imgHeight = image.getHeight();
        float actualWidth = 0;
        if (imgWidth / 595 > imgHeight / 800) {
            actualWidth = 100;
        } else {
            actualWidth = imgWidth * 100 / ((imgHeight / 800) * 595);
        }
        box.setTotalWidth(document.getPageSize().getWidth() * actualWidth / 100 - 60);
        box.setSpacingBefore(22f);
        box.setLockedWidth(true);
        image.setWidthPercentage(actualWidth);
        PdfPCell imageCell = new PdfPCell();
        imageCell.setBorder(0);
        imageCell.addElement(image);
        box.addCell(imageCell);
        document.add(box);
    }

    private static String readCSS() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        InputStream is = XMLWorkerHelper.class.getResourceAsStream("/css/index.css");
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return new String(baos.toByteArray());
    }

    // 二级标题+横线
    public static void addSubTitle(Document document, String string, String numberText)
            throws DocumentException, UnsupportedEncodingException {
        if (string == null) string = "";
        if (string.length() > 10 && !numberText.equals("atlas"))
            string = string.substring(0, 10);
        Paragraph p = new Paragraph();
        StringBuilder sb = new StringBuilder();
        if (!numberText.equals("atlas")) {
            sb.append("<table class=\"contentTable\">");
            sb.append("<tr><td class=\"contentTitle\">" + string + "</td>");
            if(!numberText.equals("")) {
                sb.append("<td class=\"contentText\">" + numberText + "</td>");
            }
        } else {
            sb.append("<table class=\"contentTable\">");
            if(string.equals("")) {
                string = "图谱截图";
                sb.append("<tr><td class=\"contentTitle\">" + string + "</td>");
            } else {
                String[] titles = string.split("%0A");
                sb.append("<tr><td class=\"contentTitle\">");
                for(String title : titles ){
                    sb.append("<p class=\"contentTitleP\">" + decode(title, "UTF-8") + "</p>");
                }
                sb.append("</td>");
            }
        }
        sb.append("</tr></table>");
        ElementList list = null;
        try {
            String css = readCSS();
            list = XMLWorkerHelper.parseToElementList(new String(sb.toString().getBytes("UTF-8")), css);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Element element : list) {
            p.add(element);
        }
        p.setSpacingBefore(5f);
        document.add(p);
        Paragraph pBlank = new Paragraph();
        pBlank.setIndentationLeft(-7f);
        pBlank.setIndentationRight(-7f);
        LineSeparator line = new LineSeparator();
        line.setLineColor(new BaseColor(0.87f, 0.87f, 0.87f));
        line.setOffset(5);
        pBlank.add(line);
        document.add(pBlank);
    }

    public static PdfPTable getChpterTitleTable(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"width: 3px; height: 28px; background-color: #5397E9;\">");
        sb.append("\n</div>");
        float[] columnWidths = {1, 50};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(105f);
        // 第一个cell
        PdfPCell cell1 = new PdfPCell();
        cell1.setUseVariableBorders(true);
        cell1.setBorder(PdfPCell.NO_BORDER);
        ElementList list = null;
        try {
            list = XMLWorkerHelper.parseToElementList(sb.toString(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Element element : list) {
            cell1.addElement(element);
        }
        cell1.setPaddingLeft(6f);
        table.addCell(cell1);

        Font myTitleFont = FontFactory.getFont(FONT_ALIAS,22,Font.NORMAL,new BaseColor(0.33f, 0.59f, 0.91f));
        PdfPCell cell2 = new PdfPCell(new Phrase(title, myTitleFont));
        cell2.setPadding(0);
        cell2.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell2.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell2);
        table.setSpacingBefore(40f);
        table.setSpacingAfter(0);
        return table;
    }

    // 封面底部表格单元格
    private static PdfPCell getCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SIMHEIFONT8));
        cell.setPadding(1);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
}
