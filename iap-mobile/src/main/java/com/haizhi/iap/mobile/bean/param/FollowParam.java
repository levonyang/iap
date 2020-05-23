package com.haizhi.iap.mobile.bean.param;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by thomas on 18/4/13.
 *
 * 关注/取关操作的参数
 */
@Data
public class FollowParam extends BasicParam
{
    private String companyName;
    private Boolean follow;

    @Override
    public Pair<String, String> doValidate()
    {
        if(StringUtils.isBlank(companyName)) return Pair.of("companyName", "companyName不能为空");
        if(follow == null) return Pair.of("follow", "follow不能为null");
        return null;
    }
}
