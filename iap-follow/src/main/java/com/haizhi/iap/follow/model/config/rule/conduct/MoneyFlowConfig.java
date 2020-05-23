//package com.haizhi.iap.follow.model.config.rule.conduct;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.google.common.collect.Maps;
//import com.haizhi.iap.follow.model.config.DateRange;
//import lombok.Data;
//
//import java.util.Map;
//
///**
// * Created by chenbo on 2017/12/11.
// */
//@Data
//public class MoneyFlowConfig extends ConductConfig {
//
//    @JsonProperty("date_range")
//    String dateRange;
//
//    Integer count;
//
//    public MoneyFlowConfig() {
//        this.conductType = ConductType.MONEY_FLOW;
//        super.setType(getType());
//        super.setName(getName());
//    }
//
//    @Override
//    public Map<String, Object> getParam() {
//        Map<String, Object> param = Maps.newHashMap();
//        param.put("date_range", getDateRange());
//        param.put("count", getCount());
//        return param;
//    }
//
//    @Override
//    public void setParam(Map<String, Object> param) {
//        if (param.get("date_range") != null && DateRange.contains(param.get("date_range").toString())) {
//            setDateRange(param.get("date_range").toString());
//        } else {
//            setDateRange(DateRange.ONE_YEAR.getName());
//        }
//        if (param.get("count") != null) {
//            try {
//                setCount(Integer.parseInt(param.get("count").toString()));
//            } catch (NumberFormatException ex) {
//                setCount(10);
//            }
//        } else {
//            setCount(10);
//        }
//    }
//}
