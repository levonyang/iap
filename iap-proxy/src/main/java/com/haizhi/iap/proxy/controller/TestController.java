package com.haizhi.iap.proxy.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Created by chenbo on 17/2/17.
 */
@Api(tags="【代理-测试模块】测试服务是否正常")
@RestController
@RequestMapping(value = "/")
public class TestController {
    @RequestMapping(value = "/", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper ping() {
        return Wrapper.OKBuilder.data("pong").build();
    }
}