package com.haizhi.iap.follow.repo;

import com.alibaba.fastjson.JSON;
import com.haizhi.iap.follow.model.FollowList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by haizhi on 2017/10/16.

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/applicationContext.xml",
        "classpath:spring/applicationContext-data.xml",
        "file:src/main/webapp/WEB-INF/SpringMVC-servlet.xml"
})
 */
public class TestFollowListRepo {
/*
    long itemID = 16L;
    long userID = 2L;

    @Autowired
    FollowListRepo followListRepo;

    @Test
    public void testFindById() {
        FollowList followList = followListRepo.findById(itemID);
        String resp = JSON.toJSONString(followList);
        System.out.println(resp);
    }

    @Test
    public void testFindByUserId() {
        List<FollowList> followList = followListRepo.findByUserId(userID);
        String resp = JSON.toJSONString(followList);
        System.out.println(resp);
    }

    @Test
    public void testSumItemCount() {
        Integer resp = followListRepo.sumItemCount(userID);
        System.out.println(resp);
    }

    @Test
    public void testCountByUserId() {
        Integer resp = followListRepo.countByUserId(userID);
        System.out.println(resp);
    }

    public void setFollowListRepo(FollowListRepo followListRepo) {
        this.followListRepo = followListRepo;
    }
*/
}
