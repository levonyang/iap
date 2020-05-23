package com.haizhi.iap.follow.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.haizhi.iap.common.bean.CustdigParam;
import com.haizhi.iap.follow.controller.InternalSearchWS;
import com.haizhi.iap.follow.controller.model.InternalWrapper;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.repo.*;
import com.haizhi.iap.follow.utils.DateUtils;
import com.haizhi.iap.follow.utils.GridFsOperation;
import com.haizhi.iap.follow.utils.PdfUtils;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Image;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.stereotype.Service;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author linyong
 * @date 2020/04/01
 * @desc 招中标服务
 */
@Slf4j
@Service
public class CustdigExportProcess {

//    private static final String DEFAULT_UPLOAD_DIR = "custdig/upload";
    private static final String GRAPH_SUFFIX = ".png";
    private static final String PDF_SUFFIX = ".pdf";
    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private CustuploadRepo custuploadRepo;

    @Autowired
    private InternalSearchWS internalSearchWS;

    @Autowired
    private RedisRepo redisRepo;

    @Autowired
    private BatchResultRepo batchResultRepo;

    @Autowired
    private GridFsOperation gridFsOperation;

    @Autowired
    private CompanyDigResultRepo companyDigResultRepo;

    @Value("${atlas.edge.tablenames:te_holder,te_manager}")
    private String edges;

    @Value("${atlas.dig.depth:5}")
    private Integer depth;

    @Value("${atlas.dig.direct:out}")
    private String direct;

    @Value("${atlas.gap.address}")
    private String atlasAddress; //图平台场景探索svg图片导出地址

    @Value("${atlas.pdf.uploaddir:custdig/upload}")
    private String upload_dir;

    @Value("${atlas.dig.result.expire.unit:HOUR}")
    private String resultExpireUnit;

    @Value("${atlas.dig.result.expire.value:1}")
    private Integer resultExpire;

    public static final String PROCESS_FAIL = "failed";
    public static final String PROCESS_SUCCESS = "success";

    /**
     * 执行任务
     * @param taskId
     * @return
     */
    public String process(Long taskId) {
        Task task = taskRepo.findById(taskId);

        //1.设置task进度和状态
        double outPercent = 0d;
        task.setPercent(outPercent);
        taskRepo.update(task);
        taskRepo.updateStatus(task.getId(), TaskStatus.RUNNING.getCode());

        //2.获取batchid,和上传企业名单
        log.info("custdig task {} start ...", task.getId());
        String batchid = task.getDataType();
        List<Map> list = custuploadRepo.findByBatchid(batchid); //查找该批次的企业名单数据
        updateTaskPercent(task,10.0); //更新任务进度10%
        if(null == list || list.size() == 0){
            log.error("task {} ,company list is empty",task.getId());
            taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
            return PROCESS_FAIL;
        }
        List<String> companys = list.stream()
                .map(row -> row.get("company").toString())
                .collect(Collectors.toList());

//        Map uploadinfo = list.get(0);
//        String userid = getFromMap(uploadinfo, "userid", "");
        updateTaskPercent(task,20.0); //更新任务进度20%

        //3.传入企业名单和批次号获取关系图截图图片
//        String urlpath = null;
        Image image = null;
        try {
            log.info("start dig and get image");
            image = getImage(batchid, companys);
//            urlpath = digAndgetPath(batchid, companys);
        } catch (Exception e) {
            log.error("{}",e);
            taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
            return PROCESS_FAIL;
        }
        updateTaskPercent(task,40.0); //更新任务进度40%
        ByteArrayOutputStream outputStream = null;
        String pdfPath = null;
        byte[] bytes = null;
        try {

            //4.通过企业名单获取企业信息数据
            log.info("start query companyinfos");
            List<Map> companyinfos = getCompanyInfosData(companys);

            updateTaskPercent(task,50.0); //更新任务进度50%

            //5.通过企业名单获取企业关系信息
            log.info("start query company relationships");

            InternalWrapper wrapper = internalSearchWS.findCustByname(companys); //获取公司id集合
            if (null == wrapper || null == wrapper.getStatus() || wrapper.getStatus() != 0) {
                log.error("通过公司名称获取公司id失败");
                taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
                return PROCESS_FAIL;
            }
            Object data = wrapper.getData();
            String jsonStr = JSON.toJSONString(data);
            List<String> companyids = JSONArray.parseArray(jsonStr, String.class);

//            List<Map> busirelations = companyDigResultRepo.findCompanyRelations(companyids,new String[]{"1","2"});
//            List<Map> holderrelations = companyDigResultRepo.findCompanyRelations(companyids,new String[]{"6","7"});
//            List<Map> managerrelations = companyDigResultRepo.findCompanyRelations(companyids,new String[]{"3","4","5"});

            //一次查询，在内存里进行分组,提高速度
            List<Map> busirelations = new ArrayList<>();
            List<Map> holderrelations = new ArrayList<>();
            List<Map> managerrelations = new ArrayList<>();
            List<Map> allCompanyRelations = companyDigResultRepo.findAllCompanyRelations(companyids);

            //过滤关系数据，并且分割成三个子列表
            diffRelations(busirelations, holderrelations, managerrelations, allCompanyRelations);

            updateTaskPercent(task,70.0); //更新任务进度70%

            //6.构建pdf,生成pdf
            log.info("start generate pdf ");
            String inquirer = task.getCompanyNames(); //查询人
            String userId = task.getUserId();
            outputStream = generatePdf(userId,inquirer,image,companyinfos,busirelations,holderrelations,managerrelations);
            updateTaskPercent(task,90.0); //更新任务进度90%

            String filename = batchid + PDF_SUFFIX;
            bytes = outputStream.toByteArray();

            log.info("generate success,save to local");
            String localpath = saveToLocalTmp(bytes,batchid); //先保存到本地

            log.info("save to local success,upload remote filesysytem");

            try {
                pdfPath = gridFsOperation.saveFile(bytes, upload_dir, filename);
            } catch (Exception e) {
                log.info("save to remote file system failed,{}",e);
                pdfPath = localpath;
            }

        } catch (Exception e) {
            log.error("generate pdf failed",e);
            taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
            return PROCESS_FAIL;
        }

        log.info("success,update custdig task status");
        //变更task状态，写入pdf文件路径
        taskRepo.finish(task.getId(), pdfPath, ((Integer)bytes.length).longValue()); //完成，更新任务进度100%，同时写入结果
        return PROCESS_SUCCESS;
    }

