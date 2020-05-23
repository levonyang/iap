package com.haizhi.iap.mobile.util;

import com.haizhi.iap.mobile.bean.param.ParamValidator;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by thomas on 18/4/13.
 */
public class ParamValidatorUtil
{
    public static <T extends ParamValidator> void validate(T param)
    {
        if(param == null) throw new RuntimeException("校验参数不能为空");
        Pair<String, String> pair = null;
        if((pair = param.validate()) != null)
            throw new RuntimeException(pair.getRight());
    }
}
