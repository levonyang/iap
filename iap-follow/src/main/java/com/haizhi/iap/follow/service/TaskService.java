package com.haizhi.iap.follow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.follow.controller.model.TaskView;
import com.haizhi.iap.follow.enums.TaskMode;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.enums.TimeOption;
import com.haizhi.iap.follow.model.ExportImages;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.repo.ExportImagesRepo;
import com.haizhi.iap.follow.repo.FollowListRepo;
import com.haizhi.iap.follow.repo.TaskRepo;
import lombok.Setter;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by chenbo on 17/1/12.
 */
@Service
public class TaskService {
    private static final Long DEFAULT_EXPIRE_DAYS = 2l;

    @Setter
    @Autowired
    TaskRepo taskRepo;

    @Setter
    @Autowired
    ExportImagesRepo exportImagesRepo;

    @Setter
    @Autowired
    FollowListRepo followListRepo;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    @Qualifier("producerTemplate")
    ProducerTemplate template;

    public void create(TaskView view){

        Task task = new Task();
//        String taskName;
//        if (view.getTaskName() == null) {
//            Date date = new Date();
//            DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//            String companyName = view.getCompanyNames();
//            if (companyName != null && !companyName.contains(",")) {
//                taskName = view.getCompanyNames() + "_pdf_" + format.format(date);
//            } else if (view.getFollowListId() != null) {
//                taskName = followListRepo.findById(view.getFollowListId()).getName()  + format.format(date);
//            } else {
//                taskName = "task_pdf_"  + format.format(date);
//            }
//
//        } else {
//            taskName = view.getTaskName();
//        }
        task.setName(view.getTaskName());
        if (view.getBeginDate() != null) {
            task.setBeginDate(new Date(view.getBeginDate()));
        }
        if (view.getEndDate() != null) {
            task.setEndDate(new Date(view.getEndDate()));
        }
        if (view.getTimeOption() != null) {
            task.setTimeOption(TimeOption.getCode(view.getTimeOption()));
        }
        if (view.getExpireDays() == null || view.getExpireDays().equals(0l)) {
            task.setExpireDays(DEFAULT_EXPIRE_DAYS);
        }
        if (view.getMode() == null) {
            task.setMode(TaskMode.ON.getCode());
        } else {
            task.setMode(TaskMode.getCode(view.getMode()));
        }
        if (view.getFollowListId() != null) {
            task.setFollowListId(view.getFollowListId());
        }
        if (view.getDataType() != null) {
            task.setDataType(view.getDataType());
        }

        task.setUserId(DefaultSecurityContext.getUserId().toString());
        task.setStatus(TaskStatus.WAITING.getCode());
        task.setType(view.getType());
        if (view.getCompanyNames() != null) {
            task.setCompanyNames(view.getCompanyNames());
        }

        // task.setIsSingle(view.getIsSingle());

        //创建task
        taskRepo.create(task);
        //images库新增记录或修改
        if (view.getImgPathListString() != null && view.getCompanyNames() != null && view.getCompanyNames().length() > 0) {
            // comanpy_name taskId => img_path_list img_intro_list
            // view.getCompanyName()
            ExportImages exportImage =  new ExportImages();
            exportImage.setTaskId(task.getId());
            exportImage.setCompany(view.getCompanyNames());
            exportImage.setImgIntroList(view.getImgIntroListString());
            exportImage.setImgPathList(view.getImgPathListString());
            exportImagesRepo.create(exportImage);
        }

        //test:
/*
        if(template == null){
            template = camelContext.createProducerTemplate();
        }
        template.sendBody("direct:task_add", task);*/
    }
}
