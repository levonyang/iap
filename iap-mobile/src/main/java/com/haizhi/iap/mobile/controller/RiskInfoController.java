package com.haizhi.iap.mobile.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.mobile.bean.normal.MongoTermQuery;
import com.haizhi.iap.mobile.bean.param.SearchParam;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.conf.MongoSchemaConstants;
import com.haizhi.iap.mobile.repo.MongoRepo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by thomas on 18/4/16.
 *
 * 风险信息
 */
@Api(tags="【移动-风险信息模块】查询失信、被执行等信息")
@Slf4j
@RestController
@RequestMapping("/risk")
public class RiskInfoController
{
    @Autowired
    private MongoRepo mongoRepo;

    /**
     * 失信信息
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/dishonest")
    public Wrapper dishonest(@RequestBody SearchParam searchParam)
    {
        try {
            MongoTermQuery termQuery = MongoTermQuery.builder().table(MongoSchemaConstants.TABLE_SHIXING_INFO).query(Pair.of(MongoSchemaConstants.FIELD_I_NAME, searchParam.getKeyword()))
                    .offset(searchParam.getOffset()).size(searchParam.getSize()).build();
            List<Map<String, Object>> results = mongoRepo.query(termQuery);
            MongoTermQuery countQuery = MongoTermQuery.builder().table(MongoSchemaConstants.TABLE_SHIXING_INFO).query(Pair.of(MongoSchemaConstants.FIELD_I_NAME, searchParam.getKeyword())).build();
            Long cnt = mongoRepo.count(countQuery);
            return Wrapper.ok(new HasMoreResult<>(cnt, Optional.ofNullable(results).map(List::size).orElse(0) < cnt, results));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    
    
    /**
     * 被执行信息
     *
     * @param searchParam
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/executed")
    public Wrapper executed(@RequestBody SearchParam searchParam)
    {
        try {
            MongoTermQuery termQuery = MongoTermQuery.builder().table(MongoSchemaConstants.TABLE_ZHIXING_INFO).query(Pair.of(MongoSchemaConstants.FIELD_I_NAME, searchParam.getKeyword()))
                    .offset(searchParam.getOffset()).size(searchParam.getSize()).build();
            List<Map<String, Object>> results = mongoRepo.query(termQuery);
            MongoTermQuery countQuery = MongoTermQuery.builder().table(MongoSchemaConstants.TABLE_ZHIXING_INFO).query(Pair.of(MongoSchemaConstants.FIELD_I_NAME, searchParam.getKeyword())).build();
            Long cnt = mongoRepo.count(countQuery);
            return Wrapper.ok(new HasMoreResult<>(cnt, Optional.ofNullable(results).map(List::size).orElse(0) < cnt, results));
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }
}
