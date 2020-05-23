package com.haizhi.iap.mobile.bean.normal;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Collection;
import java.util.List;

/**
 * Created by thomas on 18/4/17.
 */
@Data
public class EsTermQuery extends AbstractEsQuery
{
    protected Pair<String, Object> query;

    @Builder
    public EsTermQuery(Collection<String> indices, Collection<String> types, Integer offset, Integer size, List<String>projections, Collection<Sort> sorts, Pair<String, Object> query)
    {
        super(indices, types, offset, size, projections, sorts);
        this.query = query;
    }

    @Override
    public QueryBuilder toFilter()
    {
        if(query != null && StringUtils.isNotBlank(query.getLeft()) && query.getRight() != null)
            return QueryBuilders.termQuery(query.getLeft(), query.getRight());
        return null;
    }
}
