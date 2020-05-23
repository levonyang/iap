//package com.haizhi.iap.follow.model.config.event.risk;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.haizhi.iap.follow.model.config.DateRange;
//import lombok.Data;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by chenbo on 2017/12/8.
// */
//@Data
//public class OwingTaxConfig extends RiskEventConfig {
//
//    @JsonProperty("tax_category")
//    List<String> taxCategory;
//
//    @JsonProperty("own_money")
//    Double ownMoney;
//
//    public OwingTaxConfig() {
//        this.riskEventType = RiskEventType.OWING_TAX;
//        super.setType(getType());
//        super.setName(getName());
//    }
//
//    @Override
//    public Map<String, Object> getParam() {
//        Map<String, Object> param = Maps.newHashMap();
//        param.put("date_range", getDateRange());
//        param.put("tax_category", getTaxCategory());
//        param.put("own_money", getOwnMoney());
//        return param;
//    }
//
//    @Override
//    public void setParam(Map<String, Object> param) {
//        if (param.get("date_range") != null) {
//            setDateRange((String) param.get("date_range"));
//        } else {
//            setDateRange(DateRange.ONE_MONTH.getName());
//        }
//        if (param.get("tax_category") != null && param.get("tax_category") instanceof List) {
//            setTaxCategory((List<String>) param.get("tax_category"));
//        } else {
//            setTaxCategory(Lists.newArrayList("all"));
//        }
//        if (param.get("own_money") != null) {
//            try {
//                setOwnMoney(Double.parseDouble(param.get("own_money").toString()));
//            } catch (NumberFormatException ex) {
//                setOwnMoney(100000d);
//            }
//        } else {
//            setOwnMoney(100000d);
//        }
//    }
//}