    /**
     * 过滤关系数据，并且分割成三个子列表
     * @param busirelations
     * @param holderrelations
     * @param managerrelations
     * @param allCompanyRelations
     */
    private void diffRelations(List<Map> busirelations, List<Map> holderrelations, List<Map> managerrelations, List<Map> allCompanyRelations) {
        /**
         * 1、同一办公电话
         * 2、同一办公地址
         * 3、共高管或法人
         * 4、A 的第一大股东在B 任职
         * 5、A 有两个及以上股东在 B 任职
         * 6、A 控制 B 股份: 50%
         * 7、C 控股 A 40% 和 B 50%均超过25%
         * 过滤:其中1,2,3,7 存在A=>B和B=>A，所以需要过滤只保留单向的情况
         * 分割: 1,2=>busirelations, 3,4,5=>managerrelations, 6,7=>holderrelations
         * 排序:managerrelations和holderrelations按relship_flag升序
         */
        Set<String> keySet_1 = new HashSet<>();
        Set<String> keySet_2 = new HashSet<>();
        Set<String> keySet_3 = new HashSet<>();
        Set<String> keySet_7 = new HashSet<>();
        for (Map relation : allCompanyRelations) {
            Object relship_flag = relation.get("relship_flag");
            if("1".equals(relship_flag)){
                //过滤双向为单向
                String from_key = relation.get("from_key").toString();
                String to_key = relation.get("to_key").toString();
                if(!keySet_1.add(from_key+"_"+to_key)){
                    continue;
                }
                keySet_1.add(to_key+"_"+from_key);

                busirelations.add(relation);
            }else if("2".equals(relship_flag)){
                //过滤双向为单向
                String from_key = relation.get("from_key").toString();
                String to_key = relation.get("to_key").toString();
                if(!keySet_2.add(from_key+"_"+to_key)){
                    continue;
                }
                keySet_2.add(to_key+"_"+from_key);

                busirelations.add(relation);
            }else if("6".equals(relship_flag) ){
                holderrelations.add(relation);
            }else if("7".equals(relship_flag)){
                //过滤双向为单向
                String from_key = relation.get("from_key").toString();
                String to_key = relation.get("to_key").toString();
                if(!keySet_7.add(from_key+"_"+to_key)){
                    continue;
                }
                keySet_7.add(to_key+"_"+from_key);

                holderrelations.add(relation);
            }else if("3".equals(relship_flag)){
                //过滤双向为单向
                String from_key = relation.get("from_key").toString();
                String to_key = relation.get("to_key").toString();
                if(!keySet_3.add(from_key+"_"+to_key)){
                    continue;
                }
                keySet_3.add(to_key+"_"+from_key);

                managerrelations.add(relation);
            }else if("4".equals(relship_flag) || "5".equals(relship_flag)){
                managerrelations.add(relation);
            }
        }
        //持股进行排序(relship_flag从小到大)
        holderrelations.sort((o1,o2)->{
            String o1flag = getFromMap(o1,"relship_flag","");
            String o2flag = getFromMap(o2,"relship_flag","");;
            return o1flag.compareTo(o2flag);
        });
        //高管进行排序
        managerrelations.sort((o1,o2)->{
            String o1flag = getFromMap(o1,"relship_flag","");
            String o2flag = getFromMap(o2,"relship_flag","");;
            return o1flag.compareTo(o2flag);
        });
    }

