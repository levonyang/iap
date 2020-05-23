package com.haizhi.iap.follow.model.config.event.market;

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
public class NewBranchConfig extends MarketEventConfig {

    @JsonProperty("province_list")
    List<String> provinceList;

    @JsonProperty("branch_register_capital")
    Double branchRegisterCapital;

    public NewBranchConfig() {
        this.marketEventType = MarketEventType.NEW_BRANCH;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("province_list", provinceList);
        param.put("branch_register_capital", branchRegisterCapital);
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("province_list") != null && param.get("province_list") instanceof List) {
            setProvinceList((List<String>) param.get("province_list"));
        } else {
            setProvinceList(Lists.newArrayList("all"));
        }
        if (param.get("branch_register_capital") != null) {
            try {
                setBranchRegisterCapital(Double.parseDouble(param.get("branch_register_capital").toString()));
            } catch (NumberFormatException ex) {
                setBranchRegisterCapital(1000000d);
            }
        } else {
            setBranchRegisterCapital(1000000d);
        }
    }
}
