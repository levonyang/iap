package com.haizhi.iap.mobile.repo;

import com.alibaba.fastjson.JSON;
import com.haizhi.iap.mobile.bean.normal.MongoQuery;
import com.haizhi.iap.mobile.bean.normal.MongoTermQuery;
import com.haizhi.iap.mobile.bean.normal.Sort;
import com.haizhi.iap.mobile.bean.normal.ToMongoQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * Created by yuding on 2018/7/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})

public class MongoRepoTest {
    @Autowired
    MongoRepo mongoRepo;

    @Test
    public void queryTest() {
        Pair query = Pair.of("code", "000001");
        ToMongoQuery toMongoQuery = new MongoTermQuery("ssgs_regular_report", 1, 3, null, null, query);
        List<Map<String, Object>> list= mongoRepo.query(toMongoQuery);
        System.out.println(JSON.toJSONString(list.size()));
    }
}