    /**
     * 先保存到本地目录
     * @param bytes
     * @param batchid
     * @throws IOException
     */
    private String saveToLocalTmp(byte[] bytes,String batchid) throws IOException {
        ClassRelativeResourceLoader loader = new ClassRelativeResourceLoader(CustdigExportProcess.class);
        File classpath = loader.getResource("classpath:").getFile();
        String parentDir = "generatepdf";
        File dir = new File(classpath,parentDir);
        if(!dir.exists()){
            dir.mkdir();
        }
        File tmpfile = new File(dir,batchid+PDF_SUFFIX);
        if(tmpfile.exists()){
            tmpfile.delete();
        }
        FileOutputStream fileoutput = new FileOutputStream(tmpfile);
        fileoutput.write(bytes);
        fileoutput.flush();
        fileoutput.close();
        return parentDir+"/"+batchid+PDF_SUFFIX;
    }

    /**
     * 获取图谱图片
     * @param batchid
     * @param companys
     * @return
     * @throws Exception
     */
    private Image getImage(String batchid, List<String> companys) throws Exception {
        int filelength = 0;
        InputStream inputStream = null;
        String gridfilepath = upload_dir + "/" + batchid + GRAPH_SUFFIX; //gridfs上文件路径
        byte[] bytes = null;
//        GridFSDBFile dbfile = gridFsOperation.getFile(gridfilepath); //从gridfs上获取图片文件
//        if(null != dbfile){
//            inputStream = dbfile.getInputStream();
//            filelength = ((Long)dbfile.getLength()).intValue();
//        }else{

        //（注：BatchResult其实在上传名单的时候已经查询跑过，但是考虑到结果的时效性，
        // 比如客户关系数据刚好更新了，或者用户很久后才启动或者重跑任务，那么原来保存的结果可能已经无效，所以需要给结果设置失效时间）
        DateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String now = format.format(new Date());
        List<Map> dbBatchResults = batchResultRepo.findUnExpireByBatchid(batchid,now); //查询未失效的图挖掘结果
        if(dbBatchResults.size() == 0){ //如果没有图挖掘结果或者该结果已经失效，就重新进行挖掘保存
            CustdigParam param = new CustdigParam();
            param.setCompanys(companys);
            param.setType(edges); //设置挖掘边类型
            param.setDepth(depth); //设置挖掘深度
            param.setDirect(direct); //设置边方向(出OUT，入IN，双向ANY)
            InternalWrapper wrapper = internalSearchWS.custdig(param); //查询实体之间的5度关系数据
            log.info("process,internal search result:[status:{},msg:{}]",wrapper.getStatus(),wrapper.getMsg());
            if (null != wrapper && null != wrapper.getStatus() && wrapper.getStatus() == 0) {
                Map graph = (Map) wrapper.getData();
//                Map graph = dealGraph(data);
                //保存图数据信息到mysql库
                String content = JSON.toJSONString(graph);
                saveGraphResult(batchid,content); //保存图挖掘结果,(先删除再重新插入)
            }else{
                log.error("{}","failed get graph info");
            }
        }
        //开始截图
        try {
            //调用截图插件-请求图平台场景探索地址，传递参数batchid，图平台调用api数据转换接口，
            // 通过batchid查询上面保存的图数据，加载出页面，插件对页面进行截图
            String lcfilePath = screenShot(batchid);
            log.info("img filepath:{}",lcfilePath);
            File file = new File(lcfilePath);
            if(!file.exists()){
                log.error("{}","file not found");
                throw new Exception("file not found");
            }
            inputStream = new FileInputStream(file);
            Long length = file.length();
            bytes = new byte[length.intValue()];
            //inputStream.read(bytes);
            //String filename = batchid + GRAPH_SUFFIX;
            //文件上传到分布式文件系统
            //gridFsOperation.saveFile(bytes, UPLOAD_DIR, filename);
        } catch (Exception e) {
            log.error("screen Shot failed ");
            log.debug("{}",e);
            throw new Exception(e.getMessage());
        }
//        }
        Image image = null;
        try {
            inputStream.read(bytes);
            image = Image.getInstance(bytes);
        }catch (BadElementException e) {
            e.printStackTrace();
        }catch (IOException e) {
            log.error("read image error!" + e.getMessage());
            log.debug("{}",e);
            throw new Exception("read image error!" + e.getMessage());
        }
        return image;
    }

