package com.haizhi.iap.follow.model.config.event.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * Created by chenbo on 2017/12/8.
 */
@Data
public class NewInvestConfig extends MarketEventConfig {

    @JsonProperty("invest_ratio")
    Double investRatio;

    @JsonProperty("invest_amount")
    Double investAmount;

    public NewInvestConfig() {
        this.marketEventType = MarketEventType.NEW_INVEST;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("invest_ratio", getInvestRatio());
        param.put("invest_amount", getInvestAmount());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("invest_ratio") != null) {
            try {
                setInvestRatio(Double.parseDouble(param.get("invest_ratio").toString()));
            } catch (NumberFormatException ex) {
                setInvestRatio(30d);
            }
        } else {
            setInvestRatio(30d);
        }

        if (param.get("invest_amount") != null) {
            try {
                setInvestAmount(Double.parseDouble(param.get("invest_amount").toString()));
            } catch (NumberFormatException ex) {
                setInvestAmount(1000000d);
            }
        } else {
            setInvestAmount(1000000d);
        }
    }
}
