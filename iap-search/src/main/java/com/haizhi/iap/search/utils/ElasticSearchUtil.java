package com.haizhi.iap.search.utils;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenbo on 16/11/25.
 */
public class ElasticSearchUtil {

    public static <E> List<E> readFromResponse(SearchResponse searchResponse, Class<E> cls){
        List<E> results = new ArrayList<E>();
        if(searchResponse != null && searchResponse.getHits() != null){
            for(SearchHit hit : searchResponse.getHits().getHits()){
                results.add(ObjectMapperUtil.readValue(hit.getSourceAsString(), cls));
            }
        }
        return results;
    }

    public static <E> List<E> readFromResponse(MultiSearchResponse searchResponse, Class<E> cls){
        List<E> results = new ArrayList<E>();
        if(searchResponse != null && searchResponse.getResponses() != null){
            for(MultiSearchResponse.Item item : searchResponse.getResponses()){
                SearchResponse response = item.getResponse();
                results.addAll(readFromResponse(response, cls));
            }
        }
        return results;
    }

    public static Number count(SearchResponse searchResponse){
        if(searchResponse != null && searchResponse.getHits() != null){
            return searchResponse.getHits().getTotalHits();
        }else {
            return null;
        }
    }
}