    /**
     * 挖掘并且获取图片路径
     * @param batchid
     * @param companys
     * @return
     * @throws Exception
     */
    @Deprecated
    private String digAndgetPath(String batchid, List<String> companys) throws Exception {
        int filelength = 0;
        InputStream inputStream = null;
        CustdigParam param = new CustdigParam();
        param.setCompanys(companys);
        param.setType(edges);
        param.setDepth(depth);
        InternalWrapper wrapper = internalSearchWS.custdig(param); //查询实体之间的5度关系数据
        if (null != wrapper && null != wrapper.getStatus() && wrapper.getStatus() == 0) {
            Map graph = (Map) wrapper.getData();
            //保存图数据信息到mysql库
            String content = JSON.toJSONString(graph);
            saveGraphResult(batchid,content);
            //获取图片路径
            String urlPath = new String(atlasAddress).replace("@batchid",batchid);
            return urlPath;
        }else{
            log.error("{}","failed get graph info");
            throw new RuntimeException(wrapper.getMsg());
        }
    }



    /**
     * 生成pdf
     *
     *
     * @param userId
     * @param inquirer 查询人
     * @param image 图片
     * @param companyinfos 公司详细信息列表(算法2结果数据)
     * @return
     */
    private ByteArrayOutputStream generatePdf(String userId, String inquirer, Image image, List<Map> companyinfos, List<Map> businforelations, List<Map> holderrelation, List<Map> managerrelation) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, output);
        writer.setPageEvent(new CustdigPageEventHelper());
        document.open();
        //1.生成首页
        PdfUtils.generateHomePage(document,inquirer,userId);

        //2.概要
        String promt = getContent(businforelations,holderrelation,managerrelation,companyinfos);
        generatePromtPage(document, promt);

        //3.图谱部分
        document.newPage();
        PdfPTable chpterTitleTable = PdfUtils.getChpterTitleTable("一、查询结果");
        document.add(chpterTitleTable);
        if(businforelations.size() == 0 && holderrelation.size() == 0 && managerrelation.size() == 0){
            Paragraph retitle = new Paragraph("经查询，上传公司暂无关联关系", PdfUtils.SIMHEIFONT13);
            retitle.setAlignment(Element.ALIGN_LEFT);
            retitle.setSpacingBefore(20f);
            document.add(retitle);
        }else{
            Paragraph retitle = new Paragraph("经查询，上传公司的结果如下：", PdfUtils.SIMHEIFONT13);
            retitle.setAlignment(Element.ALIGN_LEFT);
            retitle.setSpacingBefore(20f);
            document.add(retitle);
//        Image image = getImageFromUrl(urlpath,writer);
            PdfUtils.canvasAtlas(document,image);
        }

        //4.上传企业列表
        PdfPTable uploadEntTitle = PdfUtils.getChpterTitleTable("二、上传公司");
        document.add(uploadEntTitle);
