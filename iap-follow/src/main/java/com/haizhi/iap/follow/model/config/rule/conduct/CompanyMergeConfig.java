package com.haizhi.iap.follow.model.config.rule.conduct;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
@Data
public class CompanyMergeConfig extends ConductConfig {

    public CompanyMergeConfig() {
        this.conductType = ConductType.COMPANY_MERGE;
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
