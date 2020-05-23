package com.haizhi.iap.tag.component;

import com.haizhi.iap.tag.recognizer.RecognizerManager;
import com.haizhi.iap.tag.service.TagInfoService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * @Author dmy
 * @Date 2017/11/4 下午2:53.
 */
@Component
public class TaskProcessor {

    @Setter
    @Autowired
    TagInfoService tagInfoService;

    @PostConstruct
    @Scheduled(cron = "${quartz.interval.tag}")
    public void process(){
        tagInfoService.buildTagDictionary();
    }
}
