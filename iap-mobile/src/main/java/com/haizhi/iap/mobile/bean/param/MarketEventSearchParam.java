package com.haizhi.iap.mobile.bean.param;

import com.haizhi.iap.mobile.enums.MarketEventType;
import lombok.Data;

import java.util.List;

/**
 * Created by thomas on 18/4/19.
 *
 * 营销事件搜索接口
 */
@Data
public class MarketEventSearchParam extends SearchParamWithSort
{
    /**
     * 搜索多个公司的营销事件
     */
    private List<String> companyNames;

    /**
     * 筛选条件
     */
    private Filter filter;

    @Data
    public static class Filter
    {
        /**
         * 事件类型
         */
        private List<MarketEventType> eventTypes;
    }
}
