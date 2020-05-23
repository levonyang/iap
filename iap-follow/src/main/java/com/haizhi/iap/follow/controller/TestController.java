package com.haizhi.iap.follow.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@Api(tags="【关注-测试模块】测试服务器是否正常")
@RestController
@RequestMapping(value = "/")
public class TestController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @NoneAuthorization
    public Wrapper test() {
        return Wrapper.OKBuilder.data("ping").build();
    }
}
