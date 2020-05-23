package com.haizhi.iap.follow;

import com.alibaba.fastjson.JSONArray;
import com.haizhi.iap.common.utils.ConfUtil;
import com.haizhi.iap.follow.service.CustdigPageEventHelper;
import com.haizhi.iap.follow.service.PDFExportProcess;
import com.haizhi.iap.follow.service.PDFReportPageEventHelper;
import com.haizhi.iap.follow.utils.PdfUtils;
import com.haizhi.iap.follow.utils.UrlUtils;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.net.URLDecoder.decode;

@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ExportTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    PDFExportProcess pdfExportProcess;

    @Test
    public void process() {

        pdfExportProcess.process(15l);

    }

    private static BaseFont fontSimhei = null;
    private static BaseFont fontPingfangRegular = null;
    public static final String LOGO = ConfUtil.getAbsolutePath("/images/logo_ht.png");
    public static final String BACK_COVER_BACKGROUND = ConfUtil.getAbsolutePath("/images/back_cover_image.png");
    public static final String SIMHEI_FONT_PATH = "/fonts/simhei.ttf";
    public static final String PINGFANG_FONT_PATH = "/fonts/PingFangRegular.ttf";
    private static Font myFont = null;
    private static Font simheiFont39 = null;
    private static Font simheiFont30 = null;
    private static Font simheiFont20 = null;
    private static Font simheiFont18 = null;
    private static Font simheiFont15 = null;
    private static Font simheiFont13 = null;
    private static Font simheiFont9 = null;
    private static Font menuSimheiFont20 = null;

    public static void main(String[] args) throws DocumentException, IOException {
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
        String alias = "fontSimhei";
        FontFactory.register(ConfUtil.getAbsolutePath(SIMHEI_FONT_PATH), alias);

        myFont = FontFactory.getFont(alias,10,Font.NORMAL,rgbPointFF);
        simheiFont39 = FontFactory.getFont(alias,39,Font.NORMAL,rgbPointTwo);
        simheiFont30 = FontFactory.getFont(alias,30,Font.NORMAL,rgbPointTwo);
        simheiFont20 = FontFactory.getFont(alias,20,Font.NORMAL,rgbPointTwo);
        simheiFont18 = FontFactory.getFont(alias,18,Font.NORMAL,rgbPointTwo);
        simheiFont15 = FontFactory.getFont(alias,15,Font.NORMAL,rgbPointTwo);
        simheiFont13 = FontFactory.getFont(alias,13,Font.NORMAL,rgbPointTwo);
        simheiFont9 = FontFactory.getFont(alias,9,Font.NORMAL,rgbPointTwo);
        menuSimheiFont20 = FontFactory.getFont(alias,20,Font.BOLDITALIC,rgbPointFF);

        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, output);
        writer.setPageEvent(new CustdigPageEventHelper());
        document.open();
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

//        Paragraph title1 = new Paragraph("企业图谱系统\n企业关联关系查询报告", simheiFont30);
        Paragraph title1 = new Paragraph("供应商（投标人）关联\n关系查询对比报告", simheiFont30);
        title1.setAlignment(Element.ALIGN_CENTER);
        title1.setSpacingBefore(320f);
        document.add(title1);

//        Paragraph title2 = new Paragraph(companyName, simheiFont20);
//        title2.setAlignment(Element.ALIGN_CENTER);
//        title1.setSpacingBefore(28f);
//        document.add(title2);

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String timecontent = "报告生成时间  " + format.format(date);
        Paragraph title3 = new Paragraph(timecontent, simheiFont18);
        title3.setAlignment(Element.ALIGN_RIGHT);
        title3.setSpacingBefore(180f);
        document.add(title3);

        String userId = "014210";
        Paragraph title4 = new Paragraph("查询人员工号：" + userId, simheiFont18);
        title4.setAlignment(Element.ALIGN_RIGHT);
        title4.setSpacingBefore(10f);
        document.add(title4);

        String username = "刘园";
        Paragraph title4_1 = new Paragraph("查询人：" + username, simheiFont18);
        title4_1.setAlignment(Element.ALIGN_RIGHT);
        title4_1.setSpacingBefore(10f);
        document.add(title4_1);

        String title5content = "（报告内容仅供参考，具体内容请以国家企业信用信息公示系统查询页面为准）";
        Paragraph title5 = new Paragraph(title5content, simheiFont13);
        title5.setAlignment(Element.ALIGN_RIGHT);
        title5.setSpacingBefore(10f);
        document.add(title5);

        //create title image
