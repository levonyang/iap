package com.haizhi.iap.configure.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by chenbo on 17/5/24.
 */
@Api(tags="【数据配置-测试模块】测试服务是否正常")
@RestController
@RequestMapping(value = "/")
public class TestController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @NoneAuthorization
    public Wrapper test() {
        return Wrapper.OKBuilder.data("pong").build();
    }
}
