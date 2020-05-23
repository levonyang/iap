package com.haizhi.iap.tag.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Created by chenbo on 16/11/8.
 */
@Api(tags="【标签-测试模块】测试服务是否正常")
@RestController
@RequestMapping(value = "/")
public class TestController {

    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper ping() {
        return Wrapper.OKBuilder.data("pong").build();
    }

}
