package com.haizhi.iap.mobile.bean.param;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by thomas on 18/4/13.
 */
public interface ParamValidator
{
    /**
     * 参数检验接口。
     *
     * @return 若检验成功，返回null。若失败，返回(field, errorMsg)
     */
    Pair<String, String> validate();
}