//        Image img = null;
//        try {
//            img = Image.getInstance(LOGO);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        img.setAlignment(Image.ALIGN_MIDDLE);
//        img.setAbsolutePosition(267, 116);
//        img.scaleAbsolute(60, 25);
//        document.add(img);

//        Paragraph bottomTitle1 = new Paragraph("北京海致星图科技有限公司", simheiFont15);
//        bottomTitle1.setSpacingBefore(508f);
//        bottomTitle1.setSpacingAfter(10f);
//        bottomTitle1.setAlignment(Element.ALIGN_CENTER);
//        document.add(bottomTitle1);
//
//        PdfPTable table = new PdfPTable(3);
//        table.setWidthPercentage(100);
//        table.addCell(getCell("深圳市南山区学府路5号芒果网大厦905", PdfPCell.ALIGN_LEFT));
//        table.addCell(getCell("北京市海淀区学院路甲5号768创意园B座8号门", PdfPCell.ALIGN_CENTER));
//        table.addCell(getCell("上海市静安区武宁南路1号越商大厦2103室", PdfPCell.ALIGN_RIGHT));
//        document.add(table);
//
//        Paragraph bottomTitle3 = new Paragraph("联系方式:010-61190338", simheiFont9);
//        bottomTitle3.setAlignment(Element.ALIGN_CENTER);
//        document.add(bottomTitle3);


//        ArrayList catalogTitle = new ArrayList();
//        ArrayList catalogPage = new ArrayList();
//        catalogTitle.add("测试1");
//        catalogPage.add(writer.getPageNumber() - 3);
//        catalogTitle.add("测试2");
//        catalogPage.add(writer.getPageNumber() - 4);
//        generateContentsPage(document,writer.getPageNumber() - 2,catalogTitle, catalogPage,writer);


//        // 图谱部分
        document.newPage();
        PdfPTable promtTable = PdfUtils.getChpterTitleTable("摘要");
        document.add(promtTable);
        String content = "    经查询：上传公司列表具备如下关联关系，测试公司1和测试公司2共同持股测试公司3，持股20%；测试人1和测试人2同时担任测试公司4董事职位；测试公司1有两个及以上股东在测试公司2任职。";
        Paragraph promttitle = new Paragraph(content, simheiFont13);
        promttitle.setAlignment(Element.ALIGN_LEFT);
        promttitle.setSpacingBefore(20f);
        document.add(promttitle);

        document.newPage();
        PdfPTable chpterTitleTable = PdfUtils.getChpterTitleTable("一、查询结果");
        document.add(chpterTitleTable);

        Paragraph retitle = new Paragraph("经查询：上传公司列表的结果如下：", simheiFont13);
        retitle.setAlignment(Element.ALIGN_LEFT);
        retitle.setSpacingBefore(20f);
        document.add(retitle);

        String urlpath = "http://localhost:8888/image/test.svg?params={\"batchid\":\"111\"}";
