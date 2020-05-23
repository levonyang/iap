package com.haizhi.iap.follow.model.config.event.risk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/8.
 */
@Data
public class BusinessStatusUnusualConfig extends RiskEventConfig {
    @JsonProperty("status_list")
    List<String> statusList;

    public BusinessStatusUnusualConfig() {
        this.riskEventType = RiskEventType.BUSINESS_STATUS_UNUSUAL;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("date_range", getDateRange());
        param.put("status_list", getStatusList());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("date_range") != null) {
            setDateRange((String) param.get("date_range"));
        }
        if (param.get("status_list") != null && param.get("status_list") instanceof List) {
            setStatusList((List<String>) param.get("status_list"));
        } else {
            setStatusList(Lists.newArrayList("all"));
        }
    }
}
