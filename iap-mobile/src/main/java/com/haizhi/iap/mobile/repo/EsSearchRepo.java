package com.haizhi.iap.mobile.repo;

import com.haizhi.iap.mobile.bean.normal.EsQuery;
import com.haizhi.iap.mobile.bean.normal.ToEsQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import java.util.*;

/**
 * Created by thomas on 18/4/11.
 */
@Repository
public class EsSearchRepo
{
    @Autowired
    private Client client;

    @PreDestroy
    public void close()
    {
        client.close();
    }

    /**
     * 通用的es搜索接口
     *
     * @param toEsQuery
     * @return
     */
    public Pair<Long, List<Map<String, Object>>> searchAndCount(ToEsQuery toEsQuery)
    {
        EsQuery esQuery = null;
        if(toEsQuery == null || (esQuery = toEsQuery.toEsQuery()) == null) return Pair.of(0L, Collections.emptyList());
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(esQuery.getIndices().toArray(new String[]{})).setTypes(esQuery.getTypes().toArray(new String[]{}));
        QueryBuilder filter = esQuery.getFilter();

        if(esQuery.getFilter() != null) searchRequestBuilder.setQuery(esQuery.getFilter());
        if(esQuery.getOffset() != null && esQuery.getOffset() >= 0) searchRequestBuilder.setFrom(esQuery.getOffset());
        if(esQuery.getSize() != null && esQuery.getSize() > 0) searchRequestBuilder.setSize(esQuery.getSize());
        if(!CollectionUtils.isEmpty(esQuery.getSorts()))
            esQuery.getSorts().forEach(searchRequestBuilder::addSort);
        SearchHits searchHits = searchRequestBuilder.get().getHits();
        List<Map<String, Object>> results = new ArrayList<>();
        if(!CollectionUtils.isEmpty(esQuery.getProjections()))
        {
            for (SearchHit searchHit : searchHits.getHits())
            {
                Map<String, Object> source = searchHit.getSourceAsMap();
                HashMap<String, Object> result = new HashMap<>();
                esQuery.getProjections().forEach(projection -> {
                    Object obj = source.get(projection);
                    if (obj != null) result.put(projection, obj);
                });
                if (!CollectionUtils.isEmpty(result)) results.add(result);
            }
        }
        else
        {
            for (SearchHit searchHit : searchHits.getHits())
            {
                Map<String, Object> source = searchHit.getSourceAsMap();
                results.add(source);
            }
        }
        return Pair.of(searchHits.totalHits, results);
    }
}
