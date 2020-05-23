package com.haizhi.iap.follow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haizhi.iap.common.bean.CustdigParam;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.ConfUtil;
import com.haizhi.iap.follow.controller.InternalSearchWS;
import com.haizhi.iap.follow.controller.model.InternalWrapper;
import com.haizhi.iap.follow.enums.TaskMode;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.enums.TimeOption;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.model.atlas.*;
import com.haizhi.iap.follow.repo.BatchResultRepo;
import com.haizhi.iap.follow.repo.CompanyDigResultRepo;
import com.haizhi.iap.follow.repo.CustuploadRepo;
import com.haizhi.iap.follow.repo.TaskRepo;
import com.haizhi.iap.follow.service.CustdigExportProcess;
import com.haizhi.iap.follow.service.CustdigService;
import com.haizhi.iap.follow.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mtl
 * @Description: 客户关系挖掘
 * @date 2020/3/10 11:34
 */
@Slf4j
@Service
public class CustdigServiceImpl implements CustdigService {

    private static final Long DEFAULT_EXPIRE_DAYS = 2l;
    private static final int STATUS_OLD = 0;
    private static final int STATUS_NEW = 1;
    public static final String DEFAULT_USER_ID = "110";
    public static final String NONE_USER_NAME = "NONE";


    @Autowired
    private CustuploadRepo custuploadRepo;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private BatchResultRepo batchResultRepo;

    @Value("${atlas.edge.tablenames:te_holder,te_manager}")
    private String edges;

    @Value("${atlas.dig.depth:5}")
    private Integer depth;

    @Value("${atlas.dig.direct:out}")
    private String direct;

    @Value("${atlas.dig.uploadsize.threshold:10}")
    private Integer uploadThreshold; //客户关系挖掘上传名单个数阈值

    @Value("${atlas.dig.result.expire.unit:HOUR}")
    private String resultExpireUnit; //客户关系挖掘结果超时单位

    @Value("${atlas.dig.result.expire.value:1}")
    private Integer resultExpire; //客户关系挖掘结果超时时间

    @Autowired
    private InternalSearchWS internalSearchWS;

    @Autowired
    private CompanyDigResultRepo companyDigResultRepo;

    @Value("${test.sql:select 1}")
    private String sql;

    @Override
    public Task saveListAndCreateTask(byte[] data, String useridStr, String username) {
        if(StringUtils.isEmpty(useridStr)){
            useridStr = DEFAULT_USER_ID; //默认的useridStr用110
        }
        Long userid = Long.parseLong(useridStr);
//        Long userid = DefaultSecurityContext.getUserId();
        try(ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            Workbook wb = WorkbookFactory.create(input);
            Sheet sheet = wb.getSheetAt(0);

            //1.读取上传企业列表
            List<Map> list =  readContent(sheet,2,new String[]{"serial","company","authperson","idnumber","phone","applydate"});
            if(null == list || list.size() == 0){
                throw new ServiceAccessException(FollowException.EMPTY_IMPORT);
            }
            //取消限制，传一家企业就只有一个孤点
//            if(list.size() < 2){
//                throw new ServiceAccessException(FollowException.IMPORT_MIN_LIMIT_TWO);
//            }
            if(list.size() > uploadThreshold){ //超过上传名单阈值
                throw new ServiceAccessException(FollowException.IMPORT_BEYOND_THRESHOLD.set("size",uploadThreshold.toString()));
            }

            String current = DateUtils.format(new Date(), DateUtils.FORMAT_YMS);
            String content = userid + current; //用户id + 时间
            String batchid = generateBatchid(content); //批次号
            log.info("upload list size:{}",list.size());

            //2.保存企业列表
            saveList(list,batchid,userid); //保存名单

            log.info("save list to db success");

            //3.对企业列表进行客户关系挖掘，并且保存挖掘结果
            custdigAndSave(list,batchid);

            log.info("searchperson:{},userid:{}",username,useridStr);
            //创建任务
            Task param = new Task();
            //设置任务信息
            param.setName("招中标信息["+content+"]");
            param.setTimeOption(TimeOption.DATA_TIME.getCode());
            param.setExpireDays(DEFAULT_EXPIRE_DAYS);
            param.setMode(TaskMode.ON.getCode());
            param.setDataType(batchid);
            param.setUserId(useridStr);
            param.setStatus(TaskStatus.WAITING.getCode());
            param.setType("custdig");
            if(null == username || username.length() == 0){ //如果名字为空就是用userid
                username = NONE_USER_NAME;
            }
            param.setCompanyNames(username); //此处暂时用于存储查询人的名字
            Task task = taskRepo.create(param);
            log.info("create task success");
            return task;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("{}",e);
            throw new ServiceAccessException(FollowException.READ_IMPORT_ERROR);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            log.error("{}",e);
            throw new ServiceAccessException(FollowException.READ_IMPORT_ERROR);
        }
    }

