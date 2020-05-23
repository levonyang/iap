package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.Graph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by caochao on 2018/08/02.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/applicationContext.xml",
        "classpath:spring/applicationContext-data.xml",
})
public class GraphServiceImplTest {

    @Autowired
    private GraphService graphService;
    @Test
    public void testGenerateAml(){
        Graph graph = graphService.generateAmlGraph("阳山中邦华翔汽车城投资有限责任公司");
        System.out.println(graph);

    }
}
