package com.haizhi.iap.search.repo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.search.conf.ESEnterpriseConstants;
import com.haizhi.iap.search.conf.ESEnterpriseRangeConf;
import com.haizhi.iap.search.conf.ESEnterpriseSearchConf;
import com.haizhi.iap.search.conf.ESRangeConf;
import com.haizhi.iap.search.controller.model.ESEnterpriseReq;
import com.haizhi.iap.search.enums.ESEnterpriseSearchType;
import com.haizhi.iap.search.enums.ESSearchMethod;
import com.haizhi.iap.search.model.GraphCompany;
import com.haizhi.iap.search.utils.DateUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.InternalDateRange;
import org.elasticsearch.search.aggregations.bucket.range.InternalRange;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
import org.elasticsearch.search.rescore.RescorerBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

/**
 * Created by chenbo on 16/12/26.
 */
@Slf4j
@Repository
public class ESEnterpriseSearchRepo {
    @Setter
    @Value("${index.enterprise_data_gov}")
    String indexOfEnterpriseDataGov;

    @Setter
    @Value("${type.enterprise_data_gov}")
    String typeOfEnterpriseDataGov;

    @Setter
    @Autowired
    Client client;

    @Setter
    @Autowired
    JdbcTemplate template;

    private static Integer minimumNumberShouldMatch = 1;
    //    private static final String DEFAULT_ANALYZER = "ik_cn_index";
    private static final Integer DEFAULT_SLOP = 20;
    private static final String TABLE_FOLLOW_ITEM = "follow_item";


    public List<String> suggest(String keyWord, String searchType,
                                Integer windowSize, List<String> fieldsFetch,
                                Integer count) throws IOException {
        QueryBuilder queryBuilder = finalQuery(keyWord, searchType, null, null, null, null, null, null, null);
        ESQueryer queryer = ESQueryer.builder()
                .client(client)
                .index(indexOfEnterpriseDataGov)
                .type(typeOfEnterpriseDataGov)
                .size(count)
                .queryBuilder(queryBuilder)
                .rescorer(rescorer())
                .windowSize(windowSize)
                .build();
        SearchResponse response = queryer.actionGet();

        log.debug("suggest query[{}] took {} ms.", queryBuilder, response.getTook().getMillis());
        List<String> result = new ArrayList<>();
        if (response.getHits() != null) {
            for (SearchHit hit : response.getHits().getHits()) {
                if (hit.getId() == null) {
                    continue;
                }
                result.add(hit.getId());
            }
        }
        return result;
    }

    public Map<String, Object> search(ESEnterpriseReq req, Integer windowSize) throws IOException {
        ESQueryer queryer = ESQueryer.builder()
                .client(client)
                .index(indexOfEnterpriseDataGov)
                .type(typeOfEnterpriseDataGov)
                .from(req.getFrom())
                .size(req.getSize())
                .queryBuilder(
                        finalQuery(req.getKeyWord(), req.getSearchType(), req.getProvince(), req.getCity(),
                                req.getIndustry(), req.getRegisterFoundMin(),
                                req.getRegisterFoundMax(),
                                req.getRegisterDateStart() == null ? null : new Date(req.getRegisterDateStart()),
                                req.getRegisterDateEnd() == null ? null : new Date(req.getRegisterDateEnd())))
                .aggregationBuilders(aggregations()) //聚合
                .rescorer(rescorer())
                .windowSize(windowSize)
                .build();
        SearchResponse response = queryer.actionGet();
        Map<String, Object> result = Maps.newHashMap();
        result.put("total_count", response.getHits().getTotalHits());
        result.put("took_ms", response.getTook().getMillis());
        result.put("result", finalResult(response, req.getSearchType()));
        result.put("aggregations", wrapBuckets(response));

        log.debug("search query[{}] took {} ms.", req.getKeyWord(), response.getTook().getMillis());
        return result;

    }

