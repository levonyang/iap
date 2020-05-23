package com.haizhi.iap.mobile.bean.param;

import com.haizhi.iap.mobile.enums.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by thomas on 18/4/11.
 *
 * 用于phrase match的入参
 */
@Data
public class EnterpriseSearchParam extends SearchParam
{
    private SortOption sort;
    private Filter filter;

    @Data
    public static class Filter
    {
        private List<ESEnterpriseSearchType> ranges;
        private List<EnterpriseType> enterpriseTypes;
        /**
         * 注册资本
         */
        private List<RegisterCapitalOption> capitals;
        /**
         * 注册年限
         */
        private List<RegisterYearOption> years;
    }

    @Override
    public Pair<String, String> doValidate()
    {
        if(sort != null && StringUtils.isBlank(sort.getSort().getField())) return Pair.of("sort.field", "sort.field不能为空");
        return null;
    }
}
