package com.haizhi.iap.mobile.service;

import com.haizhi.iap.mobile.bean.normal.EsQuery;
import com.haizhi.iap.mobile.bean.normal.Range;
import com.haizhi.iap.mobile.bean.param.EnterpriseSearchParam;
import com.haizhi.iap.mobile.bean.result.HasMoreResult;
import com.haizhi.iap.mobile.conf.ESEnterpriseSearchConf;
import com.haizhi.iap.mobile.conf.EsSchemaConstatns;
import com.haizhi.iap.mobile.enums.*;
import com.haizhi.iap.mobile.repo.EsSearchRepo;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/11.
 */
@Service
public class EnterpriseEsSearchService
{
    @Value("${index.enterprise_data_gov}")
    private String index;

    @Value("${type.enterprise_data_gov}")
    private String type;

    @Autowired
    private EsSearchRepo esSearchRepo;

    @Autowired
    private EnterpriseService enterpriseService;

    public HasMoreResult search(EnterpriseSearchParam enterpriseSearchParam)
    {
        EsQuery.EsQueryBuilder esQueryBuilder = EsQuery.builder().indices(Collections.singleton(index)).types(Collections.singleton(type));
        if(enterpriseSearchParam.getOffset() != null && enterpriseSearchParam.getOffset() >= 0 && enterpriseSearchParam.getSize() != null && enterpriseSearchParam.getSize() > 0)
            esQueryBuilder.offset(enterpriseSearchParam.getOffset()).size(enterpriseSearchParam.getSize());

        EnterpriseSearchParam.Filter filter = enterpriseSearchParam.getFilter();
        BoolQueryBuilder finalBuilder = QueryBuilders.boolQuery();
        //搜索范围
        BoolQueryBuilder searchRangeBuilder = QueryBuilders.boolQuery();
        if(filter == null || CollectionUtils.isEmpty(filter.getRanges()))
        {
            List<QueryBuilder> queryBuilders = subQueries(enterpriseSearchParam.getKeyword(), ESEnterpriseSearchType.ALL);
            queryBuilders.forEach(searchRangeBuilder::should);
        }
        else
        {
            List<QueryBuilder> queryBuilders = subQueries(enterpriseSearchParam.getKeyword(), filter.getRanges());
            queryBuilders.forEach(searchRangeBuilder::should);
        }
        finalBuilder.must(searchRangeBuilder);

        //注册资本
        if(filter != null && !CollectionUtils.isEmpty(filter.getCapitals()) && filter.getCapitals().size() != RegisterCapitalOption.values().length)
        {
            BoolQueryBuilder registerCapitalBuilder = QueryBuilders.boolQuery();
            filter.getCapitals().forEach(capitalOption -> {
                Range range = capitalOption.getRange();
                registerCapitalBuilder.should(QueryBuilders.rangeQuery(EsSchemaConstatns.FIELD_VAL_REGISTERED_CAPITAL).from(range.getFrom()).to(range.getTo()).includeLower(range.isIncludeLower()).includeUpper(range.isIncludeUpper()));
            });
            finalBuilder.must(registerCapitalBuilder);
        }

        //注册年限
        if(filter != null && !CollectionUtils.isEmpty(filter.getYears()) && filter.getYears().size() != RegisterYearOption.values().length)
        {
            BoolQueryBuilder registerYearBuilder = QueryBuilders.boolQuery();
            filter.getYears().forEach(yearOption -> {
                Range<Integer> range = yearOption.getRange();
                String from = range.getTo() != null ? String.format("now-%dy", range.getTo()) : null;
                String to = range.getFrom() != null ? String.format("now-%dy", range.getFrom()) : null;
                registerYearBuilder.should(QueryBuilders.rangeQuery(EsSchemaConstatns.FIELD_VAL_REGISTERED_DATE).from(from).to(to).includeLower(range.isIncludeLower()).includeUpper(range.isIncludeUpper()));
            });
            finalBuilder.must(registerYearBuilder);
        }

        //企业类型（行内企业、行外企业）
        if(filter != null && !CollectionUtils.isEmpty(filter.getEnterpriseTypes()) && filter.getEnterpriseTypes().size() != EnterpriseType.values().length)
        {
            BoolQueryBuilder enterpriseTypeBuilder = QueryBuilders.boolQuery();
            filter.getEnterpriseTypes().forEach(enterpriseType -> {
                BoolQueryBuilder insideQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.existsQuery(EsSchemaConstatns.FIELD_IS_INNER_ENTERPRISE)).must(QueryBuilders.termQuery(EsSchemaConstatns.FIELD_IS_INNER_ENTERPRISE, true));
                switch (enterpriseType)
                {
                    case INSIDE:
                        enterpriseTypeBuilder.should(insideQueryBuilder);
                        break;
                    case OUTSIDE:
                        enterpriseTypeBuilder.should(QueryBuilders.boolQuery().mustNot(insideQueryBuilder));
                        break;
                    default:break;
                }
            });
            finalBuilder.must(enterpriseTypeBuilder);
        }
        esQueryBuilder.filter(finalBuilder);

