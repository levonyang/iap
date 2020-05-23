package com.haizhi.iap.mobile.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.mobile.bean.param.MarketEventSearchParam;
import com.haizhi.iap.mobile.bean.param.MarketEventSettingParam;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.service.MarketEventService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by thomas on 18/4/19.
 */
@Api(tags="【移动-市场信息模块】查询、设置市场信息")
@RestController
@RequestMapping("/market")
@Slf4j
public class MarketEventController
{
    @Autowired
    private MarketEventService marketEventService;

    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public Wrapper marketEvent(@RequestBody MarketEventSearchParam searchParam)
    {
        try {
            HasMoreResult result = marketEventService.marketEvent(searchParam);
            return Wrapper.ok(result);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/setting")
    public Wrapper marketEventSetting(@RequestParam("username") String username)
    {
        try {
            HasMoreResult result = marketEventService.findMarketEventSetting(username);
            return Wrapper.ok(result);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/setting")
    public Wrapper marketEventSetting(@RequestBody MarketEventSettingParam settingParam)
    {
        try {
            marketEventService.updateMarketEventSetting(settingParam);
            return Wrapper.ok(null);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }
}
