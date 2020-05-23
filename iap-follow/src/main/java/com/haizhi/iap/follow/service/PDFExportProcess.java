package com.haizhi.iap.follow.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.common.utils.ConfUtil;
import com.haizhi.iap.follow.controller.InternalSearchWS;
import com.haizhi.iap.follow.controller.model.InternalWrapper;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.model.ExportImages;
import com.haizhi.iap.follow.model.FollowItem;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.repo.ExportImagesRepo;
import com.haizhi.iap.follow.repo.FollowItemRepo;
import com.haizhi.iap.follow.repo.RedisRepo;
import com.haizhi.iap.follow.repo.TaskRepo;
import com.haizhi.iap.follow.utils.PDFFilter;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.mongodb.gridfs.GridFSDBFile;
import joptsimple.internal.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.net.URLDecoder.decode;

/**
 * Created by chenbo on 17/1/16.
 *
 * @desc excel 导出服务
 */
@Slf4j
@Service
public class PDFExportProcess {

    @Setter
    @Value("${graph_address}")
    String graphAddress;

    @Setter
    @Autowired
    TaskRepo taskRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Setter
    @Autowired
    ExportImagesRepo exportImagesRepo;

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
    @Autowired
    InternalSearchWS internalSearchWS;

    private static Integer BYTE_BUF_SIZE = 1024;

    private static String UPLOAD_DIR = "bigdata/pdfreport";
    private static Integer MAX_NUM = 50000;

    private BaseFont fontSimhei = null;
    private BaseFont fontPingfangRegular = null;
    public static final String LOGO = ConfUtil.getAbsolutePath("/images/logo.png");
    public static final String BACK_COVER_BACKGROUND = ConfUtil.getAbsolutePath("/images/back_cover_image.png");
    public static final String SIMHEI_FONT_PATH = "/fonts/simhei.ttf";
    public static final String PINGFANG_FONT_PATH = "/fonts/PingFangRegular.ttf";
    private Font myFont = null;
    private Font simheiFont39 = null;
    private Font simheiFont20 = null;
    private Font simheiFont15 = null;
    private Font simheiFont13 = null;
    private Font simheiFont9 = null;
    private Font menuSimheiFont20 = null;
    private Map<String, Object> defaultConfig;

