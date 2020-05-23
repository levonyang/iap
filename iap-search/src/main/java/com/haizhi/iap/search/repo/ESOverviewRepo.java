package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.controller.model.AdvancedSearchReq;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model.Range;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenbo on 17/7/4.
 */
@Slf4j
@Repository
public class ESOverviewRepo {
    @Setter
    @Value("${index.enterprise_overview}")
    String indexOfEnterpriseOverview;

    @Setter
    @Value("${type.enterprise_overview}")
    String typeOfEnterpriseOverview;

    @Setter
    @Autowired
    Client client;

    @Setter
    @Autowired
    GraphRepo graphRepo;

    @Setter
    @Autowired
    ESEnterpriseSearchRepo esEnterpriseSearchRepo;

    @Setter
    @Autowired
    ExchangeRateRepo exchangeRateRepo;

    @Setter
    @Resource(name = "supportCurrencyList")
    List<String> supportCurrencyList;

    public DataItem advancedSearch(AdvancedSearchReq req) throws IOException {
        BoolQueryBuilder queryBuilder = buildQuery(req);
        ESQueryer queryer = ESQueryer.builder()
                .client(client)
                .index(indexOfEnterpriseOverview)
                .type(typeOfEnterpriseOverview)
                .from(req.getOffset())
                .size(req.getCount())
                .queryBuilder(queryBuilder)
                .build();
        SearchResponse response = queryer.actionGet();


        log.debug("advanced query.{}.value took {} ms.", queryBuilder, response.getTook().getMillis());
        List<Map<String, Object>> result = Lists.newArrayList();
        long total = 0;
        if (response != null && response.getHits() != null) {
            total = response.getHits().getTotalHits();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> company = esEnterpriseSearchRepo.queryById((String) hit.getSourceAsMap().get("name"));
                if (company != null) {
                    String marketCoefficient = null;
                    String riskCoefficient = null;
                    if (hit.getSourceAsMap().get("market_coefficient") != null
                            && hit.getSourceAsMap().get("market_coefficient") instanceof Map
                            && ((Map) hit.getSourceAsMap().get("market_coefficient")).get("value") != null) {
                        marketCoefficient = (String) ((Map) hit.getSourceAsMap().get("market_coefficient")).get("value");
                    }

                    if (hit.getSourceAsMap().get("risk_coefficient") != null
                            && hit.getSourceAsMap().get("risk_coefficient") instanceof Map
                            && ((Map) hit.getSourceAsMap().get("risk_coefficient")).get("value") != null) {
                        riskCoefficient = (String) ((Map) hit.getSourceAsMap().get("risk_coefficient")).get("value");
                    }
                    company.put("market_coefficient", marketCoefficient);
                    company.put("risk_coefficient", riskCoefficient);
                    company.put("_followed_", esEnterpriseSearchRepo.checkCompanyFollowed(String.valueOf(company.get("name"))));
                    result.add(company);
                }
            }
            return DataItem.builder().data(result).totalCount(total).build();
        }
        return new DataItem();

    }

    private BoolQueryBuilder buildQuery(AdvancedSearchReq req) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(scoredQuery(req.getRiskSort(), req.getMarketSort()));
        if (req.getLocationMap() != null && req.getLocationMap().size() > 0) {
            List<String> provinces = Lists.newArrayList();
            List<String> cities = Lists.newArrayList();
            List<String> district = Lists.newArrayList();
            for (String key : req.getLocationMap().keySet()) {
                if (key.indexOf("省") > 0) {
                    provinces.add(key);
                    provinces.add(key.substring(0, key.indexOf("省")));
                } else {
                    provinces.add(key);
                    provinces.add(key + "省");
                }
                if (req.getLocationMap().get(key) != null) {
                    for (Map<String, List<String>> cityMap : req.getLocationMap().get(key)) {
                    	for (String city : cityMap.keySet()) {
                    		if (key.indexOf("省") > 0) {
                                if (city.indexOf("市") > 0) {
                                    //广东省深圳市
                                    cities.add(key + city);
                                } else {
                                    //广东深圳
                                    cities.add(key.substring(0, key.indexOf("省")) + city);
                                }
                            } else {
                                if (city.indexOf("市") > 0) {
                                    //广东省深圳市
                                    cities.add(key + "省" + city);
                                } else {
                                    //广东深圳
                                    cities.add(key + city);
                                }
                            }
                            //深圳市
                            if (city.indexOf("市") > 0) {
                                cities.add(city);
                                cities.add(city.substring(0, city.indexOf("市")));
                            } else {
                                //深圳
                                cities.add(city);
                                cities.add(city + "市");
                            }
                            //县级
                        	if (cityMap.get(city) != null) {
                        		for(String di:cityMap.get(city)){
                        			district.add(di);
                        		}
                        	}
                    	}

                    }
                }
            }
            queryBuilder.must(QueryBuilders.termsQuery("province", provinces));
            if (cities.size() > 0) {
                queryBuilder.must(QueryBuilders.termsQuery("city", cities));
            }
            if (district.size() > 0) {
                queryBuilder.must(QueryBuilders.termsQuery("district", district));
            }
        }
        //将区间换算成各种汇率下的区间, 对每个区间再wrapPositiveRange
        wrapRegisteredCapital(queryBuilder, req.getRegisteredCapital());

        wrapList(queryBuilder, req.getIndustries(), "industry");
        wrapPositiveRange(queryBuilder, req.getRegisteredTime(), "registered_time");
        wrapListWithWild(queryBuilder, req.getBusinessStatus(), "business_status");
        wrapRange(queryBuilder, req.getInvestmentNum(), "investment_num");
        wrapRange(queryBuilder, req.getShareholdersNum(), "shareholders_num");
        wrapRange(queryBuilder, req.getShareholdersChange(), "shareholders_change");
        wrapRange(queryBuilder, req.getCompanyNameChange(), "company_name_change");
        wrapRange(queryBuilder, req.getLegalManChange(), "legal_man_change");
        wrapRange(queryBuilder, req.getAddressChange(), "address_change");
        wrapRange(queryBuilder, req.getMemberChange(), "member_change");
        wrapRange(queryBuilder, req.getRuleChange(), "rule_change");
        wrapRange(queryBuilder, req.getScopeChange(), "scope_change");
        wrapRange(queryBuilder, req.getPatternNum(), "pattern_num");
        wrapRange(queryBuilder, req.getWinBidNum(), "win_bid_num");
        wrapRange(queryBuilder, req.getActingBidNum(), "acting_bid_num");
        wrapRange(queryBuilder, req.getBidNum(), "bid_num");
        wrapRange(queryBuilder, req.getPlaintiffNum(), "plaintiff_num");
        wrapRange(queryBuilder, req.getDefendantNum(), "defendant_num");
        wrapRange(queryBuilder, req.getExecutedMoney(), "executed_money");
        wrapRange(queryBuilder, req.getExecutedCount(), "executed_count");
        wrapRange(queryBuilder, req.getTaxArrears(), "tax_arrears");
        wrapRange(queryBuilder, req.getTaxArrearsNum(), "tax_arrears_num");
        wrapRange(queryBuilder, req.getA_taxNum(), "a_tax_num");
        wrapRange(queryBuilder, req.getPunishNum(), "punish_num");
        if (req.getIsListed() != null) {
            queryBuilder.must(QueryBuilders.termQuery("is_listed", req.getIsListed()));
        }

        wrapList(queryBuilder, req.getListStatus(), "list_status");
        wrapPositiveRange(queryBuilder, req.getListTime(), "list_time");
        wrapList(queryBuilder, req.getListSector(), "list_sector");
        wrapRange(queryBuilder, req.getBranchNum(), "branch_num");
        if (req.getShareholderListed() != null) {
            queryBuilder.must(QueryBuilders.termQuery("shareholder_listed", req.getShareholderListed()));
        }

        wrapList(queryBuilder, req.getMarketCoefficient(), "market_coefficient.value");
        wrapList(queryBuilder, req.getRiskCoefficient(), "risk_coefficient.value");

        return queryBuilder;
    }

    public Map<String, Object> searchTag(String company) {
        String id = SecretUtil.md5(company);
        try {
            Map<String, Object> result = Maps.newHashMap();
            GetResponse response = client.prepareGet(indexOfEnterpriseOverview,
                    typeOfEnterpriseOverview, id).execute().get();
            if (response.isExists()) {
                result = response.getSourceAsMap();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceAccessException(-1, e.getMessage());
        }
    }

    protected BoolQueryBuilder wrapRange(BoolQueryBuilder queryBuilder, Range range, String key) {
        if (range == null) {
            return queryBuilder;
        }
        Object from = range.getFrom();
        Object to = range.getTo();
        if (from != null && to != null) {
            queryBuilder.must(QueryBuilders.rangeQuery(key).from(from).to(to));
        } else if (from != null && to == null) {
            queryBuilder.must(QueryBuilders.rangeQuery(key).from(from).includeLower(false));
        } else if (from == null && to != null) {
            queryBuilder.must(QueryBuilders.rangeQuery(key).to(to));
        }
        return queryBuilder;
    }

    //对正数范围的数据搜索,过滤0及负数
    protected BoolQueryBuilder wrapPositiveRange(BoolQueryBuilder queryBuilder, Range range, String key) {
        if (range == null) {
            return queryBuilder;
        }
        Object from = range.getFrom();
        Object to = range.getTo();
        if (from != null && to != null) {
            if ((from instanceof Number && to instanceof Number) || (isNumeric(from.toString()) && isNumeric(to.toString()))) {
                if (Double.parseDouble(from.toString()) <= 0) {
                    queryBuilder.must(QueryBuilders.rangeQuery(key).gt(0).lte(to));
                } else {
                    queryBuilder.must(QueryBuilders.rangeQuery(key).from(from).to(to));
                }
            } else {
                queryBuilder.must(QueryBuilders.rangeQuery(key).from(from).to(to));
            }
        } else if (from != null) {
            if (from instanceof Number || isNumeric(from.toString())) {
                if (Double.parseDouble(from.toString()) <= 0) {
                    queryBuilder.must(QueryBuilders.rangeQuery(key).gt(0));
                } else {
                    queryBuilder.must(QueryBuilders.rangeQuery(key).from(from));
                }
            } else {
                queryBuilder.must(QueryBuilders.rangeQuery(key).from(from));
            }
        } else if (to != null) {
            //过滤0及0以下的数据
            queryBuilder.must(QueryBuilders.rangeQuery(key).gt(0).lt(to));
        }
        return queryBuilder;
    }

    protected BoolQueryBuilder wrapList(BoolQueryBuilder queryBuilder, List<String> values, String key) {
        if (values == null || values.size() < 1) {
            return queryBuilder;
        }
        queryBuilder.must(QueryBuilders.termsQuery(key, values));
        return queryBuilder;
    }

    protected BoolQueryBuilder wrapListWithWild(BoolQueryBuilder queryBuilder, List<String> values, String key) {
        if (values == null || values.size() < 1) {
            return queryBuilder;
        }
        BoolQueryBuilder bool = new BoolQueryBuilder();
        for (String value : values) {
            //不加这个参数should里面的匹配就可有可无了
            bool.should(QueryBuilders.wildcardQuery(key, "*" + value + "*"));
        }
        bool.minimumShouldMatch(1);
        queryBuilder.must(bool);
        return queryBuilder;
    }

    protected BoolQueryBuilder wrapRegisteredCapital(BoolQueryBuilder queryBuilder, Range range) {
        if (range == null || (range.getFrom() == null && range.getTo() == null)) {
            return queryBuilder;
        }
        //获取mongo汇率列表
        Map<String, Double> allExchangeRateMap = exchangeRateRepo.getAllExchangeRateMap();
        //本系统涉及的币种
        for (String currencyType : supportCurrencyList) {
            MatchQueryBuilder matchQuery = new MatchQueryBuilder("registered_capital_unit", currencyType);
            RangeQueryBuilder rangeQuery = new RangeQueryBuilder("val_registered_capital");

            String from = null;
            String to = null;
            Double fromNum = null;
            Double toNum = null;

            if (range.getFrom() != null && isNumeric(range.getFrom().toString())) {
                if (allExchangeRateMap.containsKey(currencyType)) {
                    fromNum = Double.parseDouble(range.getFrom().toString()) / allExchangeRateMap.get(currencyType);
                } else {
                    fromNum = Double.parseDouble(range.getFrom().toString());
                }
                from = new BigDecimal(fromNum).toString();
            }

            if (range.getTo() != null && isNumeric(range.getTo().toString())) {
                if(allExchangeRateMap.containsKey(currencyType)){
                    toNum = Double.parseDouble(range.getTo().toString()) / allExchangeRateMap.get(currencyType);
                }else {
                    toNum = Double.parseDouble(range.getTo().toString());
                }
                to = new BigDecimal(toNum).toString();
            }
            if (from != null && to != null) {
                if (fromNum <= 0) {
                    rangeQuery.gt(0).lte(to);
                } else {
                    rangeQuery.gt(from).lte(to);
                }
            } else if (from != null) {
                if (fromNum <= 0) {
                    rangeQuery.gt(0);
                } else {
                    rangeQuery.from(from);
                }
            } else if (to != null) {
                rangeQuery.gt(0).lt(to);
            }
            BoolQueryBuilder bool = QueryBuilders.boolQuery().must(matchQuery).must(rangeQuery);
            queryBuilder.should(bool);
        }
        queryBuilder.minimumShouldMatch(1);

        return queryBuilder;
    }

    /**
     * 评分策略, 营销/风险字段 升序1(默认)/降序-1
     */
    public QueryBuilder scoredQuery(Integer riskSort, Integer marketSort) {

        FunctionScoreQueryBuilder functionScoreQueryBuilder =
                QueryBuilders.functionScoreQuery(
                        ScoreFunctionBuilders.scriptFunction(
                                new Script(getInlineScoreScript(riskSort, marketSort))))
                        .boostMode(CombineFunction.REPLACE)
                        .scoreMode(FunctionScoreQuery.ScoreMode.MULTIPLY);

        return functionScoreQueryBuilder;
    }

    public String getInlineScoreScript(Integer riskSort, Integer marketSort) {
        if (riskSort == null && marketSort == null) {
            return "return 1;";
        } else if (riskSort != null && marketSort == null) {
            return "def score_risk = 0;\n" +
                    "def sort_risk = " + riskSort + ";\n" +
                    "if (sort_risk == 1){\n" +
                    "    if (doc.val_risk_coefficient.value == \"高\")\n" +
                    "        score_risk = 1;\n" +
                    "    else if(doc.val_risk_coefficient.value == \"中\")\n" +
                    "        score_risk = 10;\n" +
                    "    else\n" +
                    "        score_risk = 100;\n" +
                    "}else {\n" +
                    "    if (doc.val_risk_coefficient.value == \"高\")\n" +
                    "        score_risk = 100;\n" +
                    "    else if(doc.val_risk_coefficient.value == \"中\")\n" +
                    "        score_risk = 10;\n" +
                    "    else\n" +
                    "        score_risk = 1;\n" +
                    "}\n" +
                    "return score_risk;";
        } else if (riskSort == null && marketSort != null) {
            return "def score_market = 0;\n" +
                    "def sort_market = " + marketSort + ";\n" +
                    "if (sort_market == 1){\n" +
                    "    if (doc.val_market_coefficient.value == \"优\")\n" +
                    "        score_market = 1;\n" +
                    "    else if(doc.val_market_coefficient.value == \"中\")\n" +
                    "        score_market = 10;\n" +
                    "    else\n" +
                    "        score_market = 100;\n" +
                    "}else {\n" +
                    "    if (doc.val_market_coefficient.value == \"优\")\n" +
                    "        score_market = 100;\n" +
                    "    else if(doc.val_market_coefficient.value == \"中\")\n" +
                    "        score_market = 10;\n" +
                    "    else\n" +
                    "        score_market = 1;\n" +
                    "}\n" +
                    "return score_market;";
        } else {
            return "def score_risk = 0;\n" +
                    "def score_market = 0;\n" +
                    "def sort_risk = " + riskSort + ";\n" +
                    "def sort_market = " + marketSort + ";\n" +
                    "if (sort_risk == 1){\n" +
                    "    if (doc.val_risk_coefficient.value == \"高\")\n" +
                    "        score_risk = 1;\n" +
                    "    else if(doc.val_risk_coefficient.value == \"中\")\n" +
                    "        score_risk = 10;\n" +
                    "    else\n" +
                    "        score_risk = 100;\n" +
                    "}else {\n" +
                    "    if (doc.val_risk_coefficient.value == \"高\")\n" +
                    "        score_risk = 100;\n" +
                    "    else if(doc.val_risk_coefficient.value == \"中\")\n" +
                    "        score_risk = 10;\n" +
                    "    else\n" +
                    "        score_risk = 1;\n" +
                    "}\n" +
                    "if (sort_market == 1){\n" +
                    "    if (doc.val_market_coefficient.value == \"优\")\n" +
                    "        score_market = 1;\n" +
                    "    else if(doc.val_market_coefficient.value == \"中\")\n" +
                    "        score_market = 10;\n" +
                    "    else\n" +
                    "        score_market = 100;\n" +
                    "}else {\n" +
                    "    if (doc.val_market_coefficient.value == \"优\")\n" +
                    "        score_market = 100;\n" +
                    "    else if(doc.val_market_coefficient.value == \"中\")\n" +
                    "        score_market = 10;\n" +
                    "    else\n" +
                    "        score_market = 1;\n" +
                    "}\n" +
                    "return score_risk * score_market;";
        }
    }

    //判断字符串是否是代表数字
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
