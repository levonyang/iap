package com.haizhi.iap.follow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.haizhi.iap.follow.enums.TaskStatus;
import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.repo.RedisRepo;
import com.haizhi.iap.follow.repo.TaskRepo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/1/16.
 */
@Slf4j
@Service
public class TaskProcessor implements Processor {
    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    FileUploadService fileUploadService;

    @Setter
    @Autowired
    TaskRepo taskRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Setter
    @Autowired
    ExcelExportProcess excelExportProcess;

    @Setter
    @Autowired
    PDFExportProcess pdfExportProcess;

    @Setter
    @Autowired
    private CustdigExportProcess custdigExportProcess;

    public static Map<Long, Thread> pool = Maps.newConcurrentMap();

    private static final Integer MAX_POOL_SIZE = 20;

    @Override
    public void process(Exchange exchange) throws Exception {

        List<Task> taskList = taskRepo.getAll();

        for (Task task : taskList) {

            if (task.getStatus().equals(TaskStatus.WAITING.getCode())) {
                Long id = task.getId();
                //线程池中没满且池里没这个任务,其他机器也没有跑这个任务,就跑这个任务
                if (pool.keySet().size() < MAX_POOL_SIZE && pool.get(id) == null
                        && redisRepo.getTaskCache(id) == null) {

                    redisRepo.pushTaskCache(id);
                    String taskType = task.getType();
                    if (taskType.equals("pdf")) {
                        pool.put(id, new Thread(() -> {
                            pdfExportProcess.process(task.getId());
                        }));
                    } else if(taskType.equals("custdig")){
                        pool.put(id, new Thread(() -> {
                            custdigExportProcess.process(task.getId());
                        }));
                    }else{
                        pool.put(id, new Thread(() -> {
                            excelExportProcess.process(task.getId());
                        }));
                    }

                    //启动
                    pool.get(task.getId()).start();
                }

            } else if (task.getStatus().equals(TaskStatus.RUNNING.getCode())) {

                //一直处于运行状态的任务, 检查是否超时, 超时作为失败任务
                if (System.currentTimeMillis() - task.getCreateTime().getTime() > task.getExpireDays() * 24 * 60 * 60 * 1000) {
                    log.warn("task expire {}, mark as failed, expire days: {}", task.getId(), task.getExpireDays());
                    taskRepo.updateStatus(task.getId(), TaskStatus.FAILED.getCode());
                }
            } else if (task.getStatus().equals(TaskStatus.DELETED.getCode()) ||
                    task.getStatus().equals(TaskStatus.FINISHED.getCode())
                    || task.getStatus().equals(TaskStatus.FAILED.getCode())
                    || task.getStatus().equals(TaskStatus.CANCELED.getCode())) {

                redisRepo.removeTaskCache(task.getId());
                if (pool.get(task.getId()) != null) {
                    // 如果线程池里有这个任务,终止它,从池里移除,从queue里移除
                    Thread thread = pool.get(task.getId());
                    log.info("find thread from pool {}", task.getId());

                    try{
                        if(!thread.isInterrupted()){
                            thread.interrupt();
                        }
                    }catch (Exception ex){
                        log.error("{}", ex);
                    }

                    log.info("task {} remove from pool", task.getId());
                    pool.remove(task.getId());

                    if (task.getStatus().equals(TaskStatus.DELETED) && !Strings.isNullOrEmpty(task.getExportFile())) {
                        //用户删除了任务,删除文件
                        fileUploadService.delete(task.getExportFile());
                    } else if (task.getStatus().equals(TaskStatus.CANCELED)) {
                        //取消任务,更新百分比
                        task.setPercent(0d);
                        taskRepo.update(task);
                    }
                }
            }
        }

    }

}
