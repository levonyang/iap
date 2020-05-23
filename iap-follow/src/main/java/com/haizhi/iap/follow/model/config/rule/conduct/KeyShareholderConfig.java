package com.haizhi.iap.follow.model.config.rule.conduct;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
@Data
public class KeyShareholderConfig extends ConductConfig {

    Integer layer;

    @JsonProperty("contribute_ratio")
    Double contributeRatio;

    public KeyShareholderConfig() {
        this.conductType = ConductType.KEY_SHAREHOLDER;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("layer", getLayer());
        param.put("contribute_ratio", getContributeRatio());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("layer") != null) {
            try {
                setLayer(Integer.parseInt(param.get("layer").toString()));
            } catch (NumberFormatException ex) {
                setLayer(3);
            }
        } else {
            setLayer(3);
        }
        if (param.get("contribute_ratio") != null) {
            try {
                setContributeRatio(Double.parseDouble(param.get("contribute_ratio").toString()));
            } catch (NumberFormatException ex) {
                setContributeRatio(30d);
            }
        } else {
            setContributeRatio(30d);
        }
    }
}
