package com.haizhi.iap.follow.model.config.event.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.follow.model.config.DateRange;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/8.
 */
@Data
public class BiddingConfig extends MarketEventConfig {

    @JsonProperty("date_range")
    String dateRange;

    @JsonProperty("province_list")
    List<String> provinceList;

    public BiddingConfig() {
        this.marketEventType = MarketEventType.BIDDING;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("date_range", getDateRange());
        param.put("province_list", getProvinceList());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("date_range") != null && DateRange.contains(param.get("date_range").toString())) {
            setDateRange(param.get("date_range").toString());
        } else {
            setDateRange(DateRange.ONE_MONTH.getName());
        }
        if (param.get("province_list") != null && param.get("province_list") instanceof List) {
            setProvinceList((List<String>) param.get("province_list"));
        } else {
            setProvinceList(Lists.newArrayList("all"));
        }
    }
}
