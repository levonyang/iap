package com.haizhi.iap.mobile.bean.param;

import com.haizhi.iap.mobile.enums.MarketEventType;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/19.
 */
@Data
public class MarketEventSettingParam extends BasicParam
{
    private Map<String, Boolean> settings;

    @Override
    public Pair<String, String> doValidate()
    {
        Set<String> allEventNames = Arrays.stream(MarketEventType.values()).map(MarketEventType::name).collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(settings))
        {
            if(settings.entrySet().stream().allMatch(setting -> allEventNames.contains(setting.getKey())))
                return null;
            return Pair.of("settings", "非法的settings参数");
        }

        return null;
    }
}
