package com.haizhi.iap.mobile.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.mobile.bean.param.FollowParam;
import com.haizhi.iap.mobile.bean.param.SearchParam;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.exception.ExceptionStatus;
import com.haizhi.iap.mobile.service.CustomerService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by thomas on 18/4/13.
 */
@Api(tags="【移动-客户模块】管理关注的客户")
@RequestMapping("/customer")
@RestController
@Slf4j
public class CustomerController
{
    @Autowired
    private CustomerService customerService;

    /**
     * 获取我的客户
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public Wrapper myCustomers(@RequestBody SearchParam searchParam)
    {
        try {
            HasMoreResult result = customerService.myCustomers(searchParam);
            return Wrapper.ok(result);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 获取该用户关注的企业
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/followees")
    public Wrapper followees(@RequestBody SearchParam searchParam)
    {
        try {
            HasMoreResult followees = customerService.getFollowees(searchParam);
            return Wrapper.ok(followees);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 关注/取消关注 某个企业
     *
     * @param followParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/follow")
    public Wrapper follow(@RequestBody FollowParam followParam)
    {
        int res = 0;
        try {
            res = customerService.follow(followParam);
        } catch (Exception e) {
            log.error("", e);
        }
        if(res == ExceptionStatus.FAIL_TO_MONITOR.getCode())
            return ExceptionStatus.FAIL_TO_MONITOR.get();
        return Wrapper.ok(null);
    }
}
