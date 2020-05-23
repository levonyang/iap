package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.search.model.vo.GroupFeatureVo;
import com.haizhi.iap.search.service.GroupFeatureService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
@WebAppConfiguration
public class GroupFeatureServiceImplTest {

    @Autowired
    private GroupFeatureService groupFeatureService;

    @Test
    public void listGroupFeature() {
        List<GroupFeatureVo> groupFeatureVos = groupFeatureService.listGroupFeature("万科企业股份有限公司", "省份");
        System.out.println(groupFeatureVos);
    }

    @Test
    public void test() {
        String format = String.format("%.2f", 12 * 100.0 / 23L)+ "%";
        System.out.println(format);
    }

    @Test
    public void getEnterpriseAggregateResult() {
    }
}