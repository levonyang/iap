package com.haizhi.iap.configure.service;

/**
 * Created by haizhi on 2018/7/11.
 */

import com.haizhi.iap.configure.enums.ImportStatus;
import com.haizhi.iap.configure.model.DataSourceConfig;
import com.haizhi.iap.configure.model.SourceFieldMap;
import com.haizhi.iap.configure.service.impl.DataSourceServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
public class DataSourceServiceImplTest {

    @Autowired
    DataSourceServiceImpl dataSourceService;

    static DataSourceConfig config = new DataSourceConfig();

    @Before
    public void setconfig(){

        config.setDatabase("qa_test");
        config.setId(1l);
        config.setName("bbb");
        config.setComment("测试");
        config.setDataType("detail");
        config.setTargetTable("test2");
        config.setUsername("root");
        config.setHost("192.168.1.56");
        config.setPassword("Haizhi@2018");
        config.setPort(3306);
        config.setActualNum(111l);
        config.setImportStatus(5);
        List<SourceFieldMap> list =new ArrayList<SourceFieldMap>();
        config.setFieldMapList(list);

    }

    @Test
    public void createTest() {
        Boolean str = dataSourceService.create(config);
        System.out.println(str);

    }
    @Test
    public void deleteTest(){
        Boolean str = dataSourceService.delete(121l);
        System.out.println(str);
    }

    @Test
    public void notifyImportTest(){
        dataSourceService.notifyImport(74l,52);
    }
    @Test
    public void stopImportTest(){
        dataSourceService.stopImport(74l);

    }
    @Test
    public void updateConfigStatusWithActualNumTest(){
        dataSourceService.updateConfigStatusWithActualNum(config, ImportStatus.FINISHED);
    }

}
