//package com.haizhi.iap.follow.model.config.rule.conduct;
//
//import com.google.common.collect.Maps;
//import lombok.Data;
//
//import java.util.Map;
//
///**
// * Created by chenbo on 2017/12/11.
// */
//@Data
//public class ConcertConfig extends ConductConfig {
//
//    Integer layer;
//
//    public ConcertConfig() {
//        this.conductType = ConductType.CONCERT;
//        super.setType(getType());
//        super.setName(getName());
//    }
//
//    @Override
//    public Map<String, Object> getParam() {
//        Map<String, Object> param = Maps.newHashMap();
//        param.put("layer", getLayer());
//        return param;
//    }
//
//    @Override
//    public void setParam(Map<String, Object> param) {
//        if (param.get("layer") != null) {
//            try {
//                setLayer(Integer.parseInt(param.get("layer").toString()));
//            } catch (NumberFormatException ex) {
//                setLayer(3);
//            }
//        } else {
//            setLayer(3);
//        }
//    }
//}
