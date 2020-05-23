package com.haizhi.iap.mobile.bean.param;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by thomas on 18/4/20.
 *
 * 公司简介入参
 */
@Data
public class BriefParam extends BasicParam
{
    private List<String> companys;

    @Override
    public Pair<String, String> doValidate()
    {
        if(CollectionUtils.isEmpty(companys)) return Pair.of("companys", "companys不能为空");
        return null;
    }
}