//        PdfUtils.addSubTitle(document, "企业列表", "atlas");
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
        PdfPTable listtable = PdfUtils.generateTable(companyinfos,keys,col_titles,widths);
        listtable.setSpacingBefore(40f);
        document.add(listtable);

        //5.关联关系
        PdfPTable relationTitle = PdfUtils.getChpterTitleTable("三、关联关系");
        document.add(relationTitle);

        if(businforelations.size() == 0 && holderrelation.size() == 0 && managerrelation.size() == 0){
            Paragraph nontitle = new Paragraph("经查询，上传公司暂无关联关系", PdfUtils.SIMHEIFONT13);
            nontitle.setAlignment(Element.ALIGN_LEFT);
            nontitle.setIndentationRight(20f);
            nontitle.setSpacingBefore(40f);
            document.add(nontitle);
        }else{
            //5.1工商信息
            String retitle1content = "3.1、工商信息是否重复";
            if(businforelations.size() > 0){
//                retitle1content += "\n如有:";
            }else{
                retitle1content += "\n暂无关系";
            }
            Paragraph retitle1 = new Paragraph(retitle1content, PdfUtils.SIMHEIFONT13);
            retitle1.setAlignment(Element.ALIGN_LEFT);
            retitle1.setIndentationRight(20f);
            document.add(retitle1);
            List<String> busiinfocontents = new ArrayList<>();
            for (Map relationship : businforelations) {
                String content = (String) relationship.get("relship_info");
                busiinfocontents.add(content);
            }
            PdfPTable busiinforelationTable = PdfUtils.generateTextList(busiinfocontents);//生成文本列表
            document.add(busiinforelationTable);


            //5.2持股
            String retitle2content = "3.2、一方直接或者间接持有另一方的股份总和达到25%以上，双方直接或者间接为第三方所持有的股份达到25%以上";
            if(holderrelation.size() > 0){
//                retitle2content += "\n如有:";
            }else{
                retitle2content += "\n暂无关系";
            }
            Paragraph retitle2 = new Paragraph(retitle2content, PdfUtils.SIMHEIFONT13);
            retitle2.setAlignment(Element.ALIGN_LEFT);
            retitle2.setIndentationRight(20f);
            retitle2.setSpacingBefore(15f);
            document.add(retitle2);
            List<String> holdercontents = new ArrayList<>();
            for (Map relationship : holderrelation) {
                String content = (String) relationship.get("relship_info");
                holdercontents.add(content);
            }
            PdfPTable holderrelationTable = PdfUtils.generateTextList(holdercontents);//生成文本列表
            document.add(holderrelationTable);

            //5.3高管
            String retitle3content = "3.3、双方或多方是否存在交叉任职";
            if(managerrelation.size() > 0){
//                retitle3content += "\n如有:";
            }else{
                retitle3content += "\n暂无关系";
            }
            Paragraph retitle3 = new Paragraph(retitle3content, PdfUtils.SIMHEIFONT13);
            retitle3.setAlignment(Element.ALIGN_LEFT);
            retitle3.setIndentationRight(20f);
            retitle3.setSpacingBefore(15f);
            document.add(retitle3);
            List<String> managercontents = new ArrayList<>();
            for (Map relationship : managerrelation) {
                String content = (String) relationship.get("relship_info");
                managercontents.add(content);
            }
            PdfPTable managerrelationTable = PdfUtils.generateTextList(managercontents);//生成文本列表
            document.add(managerrelationTable);
        }

        //6.生成末页