        if(enterpriseSearchParam.getSort() != null)
        {
            String field = enterpriseSearchParam.getSort().getSort().getField();
            Direction direction = enterpriseSearchParam.getSort().getSort().getDirection();
            esQueryBuilder.sorts(Collections.singleton(SortBuilders.fieldSort(field).order(direction == Direction.ASC ? SortOrder.ASC : SortOrder.DESC)));
        }

        Pair<Long, List<Map<String, Object>>> pair = esSearchRepo.searchAndCount(esQueryBuilder.build());
        List<Map<String, Object>> results = pair.getRight();
        results = results.stream().map(res -> enterpriseService.addStockSectorName(res)).collect(Collectors.toList());
        return new HasMoreResult<>(pair.getLeft(), pair.getLeft() > results.size(), results);
    }

    /**
     * 根据keyword和searchTypes构造QueryBuilder列表
     *
     * @param keyWord
     * @param searchTypes
     * @return
     */
    public List<QueryBuilder> subQueries(String keyWord, Collection<ESEnterpriseSearchType> searchTypes) {
        if(CollectionUtils.isEmpty(searchTypes)) return Collections.emptyList();
        Optional<ESEnterpriseSearchType> optional = searchTypes.stream().filter(ESEnterpriseSearchType.ALL::equals).findAny();
        if(optional.isPresent())
            return subQueries(keyWord, ESEnterpriseSearchType.ALL);
        return searchTypes.stream().flatMap(searchType -> subQueries(keyWord, searchType).stream()).collect(Collectors.toList());
    }

    public List<QueryBuilder> subQueries(String keyWord, ESEnterpriseSearchType searchType) {
        Map<ESEnterpriseSearchType, List<ESEnterpriseSearchConf>> configs = ESEnterpriseSearchConf.getConfigs();
        List<QueryBuilder> subQueries = new ArrayList<>();

        switch (searchType) {
            case ALL:
                for (List<ESEnterpriseSearchConf> confs : configs.values()) {
                    for (ESEnterpriseSearchConf conf : confs) {
                        subQueries.add(subQuery(keyWord, conf.getCol(), conf.getSearchMethod(), conf.getWeight()));
                    }
                }
                break;
            default:
                List<ESEnterpriseSearchConf> confs = configs.get(searchType);
                for (ESEnterpriseSearchConf conf : confs) {
                    subQueries.add(subQuery(keyWord, conf.getCol(), conf.getSearchMethod(), conf.getWeight()));
                }
        }
        return subQueries;
    }

    public QueryBuilder subQuery(String keyWord, String col, ESSearchMethod searchMethod, Integer weight) {

        switch (searchMethod) {
            case MATCH_PHRASE:
                return QueryBuilders.matchPhraseQuery(col, keyWord).boost(weight);
            case TERM:
                return QueryBuilders.termQuery(col, keyWord).boost(weight);
            default:
                return null;
        }
    }
}