    /**
     * 进行客户关系挖掘，并且保存结果
     * @param list
     * @param batchid
     */
    private void custdigAndSave(List<Map> list, String batchid) {
        List<String> companys = list.stream()
                .map(row -> row.get("company").toString())
                .collect(Collectors.toList());
        CustdigParam param = new CustdigParam();
        param.setCompanys(companys);
        param.setType(edges); //设置挖掘边类型
        param.setDepth(depth); //设置挖掘深度
        param.setDirect(direct); //设置边方向(出OUT，入IN，双向ANY)

        InternalWrapper wrapper = internalSearchWS.custdig(param); //查询实体之间的5度关系数据

        log.info("internal search result:[status:{},msg:{}]",wrapper.getStatus(),wrapper.getMsg());

        if (null != wrapper && null != wrapper.getStatus() && wrapper.getStatus() == 0) {
            Map graph = (Map) wrapper.getData();
//                Map graph = dealGraph(data);
            //保存图数据信息到mysql库
            String content = JSON.toJSONString(graph);
            saveGraphResult(batchid,content);
        }
    }


    /**
     * 保存挖掘结果
     * @param batchid
     * @param content
     */
    private void saveGraphResult(String batchid, String content) {
        String expireTime = DateUtils.getOffsetTime(resultExpireUnit, resultExpire); //数据失效时间
        batchResultRepo.deleteByBatchid(batchid);
        int id = batchResultRepo.insert(batchid, content,expireTime);
        if(id == -1){
            throw new RuntimeException("save graph data failed ");
        }
    }