//        InputStream inputStream = UrlUtils.getForInputStream(urlpath);
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.STATIC);
        GVTBuilder builder = new GVTBuilder();
        PdfContentByte directContent = writer.getDirectContent();
        float width = PageSize.A4.getWidth() * 1.1f;
        float height = 600f;
        PdfTemplate template = directContent.createTemplate(width, height); //生成awt Graphics2D
        Graphics2D g2d = new PdfGraphics2D(template, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        InputStream inputStream = new URL(urlpath).openStream();
        InputStreamReader isReader = new InputStreamReader(inputStream,"UTF-8");
        BufferedReader reader = new BufferedReader(isReader);
        String str = null;
        StringBuilder sb = new StringBuilder();
        while((str = reader.readLine()) != null){
            sb.append(str);
        }
        String svgcontent = sb.toString();
        if(svgcontent.indexOf("xmlns:xlink") == -1){
            svgcontent = svgcontent.replace(" xmlns="," xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=");
        }

        svgcontent = svgcontent.replaceAll(" href="," xlink:href=");
        byte[] bytes = svgcontent.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        SVGDocument svgDocument = factory.createSVGDocument(urlpath,byteArrayInputStream);
        GraphicsNode graphNode = builder.build(ctx, svgDocument); //画svg到画布
        graphNode.paint(g2d);
        g2d.dispose(); //生成Img
        ImgTemplate img1 = new ImgTemplate(template);
//        URL url = new URL(urlpath);
//        Image image = Image.getInstance(url);
//        Image image = Image.getInstance(bytes);
//        Image image = Image.getInstance("D:\\Desert.jpg");

        canvasAtlas(document,img1,"");

        //上传企业信息部分
//        document.newPage();
        PdfPTable uploadEntTitle = PdfUtils.getChpterTitleTable("二、上传公司列表");
        document.add(uploadEntTitle);
//        addSubTitle(document, "企业列表", "atlas");
        String json = "[{\"entname\":\"测试公司\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司2\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司3\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司4\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司5\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司6\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司7\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司8\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司9\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"},{\"entname\":\"测试公司10\",\"uniscid\":\"2343475396749834329482\",\"legal_man\":\"测试人\",\"legal_id\":\"124436876888676574564\",\"legal_other_custname\":\"测试公司1，测试公司2，测试公司3\",\"name\":\"测试人1\",\"actual_controller\":\"测试人2\",\"altimate_beneficiary\":\"测试人3\",\"conprop\":0.5,\"subconam\":2000000.23,\"rgtered_tel\":\"242343453534\",\"rgtered_adress\":\"测试省测试市测试区测试地址\",\"rgtered_email\":\"2343432423@qq.com\"}]";
        List<Map> data = (List<Map>) JSONArray.parse(json);
        for (Map map : data) {
            String legal_man = (String) map.get("legal_man"); //法人姓名
            String legal_id = (String) map.get("legal_id"); //法人身份证号
            String legal_other_custname = (String) map.get("legal_other_custname"); //法人名下其他企业
            String legal_info = new StringBuilder().append("姓名:").append(legal_man).append("\n")
                    .append("身份证:").append(legal_id).append("\n")
                    .append("名下企业:").append(legal_other_custname).toString();
            map.put("legal_info",legal_info); //法定代表人信息

            String conprop = map.get("conprop").toString(); //股东持股比例
            String subconam = (String) map.get("subconam").toString(); //股东认缴出资额
            String coninfo = new StringBuilder().append("持股比例:").append(conprop).append("\n").append("认缴出资:").append(subconam).toString();
            map.put("coninfo",coninfo);

            String rgtered_tel = (String)map.get("rgtered_tel"); //注册电话
            String rgtered_adress = (String)map.get("rgtered_adress"); //注册地址
            String rgtered_email = (String)map.get("rgtered_email"); //注册邮箱
            String rgtered_info = new StringBuilder().append("电话:").append(rgtered_tel).append("\n")
                    .append("地址:").append(rgtered_adress).append("\n")
                    .append("邮箱:").append(rgtered_email).toString();
            map.put("rgtered_info",rgtered_info);
        }
        String[] col_titles = new String[]{
                "公司名称","统一社会\n信用代码",
                "法人信息",
                "高管","实际\n控制人","最终\n受益人",
                "股东统计分析","注册信息"
        };
        String[] keys = new String[]{
                "entname","uniscid",
                "legal_info",
                "name","actual_controller","altimate_beneficiary",
                "coninfo","rgtered_info"
        };
        float pageSize = PageSize.A4.getWidth();
        float[] widths = new float[]{
                0.13f * pageSize,
                0.10f * pageSize,
                0.18f * pageSize,
                0.07f * pageSize,
                0.09f * pageSize,
                0.09f * pageSize,
                0.16f * pageSize,
                0.18f * pageSize
        };
        PdfPTable listtable = generateTable(data,keys,col_titles,widths);
        listtable.setSpacingBefore(40f);
        document.add(listtable);

        //算法3文本列表部分
        PdfPTable relationTitle = PdfUtils.getChpterTitleTable("三、关联关系");
        document.add(relationTitle);
        Paragraph retitle1 = new Paragraph("3.1、工商信息", simheiFont13);
        retitle1.setAlignment(Element.ALIGN_LEFT);
        retitle1.setIndentationRight(20f);
        document.add(retitle1);
        List<String> list = new ArrayList<>();
        list.add("测试公司1和测试公司2共用了一个办公电话");
        list.add("测试公司2和测试公司4共用了一个办公地址");
        list.add("测试公司2和测试公司6拥有同一办公地址");
        list.add("测试公司2和测试公司6拥有同一办公地址");
        list.add("测试公司2和测试公司6拥有同一办公地址");
        list.add("测试公司2和测试公司6拥有同一办公地址");
        list.add("测试公司2和测试公司6拥有同一办公地址");
        list.add("测试公司2和测试公司6拥有同一办公地址");
        list.add("测试公司2和测试公司6拥有同一办公地址");
        PdfPTable textlist = PdfUtils.generateTextList(list);//生成文本列表
        document.add(textlist);

        Paragraph retitle2 = new Paragraph("3.2、一方直接或者间接持有另一方的股份总和达到25%以上，双方直接或者间接为第三方所持有的股份达到25%以上", simheiFont13);
        retitle2.setAlignment(Element.ALIGN_LEFT);
        retitle2.setIndentationRight(20f);
        retitle2.setSpacingBefore(15f);
        document.add(retitle2);
        List<String> list2 = new ArrayList<>();
        list2.add("测试公司1和测试公司2共持股测试公司3超过25%，测试公司1和测试公司2共持股测试公司3超过25%，测试公司1和测试公司2共持股测试公司3超过25%");
        list2.add("测试公司1和测试公司2共持股测试公司3超过25%，测试公司1和测试公司2共持股测试公司3超过25%，测试公司1和测试公司2共持股测试公司3超过25%，测试公司1和测试公司2共持股测试公司3超过25%，测试公司1和测试公司2共持股测试公司3超过25%，测试公司1和测试公司2共持股测试公司3超过25%");
        list2.add("测试公司1和测试公司2共持股测试公司3超过25%");
        list2.add("测试公司1和测试公司2共持股测试公司3超过25%");
        list2.add("测试公司1和测试公司2共持股测试公司3超过25%");
        list2.add("测试公司1和测试公司2共持股测试公司3超过25%");
        PdfPTable textlist2 = PdfUtils.generateTextList(list2);//生成文本列表
        document.add(textlist2);

        Paragraph retitle3 = new Paragraph("3.3、双方的董事、总经理、法人为同一人，一方的第一大股东或两个以上股东在另一方任职", simheiFont13);
        retitle3.setAlignment(Element.ALIGN_LEFT);
        retitle3.setIndentationRight(20f);
        retitle3.setSpacingBefore(15f);
        document.add(retitle3);
        List<String> list3 = new ArrayList<>();
        list3.add("测试公司1有两个及以上股东在测试公司2任职");
        list3.add("测试公司1有两个及以上股东在测试公司2任职");
        list3.add("测试公司1有两个及以上股东在测试公司2任职");
        list3.add("测试公司1有两个及以上股东在测试公司2任职");
        list3.add("测试公司1有两个及以上股东在测试公司2任职");
        list3.add("测试公司1有两个及以上股东在测试公司2任职");
        PdfPTable textlist3 = PdfUtils.generateTextList(list3);//生成文本列表
        document.add(textlist3);

        //最后一页


        document.close();
        File file = new File("D:\\test.pdf");
        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream fileoutput = new FileOutputStream(file);
        fileoutput.write(output.toByteArray());
        fileoutput.flush();
        output.close();
        fileoutput.close();
    }

    /**
     * 生成表格
     * @param data
     * @param keys
     * @param col_titles
     * @return
     */
    private static PdfPTable generateTable(List<Map> data, String[] keys, String[] col_titles,float [] colwidths) throws DocumentException {
        int colnum = col_titles.length;
        PdfPTable table = new PdfPTable(colnum);
        table.setWidthPercentage(100);
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
                PdfPCell cell = generateCell(text,  PdfPCell.ALIGN_MIDDLE,PdfPCell.ALIGN_LEFT);
                table.addCell(cell);
            }
            table.completeRow();
        }
        return table;
    }


    private static PdfPCell generateHeaderCell(String text, int vertAlign, int horiAlign) {
        Font simheiFont12 = new Font(fontSimhei, 11, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        PdfPCell cell = new PdfPCell(new Phrase(text, simheiFont12));
        cell.setPaddingLeft(3);
        cell.setPaddingBottom(4);
        cell.setVerticalAlignment(vertAlign);
        cell.setHorizontalAlignment(horiAlign); //设置水平定位(靠左，居中，靠右)
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderColor(BaseColor.BLACK);
        BaseColor bkColor = new BaseColor(0.96f, 0.96f, 0.96f);
        cell.setBackgroundColor(bkColor); //背景色为亮灰色
        return cell;
    }

    private static PdfPCell generateCell(String text, int vertAlign, int horiAlign) {
        Font simheiFont10 = new Font(fontSimhei, 10, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        PdfPCell cell = new PdfPCell(new Phrase(text, simheiFont10));
        cell.setPaddingLeft(3);
        cell.setPaddingBottom(4);
        cell.setVerticalAlignment(vertAlign);
        cell.setHorizontalAlignment(horiAlign); //设置水平定位(靠左，居中，靠右)
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderColor(BaseColor.BLACK);
        return cell;
    }

    // 生成目录页
    public static void generateContentsPage(Document document, int page, ArrayList catalogTitle, ArrayList catalogPage, PdfWriter writer) throws DocumentException {
        document.newPage();
        Paragraph menu = new Paragraph("目录", menuSimheiFont20);
        menu.setSpacingBefore(20f);
        menu.setSpacingAfter(20f);
        menu.setAlignment(Element.ALIGN_CENTER);
        document.add(menu);
        document.add(new Paragraph(new Chunk("一.企业基本信息", simheiFont15)));
        for (int i = 0; i < catalogTitle.size(); i++) {
            Paragraph content = new Paragraph(new Chunk(i + 1 + "." + catalogTitle.get(i), simheiFont13));
            content.add(new Chunk(new DottedLineSeparator()));
            content.add(new Chunk(catalogPage.get(i).toString(), simheiFont13));
            document.add(content);
        }
        Paragraph pictureTitle = new Paragraph(new Chunk("二.企业知识图谱", simheiFont15));
        pictureTitle.add(new Chunk(new DottedLineSeparator()));
        pictureTitle.add(new Chunk("" + page, simheiFont13));
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

    private static void canvasAtlas(Document document, Image image, String title) throws DocumentException, UnsupportedEncodingException {
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
        box.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
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
//        LineSeparator line = new LineSeparator();
//        line.setLineColor(new BaseColor(0.87f, 0.87f, 0.87f));
//        line.setOffset(5);
//        pBlank.add(line);
        document.add(pBlank);
    }

    private static PdfPTable getChpterTitleTable(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"width: 3px; height: 17px; background-color: #5397E9;\">");
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

        Font myTitleFont = new Font(fontSimhei, 14, Font.NORMAL, new BaseColor(0.33f, 0.59f, 0.91f));
        PdfPCell cell2 = new PdfPCell(new Phrase(title, myTitleFont));
        cell2.setPadding(0);
        cell2.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell2.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell2);
        table.setSpacingBefore(15f);
        table.setSpacingAfter(0);
        return table;
    }

    // 封面底部表格单元格
    private static PdfPCell getCell(String text, int alignment) {
        Font simheiFont8 = new Font(fontSimhei, 8, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        PdfPCell cell = new PdfPCell(new Phrase(text, simheiFont8));
        cell.setPadding(0);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

}
