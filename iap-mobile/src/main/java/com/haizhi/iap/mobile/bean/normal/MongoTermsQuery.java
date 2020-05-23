package com.haizhi.iap.mobile.bean.normal;

import com.haizhi.iap.mobile.conf.MongoSchemaConstants;
import com.mongodb.client.model.Filters;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.List;

/**
 * Created by thomas on 18/4/13.
 *
 * mongo精确查询
 */
@Data
public class MongoTermsQuery extends AbstractMongoQuery
{
    @Builder
    public MongoTermsQuery(String table, Integer offset, Integer size, List<String> projections, List<Sort> sorts, Pair<String, Collection> query)
    {
        super(table, offset, size, projections, sorts);
        this.query = query;
    }

    /**
    /**
     * (查询哪个字段，要匹配的值)
     */
    protected Pair<String, Collection> query;

    @Override
    public Pair<String, String> validate()
    {
        Pair<String, String> pair = super.validate();
        if(pair != null) return pair;
        if(query != null)
        {
            if(StringUtils.isBlank(query.getLeft())) return Pair.of("query", "query.left不能为空");
            if(query.getRight() == null) return Pair.of("query", "query.right不能为null");
        }
        return null;
    }

    @Override
    public Bson toFilter()
    {
        //logic_delete字段不存在，或者该字段值不为1
        Bson filterBson = Filters.or(Filters.exists(MongoSchemaConstants.FIELD_LOGIC_DELETE, false), Filters.ne(MongoSchemaConstants.FIELD_LOGIC_DELETE, 1));
        if(query != null)
            filterBson = Filters.and(filterBson, Filters.in(query.getLeft(), query.getRight()));
        return filterBson;
    }
}
