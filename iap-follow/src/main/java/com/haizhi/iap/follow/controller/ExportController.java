package com.haizhi.iap.follow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.utils.ConfUtil;
import com.haizhi.iap.follow.controller.model.TaskView;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.FollowList;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.repo.FollowItemRepo;
import com.haizhi.iap.follow.repo.FollowListRepo;
import com.haizhi.iap.follow.repo.TaskRepo;
import com.haizhi.iap.follow.service.FileUploadService;
import com.haizhi.iap.follow.service.TaskProcessor;
import com.haizhi.iap.follow.service.TaskService;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/1/9.
 */
@Api(tags="【关注-导出模块】导出数据")
@Slf4j
@RestController
@RequestMapping(value = "/follow/export")
public class ExportController {
    @Setter
    @Autowired
    FollowItemRepo followItemRepo;

    @Setter
    @Autowired
    FileUploadService fileUploadService;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    FollowListRepo followListRepo;

    @Setter
    @Autowired
    TaskRepo taskRepo;

    @Setter
    @Autowired
    TaskService taskService;

    @Setter
    @Autowired
    TaskProcessor taskProcessor;

    private static String UPLOAD_DIR = "bigdata/exportimages";

    /**
     * multipart/form-data 上传
     *
     * @return
     */
    @RequestMapping(value = "/task_add_pdf", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    public Wrapper addTaskPdfExcel(@RequestParam(value = "file[]", required = false) MultipartFile[] multipartfiles,
                                   @RequestParam(value = "task_name", required = false) String taskName,
                                   @RequestParam(value = "follow_list_id", required = false) Long followListId,
                                   @RequestParam(value = "data_type", required = false) String dataType,
                                   @RequestParam(value = "begin_date", required = false) Long beginDate,
                                   @RequestParam(value = "end_date", required = false) Long endDate,
                                   @RequestParam(value = "time_option", required = false) String timeOption,
                                   @RequestParam(value = "type", required = false) String type,
                                   // @RequestParam(value = "is_single", required=false) Boolean isSingle,
                                   @RequestParam(value = "company_names", required = false) String companyNames,
                                   @RequestParam(value = "expire_days", required = false) Long expireDays,
                                   @RequestParam(value = "mode", required = false) String mode,
                                   @RequestParam(value = "method", required = false) String method,
                                   @RequestParam(value = "img_intro_list", required = false) String imgIntroListString) throws IllegalStateException, IOException {
        TaskView view = new TaskView();
        view.setTaskName(taskName);
        view.setFollowListId(followListId);
        view.setDataType(dataType);
        view.setBeginDate(beginDate);
        view.setEndDate(endDate);
        view.setTimeOption(timeOption);
        view.setType(type);
        view.setCompanyNames(companyNames);
        view.setExpireDays(expireDays);
        view.setMode(mode);
        view.setMethod(method);
        if (Strings.isNullOrEmpty(view.getTaskName())) {
            return FollowException.NO_TASK_NAME.get();
        } else if (view.getTaskName().contains("/") ||
                view.getTaskName().contains("@") ||
                view.getTaskName().contains("#") ||
                view.getTaskName().contains("$") ||
                view.getTaskName().contains("&")) {
            return FollowException.SPECIAL_CHAR.get();
        } else if (view.getTaskName().length() > 64) {
            return FollowException.OVER_LIMIT_NAME.get();
        }
        if (null == imgIntroListString) {
            view.setImgIntroListString("");
        } else {
            view.setImgIntroListString(imgIntroListString);
        }
        view.setImgPathListString("");
        StringBuilder pathstr = new StringBuilder();

        if (null != multipartfiles && multipartfiles.length > 0) {
            //文件上传到gridfs
            int len = multipartfiles.length;
            for (int i = 0; i < len; i++) {
                String fileName = multipartfiles[i].getOriginalFilename();
                String fileDownLoadUrl = fileUploadService.saveFile(multipartfiles[i].getBytes(), UPLOAD_DIR, fileName);
                pathstr.append(fileDownLoadUrl);
                if (i < len - 1) {
                    pathstr.append("*");
                }
            }
        }
        String pathStr = pathstr.toString();
        view.setImgPathListString(pathStr);
        // type 是必须的
        if (Strings.isNullOrEmpty(view.getType())) {
            return FollowException.MISS_TYPE.get();
        }

        if (!type.equals("pdf")) {
            return FollowException.WRONG_TYPE.get();
        }

        Task task = taskRepo.findByName(view.getTaskName());
        if (task != null) {
            return FollowException.ALEARDY_HAS_TASK_NAME.get();
        }

        taskService.create(view);

        return Wrapper.OK;
    }

    @RequestMapping(value = "/task_add", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper addTask(@RequestBody TaskView view) {
        if (Strings.isNullOrEmpty(view.getTaskName())) {
            return FollowException.NO_TASK_NAME.get();
        } else if (view.getTaskName().contains("/") ||
                view.getTaskName().contains("@") ||
                view.getTaskName().contains("#") ||
                view.getTaskName().contains("$") ||
                view.getTaskName().contains("&")) {
            return FollowException.SPECIAL_CHAR.get();
        } else if (view.getTaskName().length() > 64) {
            return FollowException.OVER_LIMIT_NAME.get();
        }

        if (Strings.isNullOrEmpty(view.getTimeOption())) {
            return FollowException.NO_TIME_OPTION.get();
        }

        if (view.getBeginDate() == null || view.getEndDate() == null) {
            return FollowException.NO_TASK_DATE.get();
        }

        if (view.getFollowListId() != null && Strings.isNullOrEmpty(view.getCompanyNames())) {
            //导出多个企业
            //return FollowException.MISS_COMPANY_NAME.get();
        } else {
            view.setCompanyNames(view.getCompanyNames().replaceAll("，", ","));
        }

        if (view.getBeginDate() >= view.getEndDate()) {
            return FollowException.BEGIN_TIME_T_START_TIME.get();
        }

        Task task = taskRepo.findByName(view.getTaskName());
        if (task != null) {
            return FollowException.ALEARDY_HAS_TASK_NAME.get();
        }
        view.setIsSingle(false);
        view.setType("excel");

        taskService.create(view);

        return Wrapper.OK;
    }

    @RequestMapping(value = "/task_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper taskList(@RequestParam("offset") Integer offset,
                            @RequestParam("count") Integer count,
                            @RequestParam(value = "type", required = false) String type,
                            @RequestParam(value = "fuzzy_name", required = false) String fuzzyKey) {
        if (offset == null) {
            offset = 0;
        }

        if (count == null) {
            count = 10;
        }

        Long userId = DefaultSecurityContext.getUserId();
        Map<String, Object> data = Maps.newHashMap();

        List<Task> tasks = taskRepo.findByCondition(userId, offset, count, type, fuzzyKey);
        for (Task task : tasks) {
            BigDecimal decimal = new BigDecimal(task.getExportFileLength() == null ?
                    0 : task.getExportFileLength() / 1024.0 / 1024.0);
            task.setFileSize(decimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            task.setFileSizeUnit("MB");

            FollowList followList = followListRepo.findById(task.getFollowListId());
            if (followList != null) {
                task.setFollowListName(followList.getName());
            }
        }
        data.put("data", tasks);
        data.put("total", taskRepo.countAll(userId, type, fuzzyKey));
        return Wrapper.OKBuilder.data(data).build();
    }


    @RequestMapping(value = "/task/{task_id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper deleteTask(@PathVariable("task_id") Long taskId) {
        if (taskId == null) {
            return FollowException.MISS_TASK_ID.get();
        }
        Task task = taskRepo.findById(taskId);
        if (task == null || !task.getUserId().equals(DefaultSecurityContext.getUserId())) {
            return FollowException.NO_THIS_TASK.get();
        }
        taskRepo.updateStatus(taskId, TaskStatus.DELETED.getCode());
        taskRepo.delete(taskId);
        if (taskProcessor.pool.get(taskId) != null) {
            try {
                taskProcessor.pool.get(taskId).interrupt();
                log.info("shut thread from export");
            } catch (Exception ex) {
                log.error("{}", ex);
            }
        }
        return Wrapper.OK;
    }

    @RequestMapping(value = "/cancel_task/{task_id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper cancelTask(@PathVariable("task_id") Long taskId) {
        if (taskId == null) {
            return FollowException.MISS_TASK_ID.get();
        }
        Task task = taskRepo.findById(taskId);
        if (task == null || !task.getUserId().equals(DefaultSecurityContext.getUserId())) {
            return FollowException.NO_THIS_TASK.get();
        }
        taskRepo.updateStatus(taskId, TaskStatus.CANCELED.getCode());
        if (taskProcessor.pool.get(taskId) != null) {
            try {
                taskProcessor.pool.get(taskId).interrupt();
                log.info("shut thread from export");
            } catch (Exception ex) {
                log.error("{}", ex);
            }
        }
        return Wrapper.OK;
    }

    @RequestMapping(value = "/restart_task/{task_id}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper restartTask(@PathVariable("task_id") Long taskId) {
        if (taskId == null) {
            return FollowException.MISS_TASK_ID.get();
        }
        Task task = taskRepo.findById(taskId);
        if (task == null || !task.getUserId().equals(DefaultSecurityContext.getUserId())) {
            return FollowException.NO_THIS_TASK.get();
        }
        taskRepo.updateStatus(taskId, TaskStatus.WAITING.getCode());
        return Wrapper.OK;
    }

    @RequestMapping(value = "/pdf_report_options", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getPdfReportOptions() {
        Map data = ConfUtil.readJson("pdf_report_params.json");
        return Wrapper.OKBuilder.data(data).build();
    }

    @RequestMapping(value = "/preview_screenshot", method = RequestMethod.GET)
    public Wrapper previewScreenShot(@RequestParam("comapny_name") String companyName, HttpServletRequest request, HttpServletResponse response) {
        String filePath = taskRepo.screenShot(companyName);
        // String fileName = "哈哈.png";
        //文件所在目录路径
        // String filePath = "/Users/zhutianpeng/Documents/project/zhaohang/iap/iap-follow/target/classes/screenshot-scripts/tmp/";
        //得到该文件
        File file = new File(filePath);
        if(!file.exists()){
            System.out.println("Have no such file!");
        }

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //设置Http响应头告诉浏览器下载这个附件
        try {
            response.setHeader("Content-Disposition", "attachment;Filename=" + URLEncoder.encode(companyName + ".png", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return FollowException.PDF_SCREEN_SHOT_FAILED.get();
        }
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return FollowException.PDF_SCREEN_SHOT_FAILED.get();
        }
        byte[] bytes = new byte[4096];
        int len = 0;
        try {
            while ((len = fileInputStream.read(bytes))>0){
                outputStream.write(bytes,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return FollowException.PDF_SCREEN_SHOT_FAILED.get();
        }
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Wrapper.OK;
    }

}