    private String readCSS() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        InputStream is = XMLWorkerHelper.class.getResourceAsStream("/css/index.css");
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return new String(baos.toByteArray());
    }

    // 过滤字符
    private String filterData(String type, String string) {
        String data = "";
        try {
            data = new PDFFilter().PDFFilter(type, string);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return data;
    }

    private PDFExportProcess() throws IOException, DocumentException {
        try {
            fontSimhei = BaseFont.createFont(SIMHEI_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            fontPingfangRegular = BaseFont.createFont(PINGFANG_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
        myFont = new Font(fontSimhei, 10, Font.NORMAL, new BaseColor(0.45f, 0.45f, 0.45f));
        simheiFont39 = new Font(fontSimhei, 39, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        simheiFont20 = new Font(fontSimhei, 20, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        simheiFont15 = new Font(fontSimhei, 15, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        simheiFont13 = new Font(fontSimhei, 13, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        simheiFont9 = new Font(fontSimhei, 9, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        menuSimheiFont20 = new Font(fontSimhei, 20, Font.BOLDITALIC, new BaseColor(0.45f, 0.45f, 0.45f));

        XMLWorkerFontProvider fontImp = new XMLWorkerFontProvider(ConfUtil.getAbsolutePath("/fonts/simhei.ttf"));
        FontFactory.setFontImp(fontImp);
        FontFactory.register(ConfUtil.getAbsolutePath("/fonts/simhei.ttf"), "fontSimhei");
        defaultConfig = ConfUtil.readJson("/pdf_report.json");

    }

    // create pdf
    public String process(Long taskId) {
        Task task = taskRepo.findById(taskId);

        double outPercent = 0d;
        task.setPercent(outPercent);
        taskRepo.update(task);
        taskRepo.updateStatus(task.getId(), TaskStatus.RUNNING.getCode());
        String companyName = task.getCompanyNames();
        if (companyName.equals("")) {
            companyName = null;
        }
        String[] companyNameArray = {};
        List<FollowItem> groupCompanyList = null;
        if (companyName != null) {
            companyNameArray = companyName.split(",");
        }

        if (companyNameArray.length == 0) {
            Long followListId = task.getFollowListId();
            Long curUserId = Long.parseLong(task.getUserId());
            groupCompanyList = followItemRepo.findByUserAndList(curUserId, followListId, 0, MAX_NUM);

        }

        log.info("pdf task {} start ...", task.getId());
        Map<String, Object> jsonObj = JSONObject.parseObject(task.getDataType());
        // 读取默认配置配置,生成详情页
        Map<String, Object> dConfig = (Map<String, Object>) (defaultConfig.get("data"));
        JSONArray jsonObjOption = (JSONArray) jsonObj.get("options");

        if (companyNameArray.length == 0
                && (groupCompanyList == null ||
                (groupCompanyList != null && groupCompanyList.size() == 0))) {
            log.info("no company name for pdf report export!");
            return "";
        } else if (companyNameArray.length == 1 || (groupCompanyList != null && groupCompanyList.size() == 1)) {
            try {
                String fileName = genFileName(task, false);
                String company;
                if (companyNameArray.length == 1) {
                    company = companyNameArray[0];
                } else {
                    company = groupCompanyList.get(0).getCompanyName();
                }
                ByteArrayOutputStream finalOutput = genOnePdfReport(company, dConfig, jsonObjOption, taskId);
                String fileDownLoadUrl = fileUploadService.saveFile(finalOutput.toByteArray(), UPLOAD_DIR, fileName);
                //更新job
                GridFSDBFile file = fileUploadService.getFile(fileDownLoadUrl);

                taskRepo.finish(task.getId(), fileDownLoadUrl, file == null ? 0 : file.getLength());
                log.info("generate {} pdf report success!", company);
                // return fileDownLoadUrl;
            } catch (Exception e) {
                log.error("{}", e);
                taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
            }
        } else if (companyNameArray.length > 1) {
            int len = companyNameArray.length;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(BYTE_BUF_SIZE);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bos);
            for (int j = 0; j < len; j++) {
                double percent = outPercent + 1.0 / len * (j + 1);
                BigDecimal decimal = new BigDecimal(percent * 100);
                percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                task.setPercent(percent);
                log.info("任务 {} 进度: {}", task.getId(), percent);
                taskRepo.update(task);
                ByteArrayOutputStream outputStream = null;
                String subCompanyName = companyNameArray[j];
                try {
                    outputStream = genOnePdfReport(subCompanyName, dConfig, jsonObjOption, taskId);
                    zipOutputStream.putNextEntry(new ZipEntry(subCompanyName + ".pdf"));
                    zipOutputStream.write(outputStream.toByteArray());

                } catch (DocumentException | IOException e) {
                    e.printStackTrace();
                    log.error("generate pdf taskId: {}, company: {}, failed.", taskId, subCompanyName);
                    log.error("{}", e);
                    taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
                }
            }
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            log.info("String fileName = genFileName(task);");
            String fileName = genFileName(task, true);
            String fileDownLoadUrl = fileUploadService.saveFile(bos.toByteArray(), UPLOAD_DIR, fileName);

            log.info("String fileName = genFileName(task); after");

            //更新job
            GridFSDBFile file = fileUploadService.getFile(fileDownLoadUrl);

            taskRepo.finish(task.getId(), fileDownLoadUrl, file == null ? 0 : file.getLength());
        } else if (groupCompanyList != null && groupCompanyList.size() > 1) {
            int len = groupCompanyList.size();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(BYTE_BUF_SIZE);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bos);
            for (int j = 0; j < len; j++) {
                double percent = outPercent + 1.0 / len * (j + 1);
                BigDecimal decimal = new BigDecimal(percent * 100);
                percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                task.setPercent(percent);
                log.info("任务 {} 进度: {}", task.getId(), percent);
                taskRepo.update(task);
                ByteArrayOutputStream outputStream = null;
                String subCompanyName = groupCompanyList.get(j).getCompanyName();
                try {
                    outputStream = genOnePdfReport(subCompanyName, dConfig, jsonObjOption, taskId);
                    zipOutputStream.putNextEntry(new ZipEntry(subCompanyName + ".pdf"));
                    zipOutputStream.write(outputStream.toByteArray());

                } catch (DocumentException | IOException e) {
                    e.printStackTrace();
                    log.error("generate pdf taskId: {}, company: {}, failed.", taskId, subCompanyName);
                    log.error("{}", e);
                    taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
                }
            }
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            log.info("String fileName = genFileName(task);");
            String fileName = genFileName(task, true);
            String fileDownLoadUrl = fileUploadService.saveFile(bos.toByteArray(), UPLOAD_DIR, fileName);

            log.info("String fileName = genFileName(task); after");

            //更新job
            GridFSDBFile file = fileUploadService.getFile(fileDownLoadUrl);

            taskRepo.finish(task.getId(), fileDownLoadUrl, file == null ? 0 : file.getLength());
        } else {
            taskRepo.finish(task.getId(), null, 0l);
            log.info("task {} has not any companies.", task.getId());
        }


        return "";
    }

    private ByteArrayOutputStream genOnePdfReport(String companyName, Map<String, Object> dConfig, JSONArray jsonObjOption, Long taskId) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, output);

        writer.setPageEvent(new PDFReportPageEventHelper(companyName));
        document.open();

        // 第一页
        generateCoverPage(document, companyName);
        // 生成封底
        generateBackCover(document, writer);

        /**真正的数据部分
         * 数据包括 工商信息，企业年报，上市信息，投资关系，知识产权，招标信息，风险信息，舆情信息
         * 和企业知识图谱
         */
        document.newPage();
//        Paragraph blank = new Paragraph(" ", myFont);
//        blank.setSpacingAfter(8f);
//        document.add(blank);
        // 纪录页的标题以及页码，方便生成目录
        ArrayList catalogTitle = new ArrayList();
        ArrayList catalogPage = new ArrayList();
        // 解析jsonarray对象
        class JSON2Collection {
            Object convert(Object jsonData) {
                Object object = null;
                if (jsonData instanceof JSONObject) {
                    object = JSONObject2Map((JSONObject) jsonData);
                } else if (jsonData instanceof JSONArray) {
                    object = JSONArray2List((JSONArray) jsonData);
                } else {
                    if (jsonData != null)
                        object = jsonData.toString();
                    else object = "null";
                }
                return object;
            }

            Object JSONObject2Map(JSONObject jsonObject) {
                Map map = new HashMap();
                for (Object entry : jsonObject.entrySet()) {
                    Map.Entry entryKV = (Map.Entry) entry;
                    map.put(entryKV.getKey().toString(), this.convert(entryKV.getValue()));
                }
                return map;
            }

            Object JSONArray2List(JSONArray jsonArray) {
                ArrayList list = new ArrayList();
                for (Object entry : jsonArray) {
                    list.add(this.convert(entry));
                }
                return list;
            }
        }
        ArrayList<Map> jsonData = (ArrayList<Map>) (new JSON2Collection().convert(jsonObjOption));
        for (Map<String, Object> firstJson : jsonData) {
            Map<String, Object> nextConfig = (Map<String, Object>) dConfig.get(firstJson.get("key"));
            ArrayList<Map<String, String>> secondJson = (ArrayList<Map<String, String>>) firstJson.get("value");
            // 显示一级目录前需要判断二级目录是否有选中的;
            Boolean hasChoice = Boolean.FALSE;
            for (Map<String, String> key : secondJson) {
                if (key.get("checked").equals("1")) {
                    hasChoice = Boolean.TRUE;
                    continue;
                }
            }
            if (hasChoice) {
                PdfPTable chpterTitleTable = getChpterTitleTable((String) nextConfig.get("cname"));
                catalogTitle.add(nextConfig.get("cname"));
                catalogPage.add(writer.getPageNumber() - 2);
                document.add(chpterTitleTable);
                // 上市公司先判断stock_code有几个，无则未上市，有就循环
                if (firstJson.get("key").equals("list")) {
                    InternalWrapper wrapper = internalSearchWS.search(companyName, null, "list", null, null, null, 0, 0, 20);
                    if (wrapper != null && wrapper.getStatus() != null && wrapper.getStatus() == 0) {
                        Map data = (Map) wrapper.getData();
                        if (data == null) {
                            addEnptyContent(document, "暂无上市信息");
                        } else {
                            ArrayList stockCode = (ArrayList) data.getOrDefault("stock_code", null);
                            if (stockCode.size() == 0) {
                                addEnptyContent(document, "暂无上市信息");
                            } else {
                                for (int i = 0; i < stockCode.size(); i++) {
                                    PdfPTable chpterTitleTable2 = getChpterTitleTable(filterData("CheckStockCode", (String) stockCode.get(i)));
                                    document.add(chpterTitleTable2);
                                    canvasTable(document, companyName, (String) firstJson.get("key"), secondJson, nextConfig, (String) stockCode.get(i));
                                }
                            }
                        }
                    }
                } else {
                    canvasTable(document, companyName, (String) firstJson.get("key"), secondJson, nextConfig, "");
                }
            }
        }
        document.newPage();
        // 图谱部分

        PdfPTable chpterTitleTable = getChpterTitleTable("企业知识图谱");
        document.add(chpterTitleTable);
        canvasPhantomjsAtlas(document, companyName, taskId);
        document.close();
        // 组合封底
        PdfReader reader = new PdfReader(output.toByteArray());
        Document documentTmp = new Document();
        ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
        PdfCopy copy = new PdfCopy(documentTmp, finalOutput);
        documentTmp.open();
        copy.addDocument(reader);
        // 生成目录页
        copy.addDocument(generateContentsPage(writer.getPageNumber() - 2, catalogTitle, catalogPage));
        reader.close();
        documentTmp.close();
        copy.close();
        // 将尾页放到最后，目录页放第二页
        PdfReader lastReader = new PdfReader(finalOutput.toByteArray());
        int n = lastReader.getNumberOfPages();
        lastReader.selectPages(String.format("1, %s, 3-%s, 2", n, n - 1));
        PdfStamper stamper = new PdfStamper(lastReader, finalOutput);
        stamper.close();
        return finalOutput;
    }

    // 生成第一页
    private void generateCoverPage(Document document, String companyName) throws DocumentException {
        Paragraph title0 = new Paragraph(" ");
        document.add(title0);
        Paragraph title1 = new Paragraph("企业关系图谱报告", simheiFont39);
        title1.setAlignment(Element.ALIGN_CENTER);
        title1.setSpacingBefore(88f);
        document.add(title1);
        Paragraph title2 = new Paragraph(companyName, simheiFont20);
        title2.setAlignment(Element.ALIGN_CENTER);
        title1.setSpacingBefore(28f);
        document.add(title2);
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        Paragraph title3 = new Paragraph(format.format(date), simheiFont15);
        title3.setAlignment(Element.ALIGN_CENTER);
        document.add(title3);
        //create title image
        Image img = null;
        try {
            img = Image.getInstance(LOGO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        img.setAlignment(Image.ALIGN_MIDDLE);
        img.setAbsolutePosition(267, 116);
        img.scaleAbsolute(60, 25);
        document.add(img);

        Paragraph bottomTitle1 = new Paragraph("北京海致星图科技有限公司", simheiFont15);
        bottomTitle1.setSpacingBefore(480f);
        bottomTitle1.setSpacingAfter(10f);
        bottomTitle1.setAlignment(Element.ALIGN_CENTER);
        document.add(bottomTitle1);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell(getCell("深圳市南山区学府路5号芒果网大厦905", PdfPCell.ALIGN_LEFT));
        table.addCell(getCell("北京市海淀区学院路甲5号768创意园B座8号门", PdfPCell.ALIGN_CENTER));
        table.addCell(getCell("上海市静安区武宁南路1号越商大厦2103室", PdfPCell.ALIGN_RIGHT));
        document.add(table);

        Paragraph bottomTitle3 = new Paragraph("联系方式:010-61190338", simheiFont9);
        bottomTitle3.setAlignment(Element.ALIGN_CENTER);
        document.add(bottomTitle3);
    }

    // 生成目录页
    public PdfReader generateContentsPage(int page, ArrayList catalogTitle, ArrayList catalogPage) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, output);
        log.info("catalog process...");
        document.open();
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
        document.close();
        PdfReader catalog = null;
        log.info("catalog process end...");
        try {
            catalog = new PdfReader(parse(output));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return catalog;
    }

    // 图谱截图判断
    public void canvasPhantomjsAtlas(Document document, String companyName, Long taskId) throws IOException, DocumentException {
        if (companyName != null && taskId.toString() != null) {
            ExportImages exportImage = exportImagesRepo.findByCompanyNameAndTaskId(companyName, taskId); // 判断能不能找到图片
            String imgPathList = "";
            String pathList = "";
            try {
                imgPathList = exportImage.getImgPathList();
                pathList = exportImage.getImgIntroList();
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
            if (imgPathList != null && !imgPathList.equals("")) {
                // 不为空，直接获取GridFSDBFile并画出图谱
                List<String> imgList = Arrays.asList(imgPathList.split("\\*"));
                List<String> titleList = Arrays.asList(pathList.split("\\*"));
                for (int i = 0; i < imgList.size(); i++) {
                    if (i > 0) {
                        document.newPage();
                    }
                    Image image = null;
                    GridFSDBFile file = fileUploadService.getFile(imgList.get(i));
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        file.writeTo(baos);
                        byte[] bytes = baos.toByteArray();
                        image = Image.getInstance(bytes);
                    } catch (Exception e) {
                        log.debug(e.getMessage());
                    }
                    //修正上传多张图谱截图，但不填写备注时会导致pdf下载任务失败的问题 2018.12.18 chengxinran&liulu
                    String title = Strings.EMPTY;
                    if(!CollectionUtils.isEmpty(titleList) && titleList.size() > i){
                        title = titleList.get(i);
                    }
                    canvasAtlas(document, image, title);
                }
            } else {
                try {
                    String st = screenShot(companyName);
                    Image image = Image.getInstance(st);
                    canvasAtlas(document, image, "");
                } catch (Exception e) {
                    log.error("screenshort error {}", companyName);
                }
            }
        }
    }

    // 截图
    public String screenShot(String companyName) throws IOException, BadElementException {
        String screenshotName = companyName + ".png";
        // 先获取用户的token
        String token = redisRepo.getPermanentToken();
        ClassRelativeResourceLoader loader = new ClassRelativeResourceLoader(PDFExportProcess.class);
        String scriptsPath = loader.getResource("classpath:screenshot-scripts").getFile().getAbsolutePath();
        File ssfile = new File(scriptsPath + "/tmp/" + screenshotName);
        String st = null;
        // 如果没有，则生成，存到本地目录下
        if (ssfile.exists()) {
            // 已经有截图，直接读取文件到pdf
            st = ssfile.getPath();
        } else {
            // 先截图再读取文件到pdf
            StringBuffer sb = new StringBuffer();
            // 给phantomjs加上可执行权限
            String systemName = System.getProperty("os.name");
            String pjsName = "";
            if (systemName.toLowerCase().contains("mac")) {
                pjsName = "phantomjs-mac.sh";
            } else if (systemName.toLowerCase().contains("linux")) {
                //注意不支持32
                pjsName = "phantomjs-linux.sh";
            } else {
                throw new RuntimeException("暂时没有" + systemName + "系统的phantomjs版本！");
            }
            String finalPath = scriptsPath + "/phantomjs/bin/" + pjsName;
            //不同系统不一样，下面不兼容
//            Path phantomjsBin = Paths.get(scriptsPath + "/phantomjs/bin/" + pjsName);
//            Files.setPosixFilePermissions(phantomjsBin, PosixFilePermissions.fromString("rwxr-xr-x"));
            Process proc = Runtime.getRuntime().exec("chmod +x " + finalPath);
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                log.error("{}", e);
            }
            sb.append(scriptsPath + "/phantomjs/bin/" + pjsName + " ");
            sb.append("--web-security=no ");
            sb.append(scriptsPath + "/cropper.js ");
            sb.append(graphAddress + "?type=Graph&operation=snapshot&company=");
            //sb.append(graphAddress + "?type=Graph&operation=snapshot&company=");
            sb.append(URLEncoder.encode(companyName, "UTF-8"));
            sb.append("&token=");
            sb.append(token);
            sb.append(" " + scriptsPath + "/tmp/");
            sb.append(screenshotName);
            String command = sb.toString();
            log.info(command);
            Process p = Runtime.getRuntime().exec(command);
            // TODO 增加超时处理
            try {
                p.waitFor(); // 等待
            } catch (InterruptedException e) {
                log.error("phantomjs screenshort error {}!", e);
            }
            if (p.exitValue() != 0) {
                log.error("screenshort error!");
            }
            try {
                st = scriptsPath + "/tmp/" + screenshotName;
            } catch (Exception e) {
                log.error("{}", e);
            }
        }
        return st;
    }

    // 生成封底
    public void generateBackCover(Document document, PdfWriter writer) throws IOException, DocumentException {
        log.info("pdf, generate back cover page.");
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
        Image logo = null;
        try {
            logo = Image.getInstance(LOGO);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void canvasAtlas(Document document, Image image, String title) throws DocumentException, UnsupportedEncodingException {
        addSubTitle(document, title, "atlas");
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

    private void canvasTable(Document document, String companyName, String type, ArrayList<Map<String, String>> firstConfig, Map<String, Object> initConfig, String stock_code) throws DocumentException, UnsupportedEncodingException {
        if (type.equals("annual_report")) {
            generateAnnualReport(document, companyName, firstConfig, initConfig);
//            addTooMuchContent(document, initConfig.get("tooManyTips").toString());
        } else {
            try {
                InternalWrapper wrapper = internalSearchWS.search(companyName, stock_code, type, null, null, null, 0, 0, 20);
                if (wrapper != null && wrapper.getStatus() != null && wrapper.getStatus() == 0) {

                    Map data = (Map) wrapper.getData();
                    Map<String, Object> config = (Map<String, Object>) initConfig.get("children"); // 配置第二层children
                    for (Map<String, String> secondConfig : firstConfig) {
                        String key = secondConfig.get("key").toString();
                        if (secondConfig.get("checked").equals("1")) {
                            Map<String, Object> lastConfig = (Map<String, Object>) config.getOrDefault(key, null);
                            if (data == null || !data.containsKey(key) || data.get(key).equals("") || lastConfig == null) {
                                addSubTitle(document, secondConfig.get("cname"), "0");
                                addEnptyContent(document, "暂无" + secondConfig.get("cname"));
                                break;
                            } else {
                                Map<String, Object> dataWrapper = (Map<String, Object>) data.get(key);
                                String tableType = (String) lastConfig.getOrDefault("tpl_type", "");
                                addSubTitle(document, secondConfig.get("cname"), dataWrapper.getOrDefault("total_count", "0").toString());
                                if (tableType.equals("map_table")) {
                                    Map<String, Object> dataMap = (Map<String, Object>) dataWrapper.get("data");
                                    if (dataMap == null || dataMap.size() == 0) {
                                        addEnptyContent(document, (String) lastConfig.get("empty_tips"));
                                    } else {
                                        mapTableStyle(document, dataMap, lastConfig);
                                    }
                                } else {
                                    ArrayList<Map<String, Object>> dataList = (ArrayList<Map<String, Object>>) dataWrapper.get("data");
                                    if (dataList.size() == 0) {
                                        addEnptyContent(document, (String) lastConfig.get("empty_tips"));
                                    } else {
                                        choiceTable(tableType, document, dataList, lastConfig, companyName);
                                        int total = Integer.parseInt(dataWrapper.getOrDefault("total_count", "0").toString());
                                        if (total > 20) {
                                            addTooMuchContent(document, (String) lastConfig.get("tooManyTips"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("{}", e);
            }
        }

    }

    public void choiceTable(String tableType, Document document, ArrayList<Map<String, Object>> dataList, Map<String, Object> lastConfig, String company) {
        switch (tableType) {
            case "list_table":
                listTableStyle(document, dataList, lastConfig);
                break;
            case "list_card":
                listCardStyle(document, dataList, lastConfig, company);
                break;
            case "list_pane":
                listPaneStyle(document, dataList, lastConfig);
                break;
            default:
                break;
        }
    }

    private void listTableStyle(Document document, ArrayList<Map<String, Object>> dataList, Map<String, Object> configDetail) {
        ArrayList<Integer> columnWidths = (ArrayList<Integer>) configDetail.get("column_widths");
        float[] columnWidths1 = arrayListToFloatArray(columnWidths);
        PdfPTable listTable = bodyTableTmp(document, columnWidths1);
        ArrayList<Map<String, Object>> schemaList1 = (ArrayList<Map<String, Object>>) configDetail.get("schema");
        for (Map<String, Object> l : schemaList1) {
            PdfPCell k = bodyTableCellTmp((String) l.get("key_cname"), Boolean.TRUE, Element.ALIGN_LEFT, Boolean.TRUE, 1);
            listTable.addCell(k);
        }
        Integer a = 1;
        for (Map<String, Object> l : dataList) {
            for (Map<String, Object> m : schemaList1) {
                String key = (String) m.get("key_name");
                String value = "";
                if (m.get("filter").toString().equals("NullToIndex")) {
                    value = filterData(m.get("filter").toString(), a.toString());
                    a++;
                } else {
                    value = filterData(m.get("filter").toString(), l.getOrDefault(key, "").toString());
                }
                PdfPCell v = bodyTableCellTmp(value, Boolean.FALSE, Element.ALIGN_LEFT, Boolean.TRUE, 1);
                listTable.addCell(v);
            }
        }
        try {
            document.add(listTable);
        } catch (Exception E) {
            log.debug(E.getMessage());
        }
    }

    private PdfPTable innerListTableStyle(Document document, ArrayList<Map<String, Object>> dataList, Map<String, Object> configDetail) {
        ArrayList<Integer> columnWidths = (ArrayList<Integer>) configDetail.get("column_widths");
        float[] columnWidths1 = arrayListToFloatArray(columnWidths);
        PdfPTable listTable = bodyTableTmp(document, columnWidths1);
        listTable.setTotalWidth(document.getPageSize().getWidth() - 78);
        listTable.setSpacingBefore(0f);
        ArrayList<Map<String, Object>> schemaList1 = (ArrayList<Map<String, Object>>) configDetail.get("schema");
        listTable.getDefaultCell().setBorderColor(new BaseColor(0.91f, 0.91f, 0.91f));
        for (Map<String, Object> l : schemaList1) {
            PdfPCell k = bodyTableCellTmp((String) l.get("key_cname"), Boolean.TRUE, Element.ALIGN_LEFT, Boolean.TRUE, 1);
            listTable.addCell(k);
        }
        for (Map<String, Object> l : dataList) {
            for (Map<String, Object> m : schemaList1) {
                String key = (String) m.get("key_name");
                String value = filterData(m.get("filter").toString(), l.getOrDefault(key, "").toString());
                PdfPCell v = bodyTableCellTmp(value, Boolean.FALSE, Element.ALIGN_LEFT, Boolean.TRUE, 1);
                listTable.addCell(v);
            }
        }
        return listTable;
    }

    private void listCardStyle(Document document, ArrayList<Map<String, Object>> dataList, Map<String, Object> configDetail, String company) {
        ArrayList<Integer> columnWidths = (ArrayList<Integer>) configDetail.get("column_widths");
        String type = (String) configDetail.get("cname");
        float[] columnWidths1 = arrayListToFloatArray(columnWidths);
        for (Map<String, Object> l : dataList) {
            ArrayList<ArrayList<Map<String, Object>>> doubleTableConfig = (ArrayList<ArrayList<Map<String, Object>>>) configDetail.get("schema");
            String identity = "--";
            for (int i = 0; i < doubleTableConfig.size(); i++) {
                PdfPTable table = bodyTableTmp(document, columnWidths1);
                PdfPTable tableBox = bodyTableTmp(document, columnWidths1);
                table.setHeaderRows(0);
                for (Map<String, Object> n : doubleTableConfig.get(i)) {
                    String key = (String) n.get("key_name");
                    PdfPCell pc = new PdfPCell();
                    pc.setPaddingTop(5f);
                    pc.setPaddingBottom(5f);
                    // 处理身份字段, 如果是开庭公告，显示空的，下面没有原告，被告改成当事人
                    if (key.equals("identity")) {
                        ArrayList<String> plain = (ArrayList<String>) l.get("plaintiff_list");
                        ArrayList<String> defendant = (ArrayList<String>) l.get("defendant_list");
                        if (plain != null) {
                            for (int x = 0; x < plain.size(); x++) {
                                if (((String) plain.get(x)).equals(company)) {
                                    identity = "原告";
                                    break;
                                }
                            }
                        }
                        if (defendant != null) {
                            for (int x = 0; x < defendant.size(); x++) {
                                if (((String) defendant.get(x)).equals(company)) {
                                    identity = "被告";
                                    break;
                                }
                            }
                        }
                        if (identity.equals("--") && type.equals("开庭公告")) {
                            pc = bodyTableCellTmpTwo("", (Integer) n.get("colspan"));
                            table.addCell(pc);

                        } else {
                            pc = bodyTableCellTmpTwo(n.get("key_cname") + ": " + filterData((String) n.get("filter"), identity), (Integer) n.get("colspan"));
                            table.addCell(pc);
                        }
                    } else {
                        try {
                            if (type.equals("新闻")) {
                                pc = bodyTableCellTmpNews(filterData((String) n.get("filter"), (String) l.get(n.get("key_name"))), (Integer) n.get("colspan"), (String) n.get("key_name"));
                                table.addCell(pc);
                                // 处理开庭公告： 1. 如果有身份字段时，应该显示原告、被告 2. 如果没有身份字段时，显示当事人
                            } else if (type.equals("开庭公告") && key.equals("plaintiff_list")) { // 开庭公告，原告字段
                                if (!identity.equals("--")) {
                                    pc = bodyTableCellTmpTwo(n.get("key_cname") + ": " + filterData((String) n.get("filter"), (String) l.get(n.get("key_name")).toString()), (Integer) n.get("colspan"));
                                    table.addCell(pc);
                                }
                            } else if (type.equals("开庭公告") && key.equals("defendant_list")) { // 开庭公告，被告字段
                                if (!identity.equals("--")) {
                                    pc = bodyTableCellTmpTwo(n.get("key_cname") + ": " + filterData((String) n.get("filter"), (String) l.get(n.get("key_name")).toString()), (Integer) n.get("colspan"));
                                    table.addCell(pc);
                                }
                            } else if (type.equals("开庭公告") && key.equals("litigants")) { // 开庭公告，当事人字段
                                if (identity.equals("--")) {
                                    pc = bodyTableCellTmpTwo(n.get("key_cname") + ": " + filterData((String) n.get("filter"), (String) l.get(n.get("key_name")).toString()), (Integer) n.get("colspan"));
                                    table.addCell(pc);
                                }
                            } else if (key.equals("acreage")) {
                                String text = n.get("key_cname") + ": " + l.get(n.get("key_name"));
                                Font small = new Font(fontSimhei, 6, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
                                Chunk m2 = new Chunk("2", small);
                                m2.setTextRise(2);
                                Paragraph square = new Paragraph(text, simheiFont9);
                                square.setIndentationLeft(20f);
                                square.add(m2);
                                PdfPCell ppc = new PdfPCell(square);
                                ppc.setPaddingLeft(9f);
                                ppc.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            } else {
                                if (n.get("filter").toString().equals("investList")) {
                                    ArrayList<Map<String, Object>> datalist = (ArrayList<Map<String, Object>>) l.get(n.get("key_name"));
                                    if (datalist.size() == 0) {
                                        pc = bodyTableCellTmpTwo(n.get("key_cname").toString() + ":暂无投资方信息", (Integer) n.get("colspan"));
                                    } else {
                                        PdfPCell pcTitle = new PdfPCell();
                                        pcTitle.setBorderColor(new BaseColor(0.91f, 0.91f, 0.91f));
                                        pcTitle = bodyTableCellTmpTwo(n.get("key_cname").toString() + ":", (Integer) n.get("colspan"));
                                        table.addCell(pcTitle);
                                        PdfPTable innerTable = innerListTableStyle(document, datalist, n);
                                        pc.setBorder(Rectangle.NO_BORDER);
                                        pc.addElement(innerTable);
                                        pc.setColspan(2);
                                    }
                                } else {
                                    pc = bodyTableCellTmpTwo(n.get("key_cname") + ": " + filterData((String) n.get("filter"), (String) l.get(n.get("key_name")).toString()), (Integer) n.get("colspan"));
                                }
                                table.setSplitLate(false);
                                table.setSplitRows(true);
                                table.addCell(pc);
                            }
                        } catch (Exception e) {
                            pc = bodyTableCellTmpTwo(n.get("key_cname") + (type.equals("新闻") ? "" : ": ") + "--", (Integer) n.get("colspan"));
                            table.addCell(pc);
                        }
                    }
                }
                table.setSplitLate(false);
                table.setSplitRows(true);
                table.setSpacingBefore(0f);
                table.setSpacingAfter(0f);
                tableBox.setSpacingBefore(0f);
                tableBox.setSpacingAfter(0f);
                PdfPCell pctc = new PdfPCell(table);
                pctc.setColspan(2);
                pctc.setBorderColor(new BaseColor(0.91f, 0.91f, 0.91f));
                if (i == 0) {
                    tableBox.setSpacingBefore(22f);
                }
                tableBox.addCell(pctc);
                try {
                    document.add(tableBox);
                } catch (Exception E) {
                    log.debug(E.getMessage());
                }
            }
        }
    }

    private void mapTableStyle(Document document, Map<String, Object> dataList, Map<String, Object> configDetail) {
        ArrayList<Integer> columnWidths = (ArrayList<Integer>) configDetail.get("column_widths");
        float[] columnWidths1 = arrayListToFloatArray(columnWidths);
        PdfPTable basicTable = bodyTableTmp(document, columnWidths1);
        ArrayList<Map<String, Object>> schemaList = (ArrayList<Map<String, Object>>) configDetail.get("schema");
        for (Map<String, Object> l : schemaList) {
            PdfPCell k = bodyTableCellTmp((String) l.get("key_cname"), Boolean.TRUE, Element.ALIGN_RIGHT, Boolean.FALSE, 1);
            basicTable.addCell(k);
            PdfPCell v = bodyTableCellTmp(filterData((String) l.get("filter"), (String) dataList.get(l.get("key_name"))),
                    Boolean.FALSE,
                    Element.ALIGN_LEFT,
                    Boolean.FALSE,
                    (Integer) l.get("colspan"));
            basicTable.addCell(v);
        }
        try {
            document.add(basicTable);
        } catch (Exception E) {
            log.debug(E.getMessage());
        }
    }

    private void listPaneStyle(Document document, ArrayList<Map<String, Object>> dataList, Map<String, Object> configDetail) {
        ArrayList<Map<String, Object>> schemaList = (ArrayList<Map<String, Object>>) configDetail.get("schema");
        try {
            document.add(listPaneTmp(dataList, schemaList));
        } catch (Exception E) {
            log.debug(E.getMessage());
        }
    }

    public float[] arrayListToFloatArray(ArrayList<Integer> al) {
        int size = al.size();
        float[] columnWidths = new float[size];
        int j = 0;
        for (Integer f : al) {
            columnWidths[j++] = (f != null ? (float) f : Float.NaN);
        }
        return columnWidths;
    }

    private void generateAnnualReport(Document document, String companyName, ArrayList<Map<String, String>> jsonConfig, Map<String, Object> config) throws DocumentException, UnsupportedEncodingException {
        if (config != null) {
            Map<String, Object> initConfig = (Map<String, Object>) config.get("children");
            InternalWrapper wrapper = internalSearchWS.search(companyName, null, "annual_report", null, null, null, 0, 0, 20);
            if (wrapper != null && wrapper.getStatus() != null && wrapper.getStatus() == 0) {
                ArrayList<Map<String, Object>> zoneData = (ArrayList<Map<String, Object>>) wrapper.getData();
                if (zoneData == null || zoneData.size() == 0) {
                    addEnptyContent(document, "" + config.get("empty_tips"));
                } else {
                    for (Map key : jsonConfig) {
                        String jsonConfigKey = key.get("key").toString();
                        if (key.get("checked").toString().equals("1")) {
                            Boolean hasKey = Boolean.TRUE;
                            PdfPTable chpterTitleTable = getChpterTitleTable(key.get("key").toString() + "企业年报");
                            document.add(chpterTitleTable);
                            for (Map<String, Object> m : zoneData) {
                                String configKey = m.getOrDefault("year", "").toString();
                                if (jsonConfigKey.equals(configKey)) {
                                    hasKey = Boolean.FALSE;
                                    for (Map.Entry children : initConfig.entrySet()) {
                                        Map<String, Object> lastConfig = (Map<String, Object>) children.getValue();
                                        addSubTitle(document, lastConfig.get("cname").toString(), "");
                                        String tableType = lastConfig.get("tpl_type").toString();
                                        Map<String, Object> report = (Map<String, Object>) m.get("report");

                                        if (tableType.equals("map_table")) {
                                            if (children.getKey().equals("enterprise_asset_status_information")) {
                                                Object isNull = report.getOrDefault("enterprise_asset_status_information", null);
                                                if (isNull == null) {
                                                    addEnptyContent(document, (String) lastConfig.get("empty_tips"));
                                                } else {
                                                    String dataType = report.getOrDefault("enterprise_asset_status_information", null).getClass().toString();
                                                    Map<String, Object> dataList = null;
                                                    if (dataType.equals("class java.util.ArrayList")) {
                                                        ArrayList<Map<String, Object>> dataListArrray = (ArrayList<Map<String, Object>>) report.get("enterprise_asset_status_information");
                                                        dataList = dataListArrray.get(0);
                                                    } else {
                                                        dataList = (Map<String, Object>) report.get("enterprise_asset_status_information");
                                                    }
                                                    mapTableStyle(document, dataList, lastConfig);
                                                }
                                            } else {
                                                mapTableStyle(document, report, lastConfig);
                                            }
                                        } else {
                                            ArrayList<Map<String, Object>> dataList = (ArrayList<Map<String, Object>>) report.getOrDefault(children.getKey(), null);
                                            if (dataList == null || dataList.size() == 0) {
                                                addEnptyContent(document, (String) lastConfig.get("empty_tips"));
                                            } else {
                                                choiceTable(tableType, document, dataList, lastConfig, companyName);
                                            }
                                        }
                                    }
                                }
                            }
                            if (hasKey) {
                                addEnptyContent(document, "暂无" + key.get("key").toString() + "企业年报");
                            }
                        }
                    }
                }
            }
        }

    }

    // 主要人员
    private Paragraph listPaneTmp(ArrayList<Map<String, Object>> dataList, ArrayList<Map<String, Object>> schemaList) {
        Paragraph context = new Paragraph();
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"tableStyle2\">");
        for (int i = 0; i < dataList.size(); i++) {
            if (i == 0) {
                sb.append("<tr>");
            } else if (i % 5 == 0) {
                sb.append("</tr><tr>");
            }
            sb.append("<td><table class=\"tableStyle1\">" +
                    "<tr><td class=\"name\">" + filterData("MoreOmit", (String) dataList.get(i).get(schemaList.get(0).get("key_name"))) + "</td></tr>" +
                    "<tr><td class=\"job\">" + dataList.get(i).get(schemaList.get(1).get("key_name")) + "</td></tr>" +
                    "</table></td>");
            if (i == dataList.size() - 1) {
                sb.append("</tr>");
            }
        }
        sb.append("</table>");
        ElementList list = null;
        try {
            String css = readCSS();
            list = XMLWorkerHelper.parseToElementList(new String(sb.toString().getBytes("UTF-8")), css);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        context.setSpacingBefore(5f);
        context.setIndentationLeft(-8f);
        for (Element element : list) {
            context.add(element);
        }
        return context;
    }

    // 一级标题
    private PdfPTable getChpterTitleTable(String title) {
        log.info("开始模块：" + title);
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

    // 二级标题+横线
    public void addSubTitle(Document document, String string, String numberText)
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
            log.warn(e.getMessage());
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

    // 为空时的内容
    public void addEnptyContent(Document document, String string)
            throws DocumentException {
        if (string == null) string = "";
        Chunk chunk = new Chunk(string, myFont);
        Paragraph p = new Paragraph();
        p.add(chunk);
        p.setSpacingBefore(12f);
        p.setSpacingAfter(12f);
        document.add(p);
        Paragraph pBlank = new Paragraph();
        pBlank.setIndentationLeft(-7f);
        pBlank.setIndentationRight(-7f);
        document.add(pBlank);
    }

    // 超出20条数据显示
    public void addTooMuchContent(Document document, String string)
            throws DocumentException {
        if (string == null) string = "";
        Chunk chunk = new Chunk(string, myFont);
        Paragraph p = new Paragraph();
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);
        p.add(chunk);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);
        Paragraph pBlank = new Paragraph();
        pBlank.setIndentationLeft(-7f);
        pBlank.setIndentationRight(-7f);
        document.add(pBlank);
    }

    // 封面底部表格单元格
    private PdfPCell getCell(String text, int alignment) {
        Font simheiFont8 = new Font(fontSimhei, 8, Font.NORMAL, new BaseColor(0.2f, 0.2f, 0.2f));
        PdfPCell cell = new PdfPCell(new Phrase(text, simheiFont8));
        cell.setPadding(0);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    // 正文表格单元格样式
    private PdfPCell bodyTableCellTmp(String string,
                                      Boolean setBackgroundColor,
                                      int horizontalAlignment,
                                      Boolean isFixedHeight,
                                      int colSpan) {
        Phrase phr = new Phrase(string, simheiFont9);
        PdfPCell ppc = new PdfPCell(phr);
        ppc.setBorderColor(new BaseColor(0.91f, 0.91f, 0.91f));
        if (setBackgroundColor) {
            ppc.setBackgroundColor(new BaseColor(0.97f, 0.97f, 0.97f));
        }
        if (isFixedHeight) {
            ppc.setFixedHeight(24f);
        } else {
            ppc.setPaddingTop(5);
            ppc.setPaddingBottom(5);
        }
        if (horizontalAlignment == Element.ALIGN_RIGHT) {
            ppc.setPaddingRight(4);
        } else if (horizontalAlignment == Element.ALIGN_LEFT) {
            ppc.setPaddingLeft(4);
        }

        ppc.setVerticalAlignment(Element.ALIGN_MIDDLE);
        ppc.setHorizontalAlignment(horizontalAlignment);
        ppc.setColspan(colSpan);
        return ppc;
    }

    // 表格中无border时，cell的样式(新闻)
    private PdfPCell bodyTableCellTmpNews(String string, Integer colspan, String n) {
        Phrase phr;
        if (n.equals("title")) {
            phr = new Phrase(string, simheiFont13);
        } else if (n.equals("summary")) {
            phr = new Phrase(string, simheiFont9);
        } else {
            phr = new Phrase(string, myFont);
        }
        PdfPCell ppc = new PdfPCell(phr);
        ppc.setPaddingLeft(10f);
        ppc.setPaddingRight(10f);
        ppc.setPaddingTop(5f);
        ppc.setPaddingBottom(5f);
        ppc.setBorderColor(new BaseColor(0));
        ppc.setVerticalAlignment(Element.ALIGN_MIDDLE);
        ppc.setHorizontalAlignment(Element.ALIGN_LEFT);
        ppc.setColspan(colspan);
        return ppc;
    }

    // 表格中无border时，cell的样式(专利信息)
    private PdfPCell bodyTableCellTmpTwo(String string, Integer colspan) {
        Phrase phr = new Phrase(string, simheiFont9);
        PdfPCell ppc = new PdfPCell(phr);
        ppc.setPaddingLeft(10f);
        ppc.setPaddingRight(10f);
        ppc.setPaddingTop(5f);
        ppc.setPaddingBottom(5f);
        ppc.setBorderColor(new BaseColor(0));
        ppc.setVerticalAlignment(Element.ALIGN_MIDDLE);
        ppc.setHorizontalAlignment(Element.ALIGN_LEFT);
        ppc.setColspan(colspan);
        return ppc;
    }

    // 正文表格样式
    private PdfPTable bodyTableTmp(Document document, float[] layout) {
        PdfPTable ppt = new PdfPTable(layout);
        ppt.setTotalWidth(document.getPageSize().getWidth() - 60);
        ppt.setLockedWidth(true);
        ppt.setSpacingBefore(22f);
        ppt.setSpacingAfter(0f);
        ppt.getDefaultCell().setBorderColor(new BaseColor(0.91f, 0.91f, 0.91f));
        return ppt;
    }

    //outputStream转inputStream
    public ByteArrayInputStream parse(OutputStream out) throws Exception {
        ByteArrayOutputStream baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream;
    }

    public String genFileName(Task task, Boolean isSingle) {
        String posfix;
        if (isSingle) {
            posfix = ".zip";
        } else {
            posfix = ".pdf";
        }
        return String.valueOf(task.getUserId()) + "/" + String.valueOf(task.getId()) + "/" + task.getName() + posfix;
    }
}
