package com.haizhi.iap.follow.model.config.rule.conduct;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
@Data
public class KeyPersonConfig extends ConductConfig {

//    @JsonProperty("common_count")
//    Integer commonCount;
//
//    List<String> positions;

    public KeyPersonConfig() {
        this.conductType = ConductType.KEY_PERSON;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
//        param.put("common_count", getCommonCount());
//        param.put("positions", getPositions());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
//        if (param.get("common_count") != null) {
//            try {
//                setCommonCount(Integer.parseInt(param.get("common_count").toString()));
//            } catch (NumberFormatException ex) {
//                setCommonCount(1);
//            }
//        } else {
//            setCommonCount(1);
//        }
//        if (param.get("positions") != null && param.get("positions") instanceof List) {
//            setPositions((List<String>) param.get("positions"));
//        } else {
//            setPositions(Lists.newArrayList("all"));
//        }
    }
}
