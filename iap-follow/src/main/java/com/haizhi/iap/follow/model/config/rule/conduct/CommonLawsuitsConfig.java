package com.haizhi.iap.follow.model.config.rule.conduct;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
public class CommonLawsuitsConfig extends ConductConfig {

    public CommonLawsuitsConfig() {
        this.conductType = ConductType.COMMON_LAWSUITS;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        return Maps.newHashMap();
    }

    @Override
    public void setParam(Map<String, Object> param) {

    }
}