    public List<GraphCompany> graphSuggest(String keyWord, String searchType,
                                           Integer windowSize,
                                           Integer count) throws IOException {
        ESQueryer queryer = ESQueryer.builder()
                .client(client)
                .index(indexOfEnterpriseDataGov)
                .type(typeOfEnterpriseDataGov)
                .size(count)
                .queryBuilder(finalQuery(keyWord, searchType, null, null, null, null, null, null, null))
                .rescorer(rescorer("1.0/3", "1.0/2"))
                .windowSize(windowSize)
                .build();
        SearchResponse response = queryer.actionGet();

        log.debug("graph suggest query[{}] took {} ms.", keyWord, response.getTook().getMillis());
        List<GraphCompany> result = Lists.newArrayList();
        if (response.getHits() != null) {
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                GraphCompany company = new GraphCompany();
                company.setName(String.valueOf(source.get("name")));
                if (hit.getHighlightFields() != null
                        && hit.getHighlightFields().get("name") != null
                        && hit.getHighlightFields().get("name") instanceof HighlightField) {
                    company.setHighlight(String.valueOf(hit.getHighlightFields().get("name").getFragments()[0].string()));
                }
                company.setLegalMan(String.valueOf(source.get("legal_man")));
                company.setBusinessStatus(String.valueOf(source.get("business_status")));
                result.add(company);
            }
        }
        return result;
    }

    private Map<String, Object> wrapBuckets(SearchResponse response) {
        Map<String, Object> wrapBuckets = new HashMap<>();
        Map<String, Aggregation> aggMap = response.getAggregations().getAsMap();
        for (String key : aggMap.keySet()) {
            List buckets = new ArrayList<>();
            if (aggMap.get(key) instanceof StringTerms) {
                for (Terms.Bucket bucket : ((StringTerms) aggMap.get(key)).getBuckets()) {
                    Map<String, Object> buc = new HashMap<>();
                    if (bucket.getKey() != null) {
                        buc.put("key", bucket.getKey());
                    }
                    buc.put("doc_count", bucket.getDocCount());
                    buckets.add(buc);
                }
            } else if (aggMap.get(key) instanceof InternalRange) {
                for (Object range : ((InternalRange) aggMap.get(key)).getBuckets()) {
                    Map<String, Object> buc = new HashMap<>();
                    if (((InternalRange.Bucket) range).getKey() != null) {
                        buc.put("key", ((InternalRange.Bucket) range).getKey());
                    }
                    buc.put("doc_count", ((InternalRange.Bucket) range).getDocCount());


                    if (((InternalRange.Bucket) range).getFrom() != null) {
                        if (aggMap.get(key) instanceof InternalDateRange) {
                            buc.put("from", ((DateTime) ((InternalDateRange.Bucket) range).getFrom()).getMillis());
                        } else {
                            buc.put("from", ((InternalRange.Bucket) range).getFrom());
                        }
                    }

                    if (((InternalRange.Bucket) range).getTo() != null) {
                        if (aggMap.get(key) instanceof InternalDateRange) {
                            buc.put("to", ((DateTime) ((InternalDateRange.Bucket) range).getTo()).getMillis());
                        } else {
                            buc.put("to", ((InternalRange.Bucket) range).getTo());
                        }
                    }

                    buckets.add(buc);
                }
            }
            wrapBuckets.put(key, Collections.singletonMap("buckets", buckets));
        }
        return wrapBuckets;
    }

    private List<Map<String, Object>> finalResult(SearchResponse response, String searchType) {
        SearchHit[] hits = response.getHits().getHits();
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : hits) {

            Map<String, Object> resultItem = hit.getSourceAsMap();
            resultItem.put("logo", "None");
            resultItem.put("score", "None");
            resultItem.put("_id", resultItem.get("name"));
            resultItem.put("_followed_", checkCompanyFollowed(String.valueOf(resultItem.get("name"))));
            resultItem.put("_score", hit.getScore());

            resultItem.remove("_src");

            Map<String, List<String>> highlight = new HashMap<>();
            for (String key : hit.getHighlightFields().keySet()) {
                if (key.equals("used_name_list.standard")) {
                    continue;
                }
                HighlightField highlightField = hit.getHighlightFields().get(key);
                List<String> highlights = new ArrayList<>();
                for (Text text : highlightField.getFragments()) {
                    highlights.add(text.toString());
                }
                highlight.put(key, highlights);
            }
            resultItem.put("_highlight", highlight);

            results.add(resultItem);
        }
        return results;
    }

    public boolean checkCompanyFollowed(String companyName) {
        Long userId = DefaultSecurityContext.getUserId();
        if (userId == null) {
            return false;
        } else {
            try {
                String sql = "select count(1) from " + TABLE_FOLLOW_ITEM +
                        " where user_id = ? and company_name = ? and is_follow = true";
                Long count = template.queryForObject(sql, Long.class, userId, companyName);
                return count != null && !count.equals(0l);
            } catch (DataAccessException ex) {
                log.error(ex.getMessage());
            }
        }
        return false;
    }

    public QueryBuilder finalQuery(String keyWord, String searchType,
                                   String filterProvince, String filterCity, String filterIndustry,
                                   Double registerFoundMin, Double registerFoundMax,
                                   Date registerDateStart,
                                   Date registerDateEnd) {
        BoolQueryBuilder finalQuery = QueryBuilders.boolQuery();
        QueryBuilder scoreQuery = scoredQuery(keyWord, searchType);
        QueryBuilder filterQuery = filter(filterProvince, filterCity, filterIndustry,
                registerFoundMin, registerFoundMax,
                registerDateStart, registerDateEnd);
        if (scoreQuery != null) {
            finalQuery.must(scoreQuery);
        }
        if (filterQuery != null) {
            finalQuery.filter(filterQuery);
        }
        return finalQuery;
    }

    public QueryBuilder filter(String filterProvince, String filterCity, String filterIndustry,
                               Double registerFoundMin, Double registerFoundMax,
                               Date registerDateStart,
                               Date registerDateEnd) {
        if (Strings.isNullOrEmpty(filterProvince) && Strings.isNullOrEmpty(filterCity) && Strings.isNullOrEmpty(filterIndustry)
                && registerFoundMin == null && registerFoundMax == null
                && registerDateStart == null && registerDateEnd == null) {
            return null;
        }
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        //省份筛选
        if (!Strings.isNullOrEmpty(filterProvince)) {
            filter.must(
                    QueryBuilders.boolQuery()
                            .minimumShouldMatch(minimumNumberShouldMatch)
                            .should(QueryBuilders.termQuery(ESEnterpriseConstants.PROVINCE_RAW, filterProvince))
                            .should(QueryBuilders.termQuery(ESEnterpriseConstants.PROVINCE_RAW, filterProvince + "省"))
                            .should(QueryBuilders.termQuery(ESEnterpriseConstants.PROVINCE_RAW,
                                    filterProvince.contains("省") ? filterProvince.substring(0, filterProvince.lastIndexOf("省")) : filterProvince)));
        }
        //城市筛选
        if (!Strings.isNullOrEmpty(filterProvince) && !Strings.isNullOrEmpty(filterCity)) {
            List<String> properties = Lists.newArrayList();

            properties.add(filterCity);
            if (filterCity.contains("市")) {
                properties.add(filterCity.substring(0, filterCity.lastIndexOf("市")));
                if (filterProvince.contains("省")) {
                    properties.add(filterProvince + filterCity);
                    properties.add(filterProvince.substring(0, filterProvince.lastIndexOf("省"))
                            + filterCity.substring(0, filterCity.lastIndexOf("市")));
                } else {
                    properties.add(filterProvince +
                            filterCity.substring(0, filterCity.lastIndexOf("市")));
                }
            } else {
                properties.add(filterCity + "市");
                if (filterProvince.contains("省")) {
                    properties.add(filterProvince + filterCity + "市");
                    properties.add(filterProvince.substring(0, filterProvince.lastIndexOf("省"))
                            + filterCity);
                } else {
                    properties.add(filterProvince + filterCity);
                }
            }

            BoolQueryBuilder shouldMatch = QueryBuilders.boolQuery().minimumShouldMatch(minimumNumberShouldMatch);

            for (String city : properties) {
                shouldMatch.should(QueryBuilders.termQuery(ESEnterpriseConstants.CITY_RAW, city));
            }
            filter.must(shouldMatch);
        }
        //行业筛选
        if (!Strings.isNullOrEmpty(filterIndustry)) {
            filter.must(QueryBuilders.termQuery(ESEnterpriseConstants.INDUSTRY_RAW, filterIndustry));
        }
        //注册资本筛选
        if (!(registerFoundMax == null && registerFoundMin == null)) {
            RangeQueryBuilder foundRange = QueryBuilders.rangeQuery(ESEnterpriseConstants.REGISTER_FOUND_COL);
            if (registerFoundMax != null) {
                foundRange.lt(registerFoundMax);
            }
            if (registerFoundMin != null) {
                foundRange.gte(registerFoundMax);
            }
            filter.must(foundRange);
        }
        //注册日期筛选
        if (!(registerDateStart == null && registerDateEnd == null)) {
            RangeQueryBuilder dateRange = QueryBuilders.rangeQuery(ESEnterpriseConstants.REGISTER_DATE_COL);
            if (registerDateStart != null) {
                dateRange.gte(DateUtils.FORMAT_T.format(registerDateStart));
            }
            if (registerDateEnd != null) {
                dateRange.lt(DateUtils.FORMAT_T.format(registerDateEnd));
            }
            filter.must(dateRange);
        }

        return filter;
    }

    //统计聚合
    public List<AggregationBuilder> aggregations() {
        AggregationBuilder provinceAgg = AggregationBuilders
                .terms(ESEnterpriseConstants.PROVINCE)
                .field(ESEnterpriseConstants.PROVINCE_RAW)
                .size(50);

        AggregationBuilder industryAgg = AggregationBuilders
                .terms(ESEnterpriseConstants.INDUSTRY)
                .field(ESEnterpriseConstants.INDUSTRY_RAW)
                .size(50);

        DateRangeAggregationBuilder dateAgg = AggregationBuilders.dateRange(ESEnterpriseConstants.REGISTER_DATE)
                .field(ESEnterpriseConstants.REGISTER_DATE_COL);
        Map<String, ESEnterpriseRangeConf> dateRangeConf = ESEnterpriseRangeConf.getDateConfigs();
        for (String key : dateRangeConf.keySet()) {
            ESEnterpriseRangeConf conf = dateRangeConf.get(key);
            if (conf.getTo() == null) {
                dateAgg.addUnboundedFrom(conf.getKey(), conf.getFrom().toString());
            } else if (conf.getFrom() == null) {
                dateAgg.addUnboundedTo(conf.getKey(), conf.getTo().toString());
            } else if (conf.getFrom() != null && conf.getTo() != null) {
                dateAgg.addRange(conf.getKey(), conf.getFrom().toString(), conf.getTo().toString());
            }
            //from 和 to 都是null的情况不处理
        }

        RangeAggregationBuilder foundAgg = AggregationBuilders.range(ESEnterpriseConstants.REGISTER_FOUND)
                .field(ESEnterpriseConstants.REGISTER_FOUND_COL);
        Map<String, ESEnterpriseRangeConf> foundRangeConf = ESEnterpriseRangeConf.getFoundConfigs();
        for (String key : foundRangeConf.keySet()) {
            ESRangeConf conf = foundRangeConf.get(key);
            if (conf.getTo() == null) {
                foundAgg.addUnboundedFrom(conf.getKey(), Double.valueOf(String.valueOf(conf.getFrom())));
            } else if (conf.getFrom() == null) {
                foundAgg.addUnboundedTo(conf.getKey(), Double.valueOf(String.valueOf(conf.getTo())));
            } else if (conf.getFrom() != null && conf.getTo() != null) {
                foundAgg.addRange(conf.getKey(), Double.valueOf(String.valueOf(conf.getFrom())), Double.valueOf(String.valueOf(conf.getTo())));
            }
            //from 和 to 都是null的情况不处理
        }
        List<AggregationBuilder> aggs = new ArrayList<>();
        aggs.add(provinceAgg);
        aggs.add(industryAgg);
        aggs.add(dateAgg);
        aggs.add(foundAgg);

        return aggs;
    }

    /**
     * 评分策略
     */
    public QueryBuilder scoredQuery(String keyWord, String searchType) {
        if (Strings.isNullOrEmpty(keyWord)) {
            return null;
        }

        DisMaxQueryBuilder disMaxQuery = QueryBuilders.disMaxQuery();
        List<QueryBuilder> subQueries = subQueries(keyWord, searchType);

        for (QueryBuilder subQuery : subQueries) {
            disMaxQuery.add(subQuery);
        }

        return QueryBuilders.functionScoreQuery(disMaxQuery,
                ScoreFunctionBuilders.scriptFunction(
                        new Script(ScriptType.INLINE,
                                "expression",
                                getInlineScoreScript(),
                                Collections.emptyMap())))
                .boostMode(CombineFunction.MULTIPLY)
                .scoreMode(FunctionScoreQuery.ScoreMode.MULTIPLY);
    }

    /**
     * 重评分
     */
    public RescorerBuilder rescorer() {
        return rescorer(null, null);
    }

    /**
     * 重打分
     * @param kCapital
     * @param kCompanyAge
     * @return
     */
    public RescorerBuilder rescorer(String kCapital, String kCompanyAge) {
        FunctionScoreQueryBuilder groovy = QueryBuilders.functionScoreQuery(
                ScoreFunctionBuilders.scriptFunction(
                        new Script(ScriptType.INLINE,
                                "groovy",
                                getInlineRescoreScript(kCapital, kCompanyAge),
                                Collections.emptyMap())
                ))
                .boostMode(CombineFunction.MULTIPLY)
                .scoreMode(FunctionScoreQuery.ScoreMode.MULTIPLY);
        QueryRescorerBuilder queryRescorerBuilder = new QueryRescorerBuilder(groovy);
        queryRescorerBuilder.setScoreMode(QueryRescoreMode.Multiply);
        return queryRescorerBuilder;
    }

    /**
     *
     * @param keyWord
     * @param searchType
     * @return
     */
    public List<QueryBuilder> subQueries(String keyWord, String searchType) {
        Map<ESEnterpriseSearchType, List<ESEnterpriseSearchConf>> configs = ESEnterpriseSearchConf.getConfigs();
        List<QueryBuilder> subQueries = new ArrayList<>();
        ESEnterpriseSearchType type = ESEnterpriseSearchType.valueOf(searchType.toUpperCase());

        switch (type) {
            case _ALL:
                for (List<ESEnterpriseSearchConf> confs : configs.values()) {
                    for (ESEnterpriseSearchConf conf : confs) {
                        subQueries.add(subQuery(keyWord, conf.getCol(), conf.getSearchMethod(), conf.getWeight()));
                    }
                }
                break;
            default:
                List<ESEnterpriseSearchConf> confs = configs.get(type);
                for (ESEnterpriseSearchConf conf : confs) {
                    subQueries.add(subQuery(keyWord, conf.getCol(), conf.getSearchMethod(), conf.getWeight()));
                }
        }
        return subQueries;
    }

    public QueryBuilder subQuery(String keyWord, String col, ESSearchMethod searchMethod, Integer weight) {

        switch (searchMethod) {
            case MATCH_PHRASE:
                return QueryBuilders.matchQuery(col,keyWord).boost(weight);
                        //QueryBuilders.matchQuery(col, keyWord)
                        //.type(MatchQuery.Type.PHRASE).slop(DEFAULT_SLOP).boost(weight);
            case TERM:
                return QueryBuilders.termQuery(col, keyWord).boost(weight);
            default:
                return null;
        }
    }


    public String getInlineScoreScript() {
        return "ln(2 + doc['val_registered_capital'].value)";
    }

    public String getInlineRescoreScript(String kCapital, String kCompanyAge) {
        if (Strings.isNullOrEmpty(kCapital)) {
            kCapital = "1.0/4";
        }
        if (Strings.isNullOrEmpty(kCompanyAge)) {
            kCompanyAge = "1.0/3";
        }
        return "    def k_capital = " + kCapital + ";" +
                "    def k_company_age = " + kCompanyAge + ";" +
                "    def val_registered_date_ms = doc.val_registered_date.date.millis;\n" +
                "    def capital = doc['val_registered_capital'].value;\n" +
                "\n" +
                "    /* capital的得分 */\n" +
                "    if (capital < 0) {\n" +
                "        capital = 0;\n" +
                "    }\n" +
                "    def score_capital = (2 + capital/1000);\n" +
                "\n" +
                "    /* company_age的得分 */\n" +
                "    def company_age_ms = DateTime.now().getMillis() - val_registered_date_ms;\n" +
                "    def valid_date_threshold = -662688000000;  /* 1949-01-01的时间值(毫秒) */\n" +
                "    if (!(doc.val_registered_date) || (val_registered_date_ms < valid_date_threshold) || (company_age_ms < 0)) {\n" +
                "        /* 时间为null、时间早于1949或晚于当前时间则认为不合法, 把它当作是新成立的公司 */\n" +
                "        company_age_ms = 0;\n" +
                "    }\n" +
                "    def score_company_age = (2 + company_age_ms/1000);\n" +
                "\n" +
                "    return (score_capital ** k_capital) * (score_company_age ** k_company_age);";
    }

    public Map<String, Object> queryById(String id) {
        try {
            GetResponse response = client.prepareGet(indexOfEnterpriseDataGov, typeOfEnterpriseDataGov, id).execute().get();
            if (response.isExists()) {
                return response.getSourceAsMap();
            }
        } catch (Exception e) {
            log.error("{}", e);
        }
        return null;
    }

    public List<Map<String, Object>> search(Integer offset, Integer count) throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        ESQueryer queryer = ESQueryer.builder()
                .client(client)
                .index(indexOfEnterpriseDataGov)
                .type(typeOfEnterpriseDataGov)
                .from(offset)
                .size(count)
                .queryBuilder(queryBuilder)
                .build();
        SearchResponse response = queryer.actionGet();


        log.debug("suggest query[{}] took {} ms.", queryBuilder, response.getTook().getMillis());
        List<Map<String, Object>> result = Lists.newArrayList();
        if (response.getHits() != null) {
            for (SearchHit hit : response.getHits().getHits()) {
                result.add(hit.getSourceAsMap());
            }
        }
        return result;
    }

    public void index(String index, String type, String id, String source) {
        IndexResponse response = client.prepareIndex(index, type, id).setSource(source).get();
        if (response != null && response.status().equals(RestStatus.CREATED)) {
            log.info("{} 创建成功!", id);
        }
    }
}
