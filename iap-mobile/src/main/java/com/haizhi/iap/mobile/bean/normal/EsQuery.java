package com.haizhi.iap.mobile.bean.normal;

import lombok.Builder;
import lombok.Data;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.Collection;

/**
 * Created by thomas on 18/4/17.
 */
@Data
@Builder
public class EsQuery implements ToEsQuery
{
    private Collection<String> indices;
    private Collection<String> types;
    private QueryBuilder filter;
    private Collection<String> projections;
    private Collection<SortBuilder> sorts;

    private Integer offset;
    private Integer size;

    @Override
    public EsQuery toEsQuery()
    {
        return this;
    }
}
