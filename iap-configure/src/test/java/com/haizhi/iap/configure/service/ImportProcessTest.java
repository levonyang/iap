package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.DataSourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by chenbo on 2017/10/21.
 */
public class ImportProcessTest {
    private ClassPathXmlApplicationContext context;
    private DataSourceService dataSourceService;
    private ImportProcess importProcess;

    @Before
    public void init() {
        context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml",
                "classpath:spring/applicationContext-data.xml");
        dataSourceService = context.getBean(DataSourceService.class);
        importProcess = context.getBean(ImportProcess.class);
    }

    @Test
    public void testImport(){
        DataSourceConfig config = dataSourceService.findConfigById(6L);
        importProcess.process(config);
    }
}