    private String generateBatchid(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes(Charset.forName("UTF-8")));
//        return Base64.getEncoder().encodeToString(content.getBytes(Charset.forName("UTF-8")));
    }



    @Override
    public AtlasResponse getData(AtlasRequest request) {
        String batchid = request.getBatchid();
        List<Map> list = batchResultRepo.findByBatchid(batchid);
        if(null == list || list.size() == 0){
            return new AtlasResponse("false","未查询到图数据",null,"1.0");
        }
        Map map = list.get(0);
        String content = (String) map.get("content");
        JSONObject data = JSON.parseObject(content);
        AtlasData atlasData = parseData(data);
        Map payload = new HashMap();
        payload.put("data",atlasData);
        return new AtlasResponse("true","成功",payload,"1.0");
    }

    @Override
    public List<Map> queryUploadList(String useridStr) {
        Long userid = Long.parseLong(useridStr);
//        Long userid = DefaultSecurityContext.getUserId();
        List<Map> list = custuploadRepo.findByUserid(userid.toString(),STATUS_NEW);
        return list;
    }

    /**
     * 把图数据转化为图平台需要的数据
     * @param data
     * @return
     */
    private AtlasData parseData(JSONObject data) {
        Map<String, Object> atlasSrc = ConfUtil.readJson("/atlas.json");
        List<Schema> schemas = parseSchemas(atlasSrc); //解析schemas
        List<UiConfig> uiConfigs = parseUiConfigs(atlasSrc); //解析uiConfigs

        List<Map> edgesSrc = (List<Map>) data.get("edges");

        dealEdges(edgesSrc,schemas); //根据Schema配置，处理边数据
//        List<Edge> edges = parseEdges(edgesSrc);
        List<Map> verticesSrc = (List<Map>) data.get("vertices");
//        List<Vertex> vertices = parseVertices(verticesSrc);
        //读取schemas和uiConfigs的配置json
        AtlasData atlasData = new AtlasData(verticesSrc,edgesSrc,schemas,uiConfigs);
        return atlasData;
    }

    /**
     * 根据Schema配置，处理图内的边数据
     * @param edgesSrc
     * @param schemas
     */
    private void dealEdges(List<Map> edgesSrc, List<Schema> schemas) {
        if(null == edgesSrc || null == schemas || edgesSrc.size() == 0 || schemas.size() == 0) return;
        //正则匹配变量格式如：${xxx}，并进行值设置
        Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z_]+)\\}"); //正则匹配
        Set<String> fields = new HashSet<>();
        for (Schema schema : schemas) {
            if(null == schema) continue;
            String table = schema.getSchema();
            String displayField = schema.getDisplayField();
            if(null == table){
                continue;
            }
            if(null == displayField){
                continue;
            }
            fields.clear();
            Matcher matcher = pattern.matcher(displayField);
            while (matcher.find()){
                String field = matcher.group(1); //获取字段名
                fields.add(field);
            }
            if(fields.size() == 0){
                continue;
            }
            String finaldisplay = "display"; //最后显示的字段
            schema.setDisplayField(finaldisplay);
            table = table.toLowerCase(); //统一以小写进行比对

            //往schema字段列表里添加显示字段
            addFieldToSchema(schema, finaldisplay);

            //处理边的信息
            for (Map edge : edgesSrc) { //
                Object edgeidObj = edge.get("_id");
                if(null == edgeidObj){
                    continue;
                }
                String edgeid = edgeidObj.toString();
                String edgetype = getEdgeType(edgeid); //获取边类型
                if(table.equals(edgetype)){
                    String content = displayField; //最后显示的字段 金额：${money},比例:${prop}%
                    for (String field : fields) { //"money","prop"
                        String value = getFromMap(edge, field, "");
                        content = content.replace("${"+field+"}",value);
                    }
                    edge.put(finaldisplay,content);
                }
            }
        }
    }

    /**
     * 往schema字段列表里添加显示字段
     * @param schema
     * @param finaldisplay
     */
    private void addFieldToSchema(Schema schema, String finaldisplay) {
        SchemaField schemaField = new SchemaField();
        schemaField.setField(finaldisplay);
        schemaField.setFieldNameCn("边显示内容");
        schemaField.setType(FieldType.STRING);
        List<SchemaField> fieldlist = schema.getFields();
        if(null == fieldlist){
            fieldlist = new ArrayList<>();
            schema.setFields(fieldlist);
        }
        fieldlist.add(schemaField);
    }

    private String getFromMap(Map map, String field, String rpl) {
        Object obj = map.get(field);
        if(null == obj){
            return rpl;
        }
        return obj.toString();
    }

    private String getEdgeType(String edgeid) {
        String[] split = edgeid.split("/");
        if(split.length > 1){
            return split[0].toLowerCase(); //统一以小写进行比对
        }
        return null;
    }

    /**
     * 解析uiConfigs
     * @param atlasSrc
     * @return
     */
    private List<UiConfig> parseUiConfigs(Map<String, Object> atlasSrc) {
        Object uiConfigsObj = atlasSrc.get("uiConfigs");
        String jsonString = JSON.toJSONString(uiConfigsObj);
        List<UiConfig> uiConfigs = JSONArray.parseArray(jsonString, UiConfig.class);
        return uiConfigs;
    }

    /**
     * 解析表结构
     * @param atlasSrc
     * @return
     */
    private List<Schema> parseSchemas(Map<String, Object> atlasSrc) {
        Object schemasObj = atlasSrc.get("schemas");
        String jsonString = JSON.toJSONString(schemasObj);
        List<Schema> schemas = JSONArray.parseArray(jsonString, Schema.class);
        return schemas;
    }

    /**
     * 解析实体数据
     * @param verticesSrc
     * @return
     */
    private List<Vertex> parseVertices(List<Map> verticesSrc) {
        String jsonString = JSON.toJSONString(verticesSrc);
        List<Vertex> vertices = JSONArray.parseArray(jsonString, Vertex.class);
        return vertices;
    }

    /**
     * 解析关系数据
     * @param edgesSrc
     * @return
     */
    private List<Edge> parseEdges(List<Map> edgesSrc) {
        String jsonString = JSON.toJSONString(edgesSrc);
        List<Edge> edges = JSONArray.parseArray(jsonString, Edge.class);
        return edges;
    }

    /**
     * 保存名单列表，并返回该批次号
     * @param list
     * @return
     */
    private void saveList(List<Map> list,String batchid,Long userid) {
        String useridStr = userid.toString();
        custuploadRepo.updateStatus(STATUS_OLD,useridStr); //把之前的上传记录设置为STATUS_OLD
        custuploadRepo.batchInsert(list,batchid,useridStr); //批量插入
    }

    /**
     * 读取excel内容
     * @param sheet
     * @param startrow
     * @param colArr
     * @return
     */
    private List<Map> readContent(Sheet sheet,int startrow, String [] colArr) {
        List<Map> list = new ArrayList();
        Iterator<Row> itr = sheet.rowIterator();
        int i = 0;
        while (itr.hasNext()){
            if(i >= startrow - 1){
                ++i;
                Row row = itr.next();
                Map data = toMap(row,colArr);
                if(null != data){
                    list.add(data);
                }
            }else{
                ++i;
                itr.next();
            }
        }
        return list;
    }

    /**
     * 转化为map
     * @param row
     * @param colArr
     * @return
     */
    private Map toMap(Row row, String[] colArr) {
        Map bean = new HashMap();
        int dealcol = 0;
        for (int i = 0; i < colArr.length; i++) {
            String value = getStringValue(row,i);
            String key = colArr[i];
            if(null != value && !"".equals(value.trim())){
                ++dealcol;
                bean.put(key,value.trim());
            }else{
                bean.put(key,null);
            }
        }
        if(dealcol == 0){
            return null;
        }else{
            return bean;
        }

    }

    /**
     * 获取值
     * @param row
     * @param i
     * @return
     */
    private String getStringValue(Row row, int i) {
        Cell cell = row.getCell(i);
        if(null == cell){
            return null;
        }else{
            cell.setCellType(Cell.CELL_TYPE_STRING);
            return cell.getStringCellValue();
        }
    }

    /**
     * 测试查询是否有问题
     * @return
     */
    public List<Map> test(){
        List<Map> result = companyDigResultRepo.findBySql(sql);
        return result;
    }

    @Override
    public Task getTask(long taskid) {
        Task task = taskRepo.findById(taskid);
        return task;
    }

    @Override
    public void startTask(long taskid) {
        taskRepo.updateStatus(taskid,TaskStatus.WAITING.getCode()); //更新任务的状态为等待
    }

    @Autowired
    private CustdigExportProcess custdigExportProcess;

    @Override
    public void screenShot(String batchid) {
        try {
            String finalPath = custdigExportProcess.screenShot(batchid);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

}
