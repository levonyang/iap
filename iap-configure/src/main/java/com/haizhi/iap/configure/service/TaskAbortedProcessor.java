package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.component.RegisterCenter;
import com.haizhi.iap.configure.enums.ImportStatus;
import com.haizhi.iap.configure.repo.DataSourceRepo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskAbortedProcessor implements Processor {
    @Setter
    @Autowired
    RegisterCenter registerCenter;

    @Setter
    @Autowired
    DataSourceRepo dataSourceRepo;

    @Override
    public void process(Exchange exchange) throws Exception {
        Long sourceConfigId = (Long) exchange.getIn().getBody();

        synchronized (this){
            Thread thread = registerCenter.getPool().get(sourceConfigId);
            if (thread != null) {
                try {
                    long threadId = thread.getId();
                    //先将资源释放
                    registerCenter.shut(sourceConfigId);
                    thread.join();
                    if(!thread.isInterrupted()){
                        thread.interrupt();
                    }
                    log.info("shut thread (threadId: {}, datasourceConfigId: {}) from import datasource", threadId, sourceConfigId);
                    registerCenter.getPool().remove(sourceConfigId);
                    dataSourceRepo.updateConfig(sourceConfigId, ImportStatus.ABORTED.getCode());
                } catch (Exception ex) {
                    log.error("{}", ex);
                }
            }
        }
    }

}
