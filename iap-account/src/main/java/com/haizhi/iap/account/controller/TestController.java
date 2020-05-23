package com.haizhi.iap.account.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.exception.ServiceAccessException;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@Api(tags="【账号-测试模块】测试账号后台服务是否正常")
@RestController
@RequestMapping(value = "/")
public class TestController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @NoneAuthorization
    public Wrapper test() {
        return Wrapper.OKBuilder.data("ping").build();
    }

    @RequestMapping("/error")
    @NoneAuthorization
    public Wrapper error() {
        throw new ServiceAccessException(-1, "error msg");
    }
}
