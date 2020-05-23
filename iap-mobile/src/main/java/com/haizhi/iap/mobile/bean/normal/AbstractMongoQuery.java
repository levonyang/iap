package com.haizhi.iap.mobile.bean.normal;

import com.haizhi.iap.mobile.bean.param.ParamValidator;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.conversions.Bson;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/17.
 */
@AllArgsConstructor
public abstract class AbstractMongoQuery implements ParamValidator, ToMongoQuery
{
    /**
     * 查询哪个表
     */
    protected String table;
    protected Integer offset;
    protected Integer size;
    /**
     * 查询返回哪些字段
     */
    protected List<String> projections;
    protected List<Sort> sorts;

    @Override
    public Pair<String, String> validate()
    {
        if(StringUtils.isBlank(table)) return Pair.of(table, "table不能为空");
        if(offset != null && offset < 0) return Pair.of("offset", "offset不能小于0");
        if(size != null && size < 1) return Pair.of("size", "size不能小于1");
        if(!CollectionUtils.isEmpty(projections))
        {
            if(StringUtils.isAnyBlank(projections.toArray(new String[projections.size()])))
                return Pair.of("projections", "projections中的元素不能为空");
        }
        if(!CollectionUtils.isEmpty(sorts) && sorts.stream().anyMatch(sort -> StringUtils.isBlank(sort.getField())))
            return Pair.of("sorts.field", "sorts.field不能为空");
        return null;
    }

    /**
     * 将query参数转换为mongo Bson过滤条件
     * @return
     */
    public abstract Bson toFilter();

    @Override
    public MongoQuery toMongoQuery()
    {
        //filter
        Bson filterBson = toFilter();
        //sort
        Bson sortBson = null;
        if(!CollectionUtils.isEmpty(sorts))
        {
            List<Bson> sortBsons = sorts.stream().filter(sort -> sort != null && StringUtils.isNotBlank(sort.getField())).map(sort -> {
                switch (sort.getDirection())
                {
                    case ASC:
                        return Sorts.ascending(sort.getField());
                    case DESC:
                        return Sorts.descending(sort.getField());
                    default:
                        break;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(sortBsons)) sortBson = Sorts.orderBy(sortBsons);
        }

        //projection
        Bson projectionBson = null;
        if(!CollectionUtils.isEmpty(projections))
            projectionBson = Projections.fields(Projections.include(projections), Projections.excludeId());
        return MongoQuery.builder().table(table).offset(offset).size(size).filter(filterBson).projection(projectionBson).sort(sortBson).build();
    }
}
