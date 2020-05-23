package com.haizhi.iap.configure.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.configure.exception.ConfigException;
import com.haizhi.iap.configure.model.GraphRelation;
import com.haizhi.iap.configure.model.SearchGraphParam;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/*.xml"})
public class GraphConfigControllerTest {

    @Autowired
    private GraphConfigController graphConfigController;

    @Test
    public void testError1() {
        GraphRelation graphRelation = new GraphRelation();
        Wrapper ret = graphConfigController.saveGraph(graphRelation);
        Assert.assertEquals(ConfigException.MISS_NAME.get().json(), ret.json());
        graphRelation.setName("haha");
        ret = graphConfigController.saveGraph(graphRelation);
        Assert.assertEquals(ConfigException.MISS_SOURCE_CONFIG_ID.get().json(), ret.json());
    }

    @Test
    public void testQueryGraphDetailError2() {
        SearchGraphParam searchGraphParam = new SearchGraphParam();
        Wrapper ret = graphConfigController.queryGraphDetail(searchGraphParam);
        Assert.assertEquals(ConfigException.MISS_GRAPH_NAME.get().json(), ret.json());
        searchGraphParam.setTable("test");
        ret = graphConfigController.queryGraphDetail(searchGraphParam);
        Assert.assertEquals(ConfigException.MISS_FROM_TO_MAP.get().json(), ret.json());
    }

}
