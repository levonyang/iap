package com.haizhi.iap.follow.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.*;
import com.haizhi.iap.follow.repo.NotificationRepo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by haizhi on 2017/10/16.

//@WebAppConfiguration(value = "src/main/webapp")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/applicationContext.xml",
        "classpath:spring/applicationContext-data.xml",
        "file:src/main/webapp/WEB-INF/SpringMVC-servlet.xml"
})
 */
public class TestNotifyService {
/*
    //模拟request,response
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        response = new MockHttpServletResponse();
    }

    @Autowired
    NotificationRepo notificationRepo;

    @Test
    public void testGetMsgs() {
        long userId = 1L;
        ReqGetMsgs reqGetMsgs = new ReqGetMsgs();

        List<Notification> data = notificationRepo.queryByCondition(userId,reqGetMsgs);
        System.out.println(data);
    }

    @Test
    public void testReadMsg() {
        ReqEditMsgs reqEditMsgs = new ReqEditMsgs();
        reqEditMsgs.getIdList().add(11L);
        reqEditMsgs.getIdList().add(14L);
        reqEditMsgs.getIdList().add(15L);
        Long userId = 1L;
        notificationRepo.read(userId,reqEditMsgs);
    }

    @Test
    public void testMsgDetail() {
        long id = 11L;
        notificationRepo.findById(id);
    }

    @Test
    public void testGetMonitorCardList() {
        List<MonitorCard> result = null;
        int count = 0;

        result  = notificationRepo.queryMonitorCardList(1L,"MONTH",9,0);
        count = notificationRepo.countMonitorCardList(1L);

        Map<String, Object> res = new MapBuilder()
                .put("data", result)
                .put("total_count", count)
                .build();

        Wrapper wrap = Wrapper.OKBuilder.data(res).build();

        String resp = JSON.toJSONString(wrap);
        System.out.println(resp);
    }

    //设置收藏
    @Test
    public void testSetCollect() {
        List<Long> idList = new ArrayList<>();
        idList.add(378L);
        idList.add(379L);

        notificationRepo.markCollected(idList, 1);
    }

    //取消收藏
    @Test
    public void testCancleCollect() {
        List<Long> idList = new ArrayList<>();
        idList.add(378L);
        idList.add(379L);

        notificationRepo.markCollected(idList, 0);
    }

    //测试删除单条数据
    @Test
    public void testDelMsg() {
        List<ReqDelMsg> list = new ArrayList<>();
        ReqDelMsg req0 = new ReqDelMsg();
        req0.setId(378L);
        list.add(req0);

        notificationRepo.deleteMsgs(1L,list);
    }



    public void setNotificationRepo(NotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }
*/
}
