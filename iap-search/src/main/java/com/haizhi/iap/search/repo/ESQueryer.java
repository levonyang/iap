package com.haizhi.iap.search.repo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.rescore.RescorerBuilder;

import java.io.IOException;
import java.util.List;

/**
 * Created by chenbo on 16/11/28.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESQueryer {
    private Client client;

    private String index;

    private String type;

    //结果集起始下标
    private Integer from;

    //返回结果数
    private Integer size;

    private Integer windowSize;

    private QueryBuilder queryBuilder;

    private List<AggregationBuilder> aggregationBuilders;

    private RescorerBuilder rescorer;

    private RestClient restClient; //new

    private RestHighLevelClient restHighLevelClient; //new

    private static Integer DEFAULT_RESULT_SIZE = 100;
    private static Integer DEFAULT_RESULT_FROM = 0;
    private static Integer DEFAULT_WINDOW_SIZE = 500;
    private static String DEFAULT_FETCH_FIELD = "_source";

//    public SearchResponse actionGet() {
//
//        SearchRequestBuilder requestBuilder = client.
//                prepareSearch(this.index).setTypes(this.type);
//
//        requestBuilder.setFrom(from == null ? DEFAULT_RESULT_FROM : from);
//        requestBuilder.setSize(size == null ? DEFAULT_RESULT_SIZE : size);
//
//        if (queryBuilder != null) {
//            requestBuilder.setQuery(queryBuilder);
//        }
//
//        if (aggregationBuilders != null) {
//            for (AggregationBuilder agg : aggregationBuilders) {
//                requestBuilder.addAggregation(agg);
//            }
//        }
//
//        if (rescorer != null) {
//            if (windowSize == null) {
//                windowSize = DEFAULT_WINDOW_SIZE;
//            }
//            requestBuilder.setRescorer(rescorer, windowSize);
//        }
//
//        requestBuilder = requestBuilder.highlighter(
//                new HighlightBuilder()
//                        .field("*")
//                        .requireFieldMatch(true)
//                        .encoder("html")
//        );
//        return requestBuilder.execute().actionGet();
//    }

    /**
     * 不使用transport
     * @return
     * @throws IOException
     */
    public SearchResponse actionGet() throws IOException {
        SearchRequest searchRequest = new SearchRequest(index).types(type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(from == null ? DEFAULT_RESULT_FROM : from);
        searchSourceBuilder.size(size == null ? DEFAULT_RESULT_SIZE : size);
        searchSourceBuilder.query(queryBuilder);
        if (aggregationBuilders != null) {
            for (AggregationBuilder agg : aggregationBuilders) {
                searchSourceBuilder.aggregation(agg);
            }
        }
        if (rescorer != null) {
            if (windowSize == null) {
                windowSize = DEFAULT_WINDOW_SIZE;
            }
            rescorer.windowSize(windowSize);
            searchSourceBuilder.addRescorer(rescorer);
        }
        searchSourceBuilder.highlighter(new HighlightBuilder()
                .field("*")
                .requireFieldMatch(true)
                .encoder("html"));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
    }
}
