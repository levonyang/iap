package com.haizhi.iap.mobile.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/4/19.
 *
 * 营销事件类型
 */
public enum MarketEventType
{
    TAX_LEVEL("纳税等级", 102),
    WIN_BID("中标公告", 104),
    PUBLIC_SENTIMENT("舆情信息", 109),
    PATENT("专利信息", 113),
    STOCK("股市事件", 114),
    BID("招标事件", 115),
    BUSINESS("工商事件", 108, 110, 111, 112);

    @Getter
    private String description;
    @Getter
    private List<Integer> types;

    MarketEventType(String description, List<Integer> types)
    {
        this.description = description;
        this.types = types;
    }

    MarketEventType(String description, Integer... types)
    {
        this.description = description;
        this.types = Arrays.asList(types);
    }

    public String getTypesAsString()
    {
        return StringUtils.join(types, ",");
    }

    public static final Map<String, MarketEventType> MARKET_EVENT_TYPES = new HashMap<>();
    static {
        Arrays.stream(MarketEventType.values()).forEach(marketEventType -> MARKET_EVENT_TYPES.put(marketEventType.name(), marketEventType));
    }

    public static MarketEventType getMarketEventType(String name)
    {
        return MARKET_EVENT_TYPES.get(name);
    }
}
