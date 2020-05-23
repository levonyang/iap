package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.component.RegisterCenter;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.repo.DataSourceRepo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskStartProcessor implements Processor {
    @Setter
    @Autowired
    RegisterCenter registerCenter;

    @Setter
    @Autowired
    DataSourceRepo dataSourceRepo;

    @Setter
    @Autowired
    ImportProcess importProcess;

    @Override
    public void process(Exchange exchange) throws Exception {
        DataSourceConfig config = (DataSourceConfig) exchange.getIn().getBody();

        if (config == null) {
            return;
        }
        //注册
        registerCenter.register(config.getId(), new Thread(() -> {
            importProcess.process(config);
        }));
        //启动
        if(registerCenter.getPool().get(config.getId()) != null){
            registerCenter.getPool().get(config.getId()).start();
        }
    }
}