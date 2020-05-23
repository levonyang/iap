package com.haizhi.iap.mobile.bean.param;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by thomas on 18/4/16.
 */
public abstract class BasicParam extends ParamWithUserInfo
{
    /**
     * 除了父类的validate之外，自己私有的校验逻辑
     *
     * @return
     */
    public abstract Pair<String, String> doValidate();

    @Override
    public Pair<String, String> validate()
    {
        Pair<String, String> pair = super.validate();
        if(pair != null) return pair;
        return doValidate();
    }
}
