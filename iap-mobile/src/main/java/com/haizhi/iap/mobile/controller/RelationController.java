package com.haizhi.iap.mobile.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.mobile.bean.param.SearchParam;
import com.haizhi.iap.mobile.bean.param.SearchParamWithDirection;
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

/**
 * Created by thomas on 18/4/11.
 *
 * 关联关系
 */
@Api(tags="【移动-关联关系模块】查询投资、招中标、担保、上下游、一致行动人等关系")
@RestController
@Slf4j
@RequestMapping("/relation")
public class RelationController
{
    @Autowired
    private EnterpriseService enterpriseService;

    /**
     * 对外投资
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/invest")
    public Wrapper invest(@RequestBody SearchParam searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getInvest(searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize());
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 资金往来
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/moneyFlow")
    public Wrapper moneyFlow(@RequestBody SearchParamWithDirection searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getMoneyFlow(searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize(), searchParam.getDirection());
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 担保关系（查arangodb来实现）
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/guarantee")
    public Wrapper guarantee2(@RequestBody SearchParamWithDirection searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getGuarantee(searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize(), searchParam.getDirection());
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 担保关系（调用iap-search的接口来实现）
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/guarantee2")
    public Wrapper guarantee(@RequestBody SearchParamWithDirection searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getGuarantee(searchParam);
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 上下游企业
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upDownStream")
    public Wrapper upDownStream(@RequestBody SearchParamWithDirection searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getUpDownStream(searchParam);
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 一致行动人
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/concert")
    public Wrapper concert(@RequestBody SearchParam searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getConcert(searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize());
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 招中标信息（查arangodb来实现）
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/bid")
    public Wrapper concert(@RequestBody SearchParamWithDirection searchParam)
    {
        try {
            Pair<Graph2, Long> pair = enterpriseService.getBidInfo(searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize(), searchParam.getDirection());
            long cnt = CollectionUtils.isEmpty(pair.getLeft().getVertexes()) ? 0L : (long) pair.getLeft().getVertexes().size();
            return Wrapper.ok(new HasMoreResult<>(pair.getRight(), pair.getRight() > cnt, pair.getLeft()));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 招中标信息
     *
     * @param searchParam
     * @return
     */
/*    @RequestMapping(method = RequestMethod.POST, value = "/bid2")
    public Wrapper concert2(@RequestBody SearchParamWithDirection searchParam)
    {

    }*/
}
