//package com.haizhi.iap.follow.model.config.event.risk;
//
//import com.google.common.collect.Maps;
//
//import java.util.Map;
//
///**
// * Created by chenbo on 2017/12/8.
// */
//public class BlindExpandConfig extends RiskEventConfig {
//
//    public BlindExpandConfig() {
//        this.riskEventType = RiskEventType.BLIND_EXPAND;
//    }
//
//    @Override
//    public Map<String, Object> getParam() {
//        Map<String, Object> param = Maps.newHashMap();
//        param.put("date_range", getDateRange());
//        return param;
//    }
//
//    @Override
//    public void setParam(Map<String, Object> param){
//        if(param.get("date_range") != null){
//            setDateRange((String) param.get("date_range"));
//        }
//    }
//}
