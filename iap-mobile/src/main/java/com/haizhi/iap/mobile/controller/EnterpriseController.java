package com.haizhi.iap.mobile.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.mobile.bean.param.BriefParam;
import com.haizhi.iap.mobile.bean.param.SearchParam;
import com.haizhi.iap.mobile.bean.result.Graph2;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.service.EnterpriseService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/4/11.
 *
 * 公司相关的信息
 */
@Api(tags="【移动-企业信息模块】获取企业股东、高管信息")
@RestController
@Slf4j
@RequestMapping("/enterprise")
public class EnterpriseController
{
    @Autowired
    private EnterpriseService enterpriseService;

    @RequestMapping(method = RequestMethod.POST, value = "/brief")
    public Wrapper brief(@RequestBody BriefParam briefParam)
    {
        try {
            List<Map<String, Object>> basicInfos = enterpriseService.brief(briefParam.getUsername(), briefParam.getCompanys());
            return Wrapper.ok(basicInfos);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 股东信息
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/shareholder")
    public Wrapper shareholder(@RequestBody SearchParam searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getShareHolder(searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize());
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 高管信息
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/officer")
    public Wrapper officer(@RequestBody SearchParam searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getOfficer(searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize());
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight()-1, pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }
}
