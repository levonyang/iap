package com.haizhi.iap.follow.model.config.event.market;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by chenbo on 2017/12/8.
 */
public class TaxLevelAConfig extends MarketEventConfig {

    public TaxLevelAConfig() {
        this.marketEventType = MarketEventType.TAX_LEVEL_A;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        return Maps.newHashMap();
    }

    @Override
    public void setParam(Map<String, Object> param){

    }
}
