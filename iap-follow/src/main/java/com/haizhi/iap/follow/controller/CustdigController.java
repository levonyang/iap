package com.haizhi.iap.follow.controller;

import com.google.common.base.Strings;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.follow.controller.model.CompanyImportAck;
import com.haizhi.iap.follow.controller.model.FileView;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.FollowList;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.model.atlas.AtlasRequest;
import com.haizhi.iap.follow.model.atlas.AtlasResponse;
import com.haizhi.iap.follow.service.CompanyImportService;
import com.haizhi.iap.follow.service.CustdigExportProcess;
import com.haizhi.iap.follow.service.CustdigService;
import com.haizhi.iap.follow.utils.MultipartFileUtil;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by linyong on 20/3/10.
 */
@Api(tags="【客户数据挖掘】导入企业信息")
@RestController
@Slf4j
@RequestMapping(value = "/follow/custdig")
public class CustdigController {

    @Autowired
    private CustdigService custdigService;

    @Autowired
    private CustdigExportProcess process;

    /**
     * multipart/form-data 上传
     * @return
     */
    @NoneAuthorization
    @RequestMapping(value = "/import/batch_import_company", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    public Wrapper batchImportCompany(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "userid",required = false) String userid,
                                      @RequestParam(value = "username",required = false) String username) {
        String filename = file.getOriginalFilename();
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            return Wrapper.error(e.getMessage());
        }
        String extention = MultipartFileUtil.readExtension(filename);
        if (extention.equals(MultipartFileUtil.XLS) || extention.equals(MultipartFileUtil.XLSX)) {
            log.info("start import ,user[userid:{},username:{}]",userid,username);
            Task task = custdigService.saveListAndCreateTask(data,userid,username); //保存名单并且创建挖掘任务
            return Wrapper.ok(task);
        } else {
            return FollowException.UNSUPPORT_FILE.get();
        }
    }

//    /**
//     * multipart/form-data 上传
//     * @return
//     */
//    @NoneAuthorization
//    @RequestMapping(value = "/import/batch_import_company", method = RequestMethod.POST,
//            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
//    public Wrapper batchImportCompany(@RequestParam("file") MultipartFile file,
//                                      @RequestParam(value = "userid",required = false) String userid,
//                                      @RequestParam(value = "username",required = false) String username) {
//        String filename = file.getOriginalFilename();
//        byte[] data;
//        try {
//            data = file.getBytes();
//        } catch (IOException e) {
//            log.error(e.getMessage(),e);
//            return Wrapper.error(e.getMessage());
//        }
//        String extention = MultipartFileUtil.readExtension(filename);
//        if (extention.equals(MultipartFileUtil.XLS) || extention.equals(MultipartFileUtil.XLSX)) {
//            Task task = custdigService.saveListAndCreateTask(data,userid,username); //保存名单并且创建挖掘任务
//            return Wrapper.ok(task);
//        } else {
//            return FollowException.UNSUPPORT_FILE.get();
//        }
//    }

    @NoneAuthorization
    @RequestMapping(value = "/downloadTemplate",method = RequestMethod.GET)
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ClassRelativeResourceLoader loader = new ClassRelativeResourceLoader(CustdigController.class);
        File file = loader.getResource("classpath:template/custdig_template.xlsx")
                .getFile();
        InputStream inputStream = new FileInputStream(file);
        OutputStream outputStream = response.getOutputStream();
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type","application/vnd.ms-excel;charset=utf-8");
        String outputname = URLEncoder.encode("招中标名单上传模板.xlsx","UTF-8");
        response.setHeader("Content-Disposition","attachment;fileName="+outputname);
        byte[] bytes = new byte[4096];
        int length;
        while((length = inputStream.read(bytes)) > 0){
            outputStream.write(bytes,0,length);
        }
        outputStream.close();
        inputStream.close();
    }

    @NoneAuthorization
    @RequestMapping(value = "/getData",method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON,produces = MediaType.APPLICATION_JSON)
    public AtlasResponse query(@RequestBody AtlasRequest request){
        AtlasResponse response = custdigService.getData(request);
        return response;
    }

    /**
     * 查询登录上传的企业名单
     * @return
     */
    @NoneAuthorization
    @RequestMapping(value = "/finduploads",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON)
    public Wrapper queryUploadList(@RequestParam("userid") String userid){
        List<Map> list = custdigService.queryUploadList(userid);
        return Wrapper.ok(list);
    }

    @NoneAuthorization
    @RequestMapping("/startProcess/{taskid}")
    public Wrapper startProcess(@PathVariable String taskid){
        String result = process.process(Long.parseLong(taskid));
        return Wrapper.ok(result);
    }

    @NoneAuthorization
    @RequestMapping(value = "/testSql",method = RequestMethod.GET)
    public Wrapper testSql(){
        List<Map> list = custdigService.test();
        return Wrapper.ok(list);
    }

    /**
     * 获取任务信息
     * @param taskid
     * @return
     */
    @NoneAuthorization
    @RequestMapping(value = "/taskinfo", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper taskList(@RequestParam("taskid") String taskid) {
        Task task = custdigService.getTask(Long.parseLong(taskid));
        if(null == task){
            return Wrapper.error("task not found");
        }
        return Wrapper.ok(task);
    }

    /**
     * 启动任务
     * @param taskid
     * @return
     */
    @NoneAuthorization
    @RequestMapping(value = "/startTask", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper startTask(@RequestParam("taskid") String taskid) {
        custdigService.startTask(Long.parseLong(taskid));
        return Wrapper.ok(null);
    }

    /**
     * 测试截图
     * @param batchid
     * @return
     */
    @NoneAuthorization
    @RequestMapping(value = "/screenShot",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON)
    public Wrapper screenShot(@RequestParam("batchid") String batchid){
        custdigService.screenShot(batchid);
        return Wrapper.ok(null);
    }

}
