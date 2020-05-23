package com.haizhi.iap.configure.component;

import com.haizhi.iap.configure.enums.ImportStatus;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.repo.DataSourceRepo;
import com.haizhi.iap.configure.repo.RedisRepo;
import com.haizhi.iap.configure.service.DataSourceService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by chenbo on 2017/10/31.
 */
@Slf4j
@Component
public class TaskDispatcher {
    @Setter
    @Autowired
    DataSourceService dataSourceService;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @PostConstruct
    public void processShutDownTask() {
        //设置锁
        if(redisRepo.getLock() == null){
            redisRepo.pushLock();
            List<DataSourceConfig> sourceConfigList = dataSourceService.findByStatus(ImportStatus.IMPORTING);

            for (DataSourceConfig config : sourceConfigList) {
                //更新actual_num（需要抽成service的一个方法）
                dataSourceService.updateConfigStatusWithActualNum(config, ImportStatus.FAILED);
                log.warn("由于上次程序异常退出, 将任务{} {}置为失败", config.getId(), config.getName());
            }
            redisRepo.releaseLock();
        }

    }
}
