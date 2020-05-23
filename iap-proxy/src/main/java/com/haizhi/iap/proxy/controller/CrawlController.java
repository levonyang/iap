package com.haizhi.iap.proxy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.config.ApplicationConfigurer;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.proxy.exception.ProxyException;
import com.haizhi.iap.proxy.repo.EnterpriseRepo;
import com.haizhi.iap.proxy.repo.UserRepo;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenbo on 17/2/17.
 */

@Api(tags="【代理-爬取模块】爬取企业相关信息")
@Slf4j
@RestController
@RequestMapping(value = "/proxy/crawl")
public class CrawlController {

    @Setter
    @Autowired
    UserRepo userRepo;

//    @Setter
//    @Autowired
//    ClientConnectionPool pool;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    //@Setter
    //@Autowired
    //DeepSearchWS deepSearchWS;

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Setter
    @Autowired
    ApplicationConfigurer applicationConfigurer;

    private static Pattern companyNamePatter = Pattern.compile("^((.*)[^、,](公司|中心|厂|居委会|委员会|所|院|厅|局|处|幼儿园" +
            "|服务部|行|社|政府|店|部|小学|校|酒吧|场|发廊|城|馆|零售|家|站|档|摊))$");
    private static Pattern specialPattern = Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*——+|{}；。，、？]");


    @RequestMapping(value = "/get_schedule_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Map list(@RequestParam(value = "offset", required = false) Integer offset,
                    @RequestParam(value = "count", required = false) Integer count,
                    @RequestParam(value = "schedule_type") Integer scheduleType) {
        Long userId = DefaultSecurityContext.getUserId();

        if (offset == null) {
            offset = 0;
        }
        if (count == null) {
            count = 10;
        }

        if (scheduleType == null) {
            scheduleType = 0;
        }

//        //采用httpClient调用的方法可以实现
//        try {
//            URIBuilder builder;
//
//            builder = new URIBuilder(pool.getCrawlIP() + "/get_schedule_list");
//
//            builder.addParameter("user", user.getUsername());
//
//            builder.addParameter("start", offset.toString());
//            builder.addParameter("limit", count.toString());
//
//            HttpGet get = new HttpGet(builder.build());
//
//            String responseStr = pool.execute(get);
//            return objectMapper.readValue(responseStr, Map.class);
//        } catch (Exception e) {
//            log.error("{}", e);
//            throw new ServiceAccessException(-1, e.getMessage());
//        }
        String finalUsername = applicationConfigurer.getEnv() + "_" + userId;
//        try {
//            Map map = deepSearchWS.listSchedule(finalUsername, offset.toString(), count.toString(), scheduleType.toString());
//            return map;
//        } catch (Exception e) {
//            log.error("{}", e);
//            throw new ServiceAccessException(-1, e.getMessage());
//        }
        return Maps.newHashMap();
    }

    @RequestMapping(value = "/new_detail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Map getNewDetail(@RequestBody Map<String, Object> map) {

        Long userId = DefaultSecurityContext.getUserId();
        String finalUsername = applicationConfigurer.getEnv() + "_" + userId;

        if (map.get("data") == null) {
            throw new ServiceAccessException(ProxyException.MISS_DATA.get());
        } else if (map.get("data") instanceof List) {
            for (Map<String, Object> company : (List<Map>) map.get("data")) {
                if (company.get("company") != null) {
                    String companyName = company.get("company").toString();
                    if (containsSpecialChr(companyName)) {
                        throw new ServiceAccessException(ProxyException.CONTAINS_SPECIAL_CHAR);
                    }

//                    if (enterpriseRepo.getBasic(companyName) == null) {
//                        //企业不在库, 属于待抓取, 需要判断是否上报过
//                        if (!isCompanyName(companyName)) {
//                            throw new ServiceAccessException(ProxyException.ILLEGAL_COMPANY_NAME);
//                        }else {
//                            Map response = deepSearchWS.isPosted(companyName, finalUsername);
//                            if (response.get("query_result") != null && Boolean.parseBoolean(response.get("query_result").toString())) {
//                                throw new ServiceAccessException(ProxyException.RESUBMIT);
//                            }
//                        }
//                    }
                }
            }
        }

        try {
            //企业在库,属于上报更新,直接上报
            //map.put("user", finalUsername);
            //return deepSearchWS.getNewDetail(map);
            return Maps.newHashMap();
        } catch (Exception e) {
            log.error("{}", e);
            throw new ServiceAccessException(-1, e.getMessage());
        }

    }

    @NoneAuthorization
    @RequestMapping(value = "/query_status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Map queryStatus(@RequestParam("company") String company) {
//        try {
//            return deepSearchWS.queryStatus(company);
//        } catch (Exception e) {
//            log.error("{}", e);
//            throw new ServiceAccessException(-1, e.getMessage());
//        }
    return Maps.newHashMap();
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Map getNewDetail(@RequestParam(value = "company") String company) {
        if (Strings.isNullOrEmpty(company)) {
            throw new ServiceAccessException(ProxyException.NO_COMPANY_PROVIDED);
        }
        Long userId = DefaultSecurityContext.getUserId();
        String finalUsername = applicationConfigurer.getEnv() + "_" + userId;

//        try {
//            return deepSearchWS.cancelCrawl(company, finalUsername);
//        } catch (Exception e) {
//            log.error("{}", e);
//            throw new ServiceAccessException(-1, e.getMessage());
//        }
        return Maps.newHashMap();
    }

    @RequestMapping(value = "/level_count", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Map<String, Object> getLevelAndCount() {
//        try {
//            URIBuilder builder;
//
//            builder = new URIBuilder(pool.getCrawlIP() + "/query_schedule_level_count");
//
//            HttpGet get = new HttpGet(builder.build());
//
//            String responseStr = pool.execute(get);
//            return objectMapper.readValue(responseStr, Map.class);
//        } catch (Exception e) {
//            log.error("{}", e);
//            throw new ServiceAccessException(-1, e.getMessage());
//        }

//        try {
//            return deepSearchWS.levelAndCount();
//        } catch (Exception e) {
//            log.error("{}", e);
//            throw new ServiceAccessException(-1, e.getMessage());
//        }
        return Maps.newHashMap();
    }

    protected static boolean isCompanyName(String company) {
        Matcher matcher = companyNamePatter.matcher(company);
        return matcher.matches();
    }

    protected static boolean containsSpecialChr(String company) {
        Matcher matcher = specialPattern.matcher(company);
        return matcher.find();
    }
}
