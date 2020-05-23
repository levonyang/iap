//package com.haizhi.iap.follow.model.config.event.risk;
//
//import com.google.common.collect.Maps;
//import com.haizhi.iap.follow.model.config.DateRange;
//import lombok.Data;
//
//import java.util.Map;
//
///**
// * Created by chenbo on 2017/12/8.
// */
//@Data
//public class NameChangeConfig extends RiskEventConfig {
//
//    Integer times;
//
//    public NameChangeConfig() {
//        this.riskEventType = RiskEventType.NAME_CHANGE;
//        super.setType(getType());
//        super.setName(getName());
//    }
//
//    @Override
//    public Map<String, Object> getParam() {
//        Map<String, Object> param = Maps.newHashMap();
//        param.put("date_range", getDateRange());
//        param.put("times", getTimes());
//        return param;
//    }
//
//    @Override
//    public void setParam(Map<String, Object> param) {
//        if (param.get("date_range") != null) {
//            setDateRange((String) param.get("date_range"));
//        } else {
//            setDateRange(DateRange.ONE_YEAR.getName());
//        }
//        if (param.get("times") != null) {
//            try {
//                setTimes(Integer.parseInt(param.get("times").toString()));
//            } catch (NumberFormatException ex) {
//                setTimes(2);
//            }
//        } else {
//            setTimes(2);
//        }
//    }
//}
