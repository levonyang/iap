package com.haizhi.iap.search.controller;

import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.search.service.EnterpriseSearchService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by chenbo on 16/11/8.
 */
@Api(tags="【搜索-测试模块】测试服务是否正常")
@RestController
@RequestMapping(value = "/")
public class TestController {
    @Autowired
    EnterpriseSearchService enterpriseSearchService;

    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper ping() {
        return Wrapper.OKBuilder.data("pong").build();
    }

    @RequestMapping(value = "/graph_test", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper testGraph() {
        Map<String, Object> result = Maps.newHashMap();
        Pair<String, String> faction = enterpriseSearchService.getFaction("招商银行股份有限公司");
        result.put("faction", faction.getRight());
        result.put("faction_id", faction.getLeft());
        return Wrapper.OKBuilder.data(result).build();
    }

}
