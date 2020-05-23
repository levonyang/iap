package com.haizhi.iap.mobile.bean.normal;

import lombok.Builder;
import lombok.Data;
import org.bson.conversions.Bson;

/**
 * Created by thomas on 18/4/13.
 *
 * 底层的、基本的mongo查询接口
 */
@Data
@Builder
public class MongoQuery
{
    private String table;
    private Bson filter;
    private Bson projection;
    private Bson sort;

    private Integer offset;
    private Integer size;
}
