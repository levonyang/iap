package com.haizhi.iap.follow.service.impl;

import com.haizhi.iap.follow.model.atlas.AtlasRequest;
import com.haizhi.iap.follow.model.atlas.AtlasResponse;
import com.haizhi.iap.follow.service.CustdigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class CustdigServiceImplTest {

    @Autowired
    private CustdigService custdigService;

    @Test
    public void getData() {
        AtlasRequest request = new AtlasRequest();
        request.setBatchid("MTAwMjM5MjIwMjAwMzEyMTEwMjMz");
        AtlasResponse data = custdigService.getData(request);
        Object payload = data.getPayload();
    }


}