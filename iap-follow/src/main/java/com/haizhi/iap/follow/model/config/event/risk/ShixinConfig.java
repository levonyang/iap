package com.haizhi.iap.follow.model.config.event.risk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.haizhi.iap.follow.model.config.DateRange;
import lombok.Data;

import java.util.Map;

/**
 * Created by chenbo on 2017/12/8.
 */
@Data
public class ShixinConfig extends RiskEventConfig {

    @JsonProperty("executed_money")
    Double executedMoney;

    public ShixinConfig() {
        this.riskEventType = RiskEventType.SHIXIN_INFO;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("date_range", getDateRange());
        param.put("executed_money", getExecutedMoney());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("date_range") != null) {
            setDateRange((String) param.get("date_range"));
        } else {
            setDateRange(DateRange.ONE_MONTH.getName());
        }
        if (param.get("executed_money") != null) {
            try {
                setExecutedMoney(Double.parseDouble(param.get("executed_money").toString()));
            } catch (NumberFormatException ex) {
                setExecutedMoney(100000d);
            }
        } else {
            setExecutedMoney(100000d);
        }
    }
}