//        PdfUtils.generateLastPage(document,writer);
        document.close();
        return output;
    }

    /**
     * 获取概要内容
     * @param businforelations
     * @param holderrelation
     * @param managerrelation
     * @param companyinfos
     * @return
     */
    private String getContent(List<Map> businforelations, List<Map> holderrelation, List<Map> managerrelation, List<Map> companyinfos) {
        StringBuilder sb = new StringBuilder();
        if(businforelations.size() == 0 && holderrelation.size() == 0 && managerrelation.size() == 0){
            sb.append("经查询，上传公司暂无关联关系。");
        }else{
            sb.append("经查询，上传公司具备如下关联关系，");
            List<String> list = new ArrayList<>();
            String busiinfoContent = getFirstContent(businforelations);
            if(null != busiinfoContent){
                list.add(busiinfoContent);
            }
            String managerContent = getFirstContent(managerrelation);
            if(null != managerContent){
                list.add(managerContent);
            }
            String holderContent = getFirstContent(holderrelation);
            if(null != holderContent){
                list.add(holderContent);
            }
//            if(log.isDebugEnabled()){
//                for (Map companyinfo : companyinfos) {
//                    log.debug(companyinfo.toString());
//                }
//            }
//            if(companyinfos.size() > 0){
//                Map map = companyinfos.get(0);
//                String entname = getFromMap(map,"entname", null); //公司名称
//                String uniscid = getFromMap(map,"uniscid",null);	//统一社会信用代码
//                String legal_man = getFromMap(map,"legal_man",null);	//法人姓名
//            String legal_id = getFromMap(map,"legal_id",null);	//法人身份证号
//            String legal_other_custname = getFromMap(map,"legal_other_custname",null);	//法人名下其他企业
//                String name = getFromMap(map,"name",null);	//公司高管名称
//            String actual_controller = getFromMap(map,"actual_controller",null);	//公司实际控制人
//            String altimate_beneficiary = getFromMap(map,"altimate_beneficiary",null);	//最终受益人
//            String conprop = getFromMap(map,"conprop",null);	//股东持股比例
//            String subconam = getFromMap(map,"subconam",null);	//股东认缴出资额
//            String rgtered_tel = getFromMap(map,"rgtered_tel",null);	//注册电话
//                String rgtered_adress = getFromMap(map,"rgtered_adress",null);	//注册地址
//            String rgtered_email = getFromMap(map,"rgtered_email",null);	//注册邮箱
//                StringBuilder contentBuilder = new StringBuilder();
//                contentBuilder.append(entname);
//                boolean showcompany = false;
//                List<String> basiclist = new ArrayList<>();
//                if(null != uniscid && uniscid.length() > 0){
//                    contentBuilder.append("，").append("统一社会信用代码是").append(uniscid);
//                    showcompany = true;
//                }
//                if(null != legal_man && legal_man.length() > 0){
//                    contentBuilder.append("，").append("法人代表是").append(legal_man);
//                    showcompany = true;
//                }
////                if(null != name && name.length() > 0){
////                    contentBuilder.append("，").append("该公司的高管有").append(name);
////                    showcompany = true;
////                }
//                if(null != rgtered_adress && rgtered_adress.length() > 0){
//                    contentBuilder.append("，").append("在").append(rgtered_adress).append("注册");
//                    showcompany = true;
//                }
//                if(showcompany){
//                    list.add(contentBuilder.toString());
//                }
//            }
            for (int i = 0;i < list.size();i++) {
                String subpromt = list.get(i);
                sb.append(subpromt);
                if(i == (list.size() - 1)){
                    sb.append("。");
                }else{
                    sb.append("；");
                }
            }
        }
        return sb.toString();

    }

    private String getFirstContent(List<Map> relations) {
        if(relations.size() > 0){
            Map map = relations.get(0);
            return getFromMap(map,"relship_info",null);
        }
        return null;
    }

    /**
     * 生成概要页面
     * @param document
     * @param content
     * @throws DocumentException
     */
    private void generatePromtPage(Document document, String content) throws DocumentException {
        document.newPage();
        PdfPTable promtTable = PdfUtils.getChpterTitleTable("摘要");
        document.add(promtTable);
        Paragraph promttitle = new Paragraph(content, PdfUtils.SIMHEIFONT13);
        promttitle.setAlignment(Element.ALIGN_LEFT);
        promttitle.setSpacingBefore(20f);
        document.add(promttitle);
    }

    /**
     * 从链接获取图片
     * @param urlpath
     * @param writer
     * @return
     * @throws IOException
     * @throws BadElementException
     */
    private Image getImageFromUrl(String urlpath, PdfWriter writer) throws IOException, BadElementException {
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
        ImgTemplate img = new ImgTemplate(template);
        return img;
    }

    /**
     * 通过企业名单获取企业关系信息(是否共用办公电话等等)
     * @param companys
     * @return
     */
    private List<Map> getCompanyRelationData(List<String> companys) {
        InternalWrapper wrapper = internalSearchWS.findCustByname(companys); //获取公司id集合
        if (null != wrapper && null != wrapper.getStatus() && wrapper.getStatus() == 0) {
            Object data = wrapper.getData();
            String jsonStr = JSON.toJSONString(data);
            List<String> companyids = JSONArray.parseArray(jsonStr, String.class);
            List<Map> result = companyDigResultRepo.findCompanyRelations(companyids,new String[]{"1","2","3","4","5","6","7"});
            return result;
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * 通过企业名单获取企业信息数据
     * @param companys
     * @return
     */
    private List<Map> getCompanyInfosData(List<String> companys) {
        List<Map> result = companyDigResultRepo.findCompanyInfos(companys);
        for (Map map : result) {
            String legal_man = getFromMap(map,"legal_man",""); //法人姓名
            String legal_id = getFromMap(map,"legal_id",""); //法人身份证号
            String legal_other_custname = getFromMap(map,"legal_other_custname",""); //法人名下其他企业
            String legal_info = new StringBuilder().append("姓名:").append(legal_man).append("\n")
//                    .append("身份证:").append(legal_id).append("\n")
                    .append("名下企业:").append(legal_other_custname).toString();
            map.put("legal_info",legal_info); //法定代表人信息

            String conprop = getFromMap(map,"conprop",""); //股东持股比例
            String subconam = getFromMap(map,"subconam",""); //股东认缴出资额
            String coninfo = new StringBuilder().append("持股比例:").append(conprop).append("\n").append("认缴出资:").append(subconam).toString();
            map.put("coninfo",coninfo);

            String rgtered_tel = getFromMap(map,"rgtered_tel",""); //注册电话
            String rgtered_adress = getFromMap(map,"rgtered_adress",""); //注册地址
            String rgtered_email = getFromMap(map,"rgtered_email",""); //注册邮箱
            String rgtered_info = new StringBuilder().append("电话:").append(rgtered_tel).append("\n")
                    .append("地址:").append(rgtered_adress).append("\n")
                    .append("邮箱:").append(rgtered_email).toString();
            map.put("rgtered_info",rgtered_info);
        }
        return result;
    }

    private String getFromMap(Map map, String key, String s) {
        Object obj = map.get(key);
        if(null != obj){
            return obj.toString();
        }
        return s;
    }

    /**
     * 保存图挖掘的结果
     * @param batchid 批次号
     * @param content 结果内容
     */
    private void saveGraphResult(String batchid, String content) {
        String offsetTime = DateUtils.getOffsetTime(resultExpireUnit, resultExpire); //失效时间
        batchResultRepo.deleteByBatchid(batchid); //先删除之前保存的结果
        int id = batchResultRepo.insert(batchid, content, offsetTime); //重新插入，并设置失效的时间
        if(id == -1){
            throw new RuntimeException("save graph data failed ");
        }
    }

//    /**
//     * 处理图数据
//     * @param data
//     * @return
//     */
//    private Map dealGraph(List<Map> data) {
//        List<Map> finaledges = new ArrayList<>();
//        List<Map> finalvertices = new ArrayList<>();
//        Set<String> idSet = new HashSet<>();
//        for (Map map : data) {
//            List<Map> edges = (List<Map>) map.get("edges");
//            for (Map edge : edges) {
//                String edgid = (String) edge.get("_id");
//                boolean add = idSet.add(edgid);
//                if(add){
//                    finaledges.add(edge);
//                }
//            }
//            List<Map> vertices = (List<Map>) map.get("vertices");
//            for (Map vertex : vertices) {
//                String vertexid = (String) vertex.get("_id");
//                boolean add = idSet.add(vertexid);
//                if(add){
//                    finalvertices.add(vertex);
//                }
//            }
//        }
//        Map graph = new HashMap();
//        graph.put("edges",finaledges);
//        graph.put("vertices",finalvertices);
//        return graph;
//    }

    // 截图
    public String screenShot(String batchid) throws IOException, BadElementException {
        String screenshotName = batchid + GRAPH_SUFFIX;
        ClassRelativeResourceLoader loader = new ClassRelativeResourceLoader(CustdigExportProcess.class);
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
//            String systemName = "linux";
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
            sb.append(scriptsPath).append("/phantomjs/bin/").append(pjsName);
            sb.append(" --web-security=no ");
            sb.append(scriptsPath).append("/cropper_custdig.js ");
            String batchidEncode = URLEncoder.encode(batchid,"utf8"); //避免等号问题
            sb.append(atlasAddress.replace("@batchid",batchidEncode));
            sb.append(" ").append(scriptsPath).append("/tmp/");
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

    /**
     * 更新任务进度
     * @param task
     * @param percent
     */
    private void updateTaskPercent(Task task,double percent){
        task.setPercent(percent);
        taskRepo.update(task);
    }


}
