package com.haizhi.iap.mobile.bean.param;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by thomas on 18/4/16.
 */
@Data
public class ParamWithUserInfo implements ParamValidator
{
    public static final String FIELD_USER_NAME = "username";

    protected String username;

    @Override
    public Pair<String, String> validate()
    {
        if(StringUtils.isBlank(username)) return Pair.of("username", "username不能为空");
        return null;
    }
}
