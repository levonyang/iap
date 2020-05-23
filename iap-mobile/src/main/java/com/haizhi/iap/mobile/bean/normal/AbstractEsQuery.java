package com.haizhi.iap.mobile.bean.normal;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/17.
 */
@Data
@AllArgsConstructor
public abstract class AbstractEsQuery implements ToEsQuery
{
    protected Collection<String> indices;
    protected Collection<String> types;

    protected Integer offset;
    protected Integer size;
    /**
     * 查询返回哪些字段
     */
    protected List<String> projections;
    protected Collection<Sort> sorts;

    public abstract QueryBuilder toFilter();

    @Override
    public EsQuery toEsQuery()
    {
        EsQuery.EsQueryBuilder builder = EsQuery.builder().indices(indices).types(types).projections(projections);
        if(!CollectionUtils.isEmpty(sorts))
        {
            List<SortBuilder> sortBuilders = sorts.stream().filter(sort -> sort != null && StringUtils.isNotBlank(sort.getField())).map(sort -> {
                switch (sort.getDirection())
                {
                    case ASC:
                        return SortBuilders.fieldSort(sort.getField()).order(SortOrder.ASC);
                    case DESC:
                        return SortBuilders.fieldSort(sort.getField()).order(SortOrder.DESC);
                    default:
                        break;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            builder.sorts(sortBuilders);
        }
        QueryBuilder filter = toFilter();
        if(filter != null) builder.filter(filter);
        return builder.offset(offset).size(size).build();
    }
}
