package com.haizhi.iap.follow.service;

import com.haizhi.iap.common.bean.CustdigParam;
import com.haizhi.iap.follow.controller.InternalSearchWS;
import com.haizhi.iap.follow.controller.model.InternalWrapper;
import com.itextpdf.text.DocumentException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenbo on 2017/9/28.
 */
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class CustdigExportProccessTest  extends AbstractJUnit4SpringContextTests {

    @Autowired
    private CustdigExportProcess process;

    @Autowired
    private InternalSearchWS internalSearchWS;

    @Test
    public void process() throws IOException, DocumentException {
        process.process(305L);
//        process.process(224l);
//        List<String> names = new ArrayList<>();
//        names.add("阿里巴巴");
//        names.add("滴滴");
//        names.add("腾讯科技");
//        names.add("美团科技");
//        names.add("快手");
//        names.add("网易");
//        names.add("今日头条");
//        names.add("海致网络");
//        names.add("张家辉");
//        names.add("刘德华");
//        names.add("肖战");
//        names.add("周星驰");
//        names.add("周润发");
//        names.add("梁家辉");
//        names.add("测试找不到的");
//        CustdigParam param = new CustdigParam();
//        param.setCompanys(names);
//        param.setType("te_holder,te_manager");
//        param.setDepth(5);
//        InternalWrapper wrapper = internalSearchWS.custdig(param);
//        Integer code = wrapper.getCode();
    }

    @Test
    public void findbynames(){
        List<String> names = new ArrayList<>();
        names.add("阿里巴巴");
        names.add("滴滴");
        names.add("腾讯科技");
        names.add("美团科技");
        names.add("快手");
        names.add("网易");
        names.add("今日头条");
        names.add("海致网络");
        names.add("张家辉");
        names.add("刘德华");
        names.add("肖战");
        names.add("周星驰");
        names.add("周润发");
        names.add("梁家辉");
        names.add("测试找不到的");
        InternalWrapper wrapper = internalSearchWS.findCustByname(names);
        Integer code = wrapper.getCode();
    }

//    @Test
//    public void testExcel() {
//        ExcelExportProcess process = context.getBean(ExcelExportProcess.class);
//        process.process(487L);
//    }
}
