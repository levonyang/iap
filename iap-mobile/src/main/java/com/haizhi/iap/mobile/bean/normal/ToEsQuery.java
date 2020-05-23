package com.haizhi.iap.mobile.bean.normal;

/**
 * Created by thomas on 18/4/13.
 *
 * 将一个业务相关的query参数转换为底层的MongoQuery
 */
public interface ToEsQuery
{
    EsQuery toEsQuery();
}
