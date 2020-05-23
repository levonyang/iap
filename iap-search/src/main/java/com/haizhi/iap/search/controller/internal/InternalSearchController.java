package com.haizhi.iap.search.controller.internal;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.bean.CustdigParam;
import com.haizhi.iap.search.controller.model.Basic;
import com.haizhi.iap.search.controller.model.EnterpriseReq;
import com.haizhi.iap.search.enums.EnterpriseSearchType;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.service.CustdigSearchService;
import com.haizhi.iap.search.service.EnterpriseSearchService;
import io.swagger.annotations.Api;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenbo on 2017/9/28.
 */
@Api(tags="【搜索-内部检索企业信息模块】其它模块请求搜索企业信息")
@RestController
@RequestMapping("/internal")
public class InternalSearchController {

    @Setter
    @Autowired
    private EnterpriseSearchService enterpriseSearchService;

    @Autowired
    private CustdigSearchService custdigSearchService;

    private static Integer DEFAULT_PAGE_SIZE = 10;

    private static List<String> enterpriseSearchTypes = Lists.newArrayList();

    @PostConstruct
    private void init() {
        for (EnterpriseSearchType type : EnterpriseSearchType.values()) {
            enterpriseSearchTypes.add(type.getName());
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper search(@RequestParam("name") String name,
                          @RequestParam(value = "stock_code", required = false) String stockCode,
//                   @RequestParam("stock_type") String stockType,
                          @RequestParam("type") String type,
                          @RequestParam(value = "sub_type", required = false) String subType,
                          @RequestParam(value = "third_type", required = false) String thirdType,
                          @RequestParam(value = "year_quarter", required = false) String yearQuarter,
                          @RequestParam(value = "only_count", required = false) Integer onlyCount,
                          @RequestParam(value = "offset", required = false) Integer offset,
                          @RequestParam(value = "count", required = false) Integer count) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        } else if (!enterpriseSearchTypes.contains(type)) {
            return SearchException.WRONG_TYPE.get();
        }

        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_NAME.get();
        }

        if (!Strings.isNullOrEmpty(subType) && subType.equals("news")
                && Strings.isNullOrEmpty(thirdType)) {
            return SearchException.MISS_THIRD_TYPE.get();
        }

        if (onlyCount == null) {
            onlyCount = 1;
        }

        if (offset == null) {
            offset = 0;
        }
        if (count == null) {
            count = DEFAULT_PAGE_SIZE;
        }
        Boolean onlyCounting = onlyCount.equals(1);

        EnterpriseReq req = new EnterpriseReq(name, stockCode, type, subType, thirdType, yearQuarter, onlyCounting, offset, count);

        Basic basicInfo = enterpriseSearchService.basic(req);

        Object data = null;
        if (type.equals(EnterpriseSearchType.ALL.getName())) {
            req.setSubType(null);
            Map<String, Object> all = Maps.newHashMap();
            all.put(EnterpriseSearchType.BASIC.getName(), basicInfo);
            all.put(EnterpriseSearchType.ANNUAL_REPORT.getName(), enterpriseSearchService.annualReport(req));
            all.put(EnterpriseSearchType.LIST.getName(), enterpriseSearchService.listing(req));
            all.put(EnterpriseSearchType.INVESTMENT.getName(), enterpriseSearchService.invest(req));
            all.put(EnterpriseSearchType.INTELLECTUAL_PROPERTY.getName(), enterpriseSearchService.intellectualProperty(req));
            all.put(EnterpriseSearchType.BIDDING.getName(), enterpriseSearchService.bidding(req));
            all.put(EnterpriseSearchType.RISK.getName(), enterpriseSearchService.risk(req));
            all.put(EnterpriseSearchType.PUBLIC_SENTIMENT.getName(), enterpriseSearchService.publicSentiment(req));
            all.put(EnterpriseSearchType.INVESTMENT_INSTITUTION.getName(), enterpriseSearchService.investInstitution(req));
            data = all;

        } else if (type.equals(EnterpriseSearchType.BASIC.getName())) {
            data = basicInfo;
        } else if (type.equals(EnterpriseSearchType.ANNUAL_REPORT.getName())) {
            data = enterpriseSearchService.annualReport(req);
        } else if (type.equals(EnterpriseSearchType.LIST.getName())) {
            data = enterpriseSearchService.listing(req);
        } else if (type.equals(EnterpriseSearchType.INVESTMENT.getName())) {
            data = enterpriseSearchService.invest(req);
        } else if (type.equals(EnterpriseSearchType.INTELLECTUAL_PROPERTY.getName())) {
            data = enterpriseSearchService.intellectualProperty(req);
        } else if (type.equals(EnterpriseSearchType.BIDDING.getName())) {
            data = enterpriseSearchService.bidding(req);
        } else if (type.equals(EnterpriseSearchType.RISK.getName())) {
            data = enterpriseSearchService.risk(req);
        } else if (type.equals(EnterpriseSearchType.PUBLIC_SENTIMENT.getName())) {
            data = enterpriseSearchService.publicSentiment(req);
        } else if (type.equals(EnterpriseSearchType.INVESTMENT_INSTITUTION.getName())) {
            data = enterpriseSearchService.investInstitution(req);
        }
        return Wrapper.OKBuilder.data(data).build();
    }

    /**
     * 获取多个客户关系挖掘信息
     * @param param
     * @return
     */
    @NoneAuthorization
    @RequestMapping(value = "/custdig",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON)
    public Wrapper custdig(@RequestBody CustdigParam param){
        Object data = custdigSearchService.searchShortPath(param);
        return Wrapper.ok(data);
    }

    /**
     * 通过企业名称查询企业id集合
     * @param companys
     * @return
     */
    @NoneAuthorization
    @RequestMapping(value = "/findCustByname",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON)
    public Wrapper findCustByname(@RequestBody List<String> companys){
        Set<String> companyids = custdigSearchService.findCustByname(companys);
        return Wrapper.ok(companyids);
    }
}
