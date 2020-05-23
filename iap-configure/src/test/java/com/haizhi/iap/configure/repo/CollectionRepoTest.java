package com.haizhi.iap.configure.repo;

import com.alibaba.fastjson.JSON;
import com.haizhi.iap.configure.model.Param;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * Created by yuding on 2018/7/10.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})

public class CollectionRepoTest {

    @Autowired
    private CollectionRepo collectionRepo;

    @Test
    public void getCollectionByNameAndConditionTest() {

        Param param = new Param();
        param.setId(1l);
        param.setIsDesc(1);
        param.setOrderFieldName("acquirer_industry");
        param.setOrderKey(1l);
        param.setIsOrder(1);

        List<Map> list = collectionRepo
                .getCollectionByNameAndCondition("acquirer_event", "source_site",
                        0, 3, "私募通", param);
        String resp = JSON.toJSONString(list);
        System.out.println(resp);
    }

    @Test
    public void countAllByNameAndConditionTest() {
        Long num = collectionRepo
                .countAllByNameAndCondition("acquirer_event", "source_site",
                        "私募通");
        System.out.println(num);
    }


}
