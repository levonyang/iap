package com.haizhi.iap.tag.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.tag.model.SearchTagResponse;
import com.haizhi.iap.tag.model.TagDetail;
import com.haizhi.iap.tag.model.TagDoc;
import com.haizhi.iap.tag.param.MapDataRequest;
import com.haizhi.iap.tag.param.TagDetailRequest;
import com.haizhi.iap.tag.param.TagDetailSearchRequest;
import com.haizhi.iap.tag.repo.ElasticSearchRepo;
import com.haizhi.iap.tag.service.ESTagService;
import com.haizhi.iap.tag.service.TagInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ESTagServiceImpl implements ESTagService {

    @Autowired
    Client client;

    @Autowired
    private ElasticSearchRepo elasticSearchRepo;

    @Autowired
    private TagInfoService tagInfoService;

    private static final int MAX_PARENT_DEEP = 0;

    @Override
    public boolean addTagCollection(String collectionName, Map<String, Object> param) {
        if (param.get("number_of_shards") != null && param.get("number_of_replicas") != null) {
            Integer shards = (Integer) param.get("number_of_shards");
            Integer replicas = (Integer) param.get("number_of_replicas");
            return elasticSearchRepo.createCollection(collectionName, shards, replicas);
        }
        return elasticSearchRepo.createCollection(collectionName);
    }

    @Override
    public SearchTagResponse searchES(TagDetailSearchRequest tagDetailSearchRequest) {
        List<TagDetailRequest> searchRequest = tagDetailSearchRequest.getSearchParams();
        BoolQueryBuilder queryBuilder = buildSearchQuery(searchRequest);
        buildFilterQuery(queryBuilder, tagDetailSearchRequest.getFilters());

        SortBuilder sort = SortBuilders.scoreSort();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(tagDetailSearchRequest.getEsIndexName()).setTypes(tagDetailSearchRequest.getEsIndexName())
                .setQuery(queryBuilder).addSort(sort);
        if (StringUtils.isNotEmpty(tagDetailSearchRequest.getOrderBy())) {
            SortOrder order = tagDetailSearchRequest.getOrder().equals("DESC") ? SortOrder.DESC : SortOrder.ASC;
            searchRequestBuilder.addSort(SortBuilders.fieldSort(tagDetailSearchRequest.getOrderBy()).order(order));
        }
        SearchResponse searchResponse = searchRequestBuilder.setFrom(tagDetailSearchRequest.getFrom()).setSize(tagDetailSearchRequest.getSize())
                                        .execute().actionGet();

        return buildSearchTagResponse(searchResponse);
    }

    @Override
    public SearchTagResponse searchWithParent(TagDetailSearchRequest tagDetailSearchRequest) {
        List<TagDetailRequest> searchRequest = tagDetailSearchRequest.getSearchParams();
        BoolQueryBuilder queryBuilder = buildSearchQueryWithParent(searchRequest);
        buildFilterQuery(queryBuilder, tagDetailSearchRequest.getFilters());

        SortBuilder sort = SortBuilders.scoreSort();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(tagDetailSearchRequest.getEsIndexName()).setTypes(tagDetailSearchRequest.getEsIndexName())
                .setQuery(queryBuilder).addSort(sort);
        if (StringUtils.isNotEmpty(tagDetailSearchRequest.getOrderBy())) {
            SortOrder order = tagDetailSearchRequest.getOrder().equals("DESC") ? SortOrder.DESC : SortOrder.ASC;
            searchRequestBuilder.addSort(SortBuilders.fieldSort(tagDetailSearchRequest.getOrderBy()).order(order));
        }
        SearchResponse searchResponse = searchRequestBuilder.setFrom(tagDetailSearchRequest.getFrom()).setSize(tagDetailSearchRequest.getSize())
                                        .execute().actionGet();

        return buildSearchTagResponse(searchResponse);
    }

    private void buildFilterQuery(BoolQueryBuilder queryBuilder, List<Map<String, Object>> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return;
        }
        for (Map<String, Object> filter : filters) {
            String filterName = (String) filter.get("field");
            if (StringUtils.isEmpty(filterName)) continue;
            Object eq = filter.get("eq");
            if (eq != null) {
                queryBuilder.filter(QueryBuilders.termQuery(filterName, eq));
            }
            Object min = filter.get("min");
            Object max = filter.get("max");
            if (min != null && max != null) {
                queryBuilder.filter(QueryBuilders.rangeQuery(filterName).gte(min).lte(max));
            } else if (min != null) {
                queryBuilder.filter(QueryBuilders.rangeQuery(filterName).gte(min));
            } else if (max != null) {
                queryBuilder.filter(QueryBuilders.rangeQuery(filterName).lte(max));
            }
        }
    }

    private BoolQueryBuilder buildSearchQueryWithParent(List<TagDetailRequest> searchRequest) {
        searchRequest = findParentWithBoost(searchRequest);
        //searchRequest = mergeField(searchRequest);

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (TagDetailRequest one : searchRequest) {
            QueryBuilder subQuery = buildSubQuery(one);
            if (subQuery == null) {
                continue;
            }
            queryBuilder.should(subQuery);
        }
        return queryBuilder;
    }

    private List<TagDetailRequest> mergeField(List<TagDetailRequest> searchRequest) {
        Map<String, TagDetailRequest> tagMaps = Maps.newHashMap();
        searchRequest.stream().forEach(item -> {
            if(tagMaps.containsKey(item.getFieldName())){
                TagDetailRequest tagDetailRequest = tagMaps.get(item.getFieldName());
                Map<String, Object> op = tagDetailRequest.getOp();
                Map<String, Object> newOp = Maps.newHashMap();
                op.keySet().stream().forEach(opitem-> {
                    String mergeStr = StringUtils.join(op.get(opitem).toString(), " ", item.getOp().get(opitem).toString());
                    newOp.put(opitem, mergeStr);
                });
                tagDetailRequest.setOp(newOp);
                tagMaps.put(item.getFieldName(), tagDetailRequest);
            } else {
                tagMaps.put(item.getFieldName(), item);
            }
        });

        return tagMaps.values().stream().collect(Collectors.toList());
    }

    private List<TagDetailRequest> findParentWithBoost(List<TagDetailRequest> searchRequest) {
        List<TagDetailRequest> withParentRequest = Lists.newArrayList();
        for (TagDetailRequest one : searchRequest) {
            float curBoost = (float) 1.0;
            one.setBoost(curBoost);
            withParentRequest.add(one);
            String value = (String) one.getOp().get("eq");
            try {
                // value应该为某个tag的id，获取父级节点并且设置boost为curBoost的1/2
                TagDetail parent = tagInfoService.getParentInfoById(Integer.parseInt(value));
                int deep = 0;
                while (parent != null) {
                    deep++;
                    if (deep > MAX_PARENT_DEEP) {
                        break;
                    }
                    TagDetailRequest p = new TagDetailRequest();
                    p.setBoost(curBoost / (float) 2.0);
                    p.setFieldName(parent.getFieldName());
                    Map<String, Object> parentOp = Maps.newHashMap();
                    parentOp.put("eq", parent.getId().toString());
                    p.setOp(parentOp);

                    withParentRequest.add(p);
                    curBoost /= 2.0;
                    parent = tagInfoService.getParentInfoById(parent.getId());
                }
            } catch (Exception e) {
                continue;
            }
        }
        return withParentRequest;
    }

    private SearchTagResponse buildSearchTagResponse(SearchResponse searchResponse) {
        SearchTagResponse response = new SearchTagResponse();
        if (searchResponse.status().getStatus() == 200) {
            response.setTotalHit(searchResponse.getHits().getTotalHits());
            List<TagDoc> tagDocList = Lists.newArrayList();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            if (searchHits != null && searchHits.length > 0) {
                for (SearchHit hit : searchHits) {
                    TagDoc doc = new TagDoc();
                    doc.setId(hit.getId());
                    doc.setFields(hit.getSourceAsMap());
                    tagDocList.add(doc);
                }
            }
            response.setDocs(tagDocList);
        }
        return response;
    }

    private BoolQueryBuilder buildSearchQuery(List<TagDetailRequest> searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (TagDetailRequest one : searchRequest) {
            QueryBuilder subQuery = buildSubQuery(one);
            if (subQuery == null) {
                continue;
            }
            queryBuilder.must(subQuery);
        }
        return queryBuilder;
    }

    private QueryBuilder buildSubQuery(TagDetailRequest tagDetailRequest) {
        String fieldName = tagDetailRequest.getFieldName();
        Map<String, Object> op = tagDetailRequest.getOp();
        float boost = tagDetailRequest.getBoost();
        if (boost < 1e-6) {
            boost = (float) (1.0);
        }
        if (fieldName.endsWith("_s")) {
            // 只有eq操作
            String eqStr = (String) op.get("eq");
            if (StringUtils.isNotEmpty(eqStr)) {
                return QueryBuilders.constantScoreQuery(QueryBuilders.matchQuery(fieldName, eqStr)).boost(boost);
            }
        } else if (fieldName.endsWith("_i")) {
            if (op.containsKey("eq")) {
                return QueryBuilders.constantScoreQuery(QueryBuilders.termQuery(fieldName, op.get("eq"))).boost(boost);
            } else {
                return QueryBuilders.constantScoreQuery(rangeTerm(fieldName, op.get("min"), op.get("max"))).boost(boost);
            }
        } else if (fieldName.endsWith("_l")) {
            if (op.containsKey("eq")) {
                return QueryBuilders.constantScoreQuery(QueryBuilders.termQuery(fieldName, op.get("eq"))).boost(boost);
            } else {
                return QueryBuilders.constantScoreQuery(rangeTerm(fieldName, op.get("min"), op.get("max"))).boost(boost);
            }
        }
        return null;
    }

    private RangeQueryBuilder rangeTerm(String fieldName, Object minO, Object maxO) {
        int min = minO == null ? Integer.MIN_VALUE : (Integer) minO;
        int max = maxO == null ? Integer.MAX_VALUE : (Integer) maxO;
        return QueryBuilders.rangeQuery(fieldName).gte(min).lte(max);
    }

    @Override
    public Integer bulkImportData(MapDataRequest mapDataRequest) {
        return elasticSearchRepo.bulkUpsertData(mapDataRequest);
    }
}
