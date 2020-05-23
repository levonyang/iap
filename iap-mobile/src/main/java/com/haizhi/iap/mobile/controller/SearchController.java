package com.haizhi.iap.mobile.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.mobile.bean.param.EnterpriseSearchParam;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.service.EnterpriseEsSearchService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by thomas on 18/4/11.
 */
@Api(tags="【移动-查询企业信息模块】查询企业信息")
@RestController
@Slf4j
@RequestMapping("/")
public class SearchController
{
    @Autowired
    private EnterpriseEsSearchService enterpriseEsSearchService;

    @NoneAuthorization
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public Wrapper searchCompany(@RequestBody EnterpriseSearchParam enterpriseSearchParam)
    {
        try {
            HasMoreResult result = enterpriseEsSearchService.search(enterpriseSearchParam);
            return Wrapper.ok(result);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }
}
