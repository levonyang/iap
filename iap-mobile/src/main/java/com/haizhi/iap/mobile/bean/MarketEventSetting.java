package com.haizhi.iap.mobile.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.mobile.enums.MarketEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 18/4/19.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketEventSetting
{
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private String name;

    /**
     * 以逗号分隔的事件子类型代号集合，对应notification表中的type
     * 比如工商事件有108, 110, 111, 112几个子类型
     */
    private String types;

    private String description;

    /**
     * 是否启用该事件推送
     */
    private boolean enable;

    @JsonIgnore
    public List<Integer> getTypesAsList()
    {
        List<Integer> results = new ArrayList<>();
        if(StringUtils.isNotBlank(types))
        {
            for (String code : types.split(","))
            {
                try {
                    results.add(Integer.parseInt(code));
                } catch (Exception ignore) {}
            }
        }
        return results;
    }

    public MarketEventSetting(Long id, Long userId, MarketEventType marketEventType, boolean enable)
    {
        this(id, userId, marketEventType.name(), marketEventType.getTypesAsString(), marketEventType.getDescription(), enable);
    }
}
