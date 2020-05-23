package com.haizhi.iap.follow.model.config.event.macro;

import lombok.Data;

import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
@Data
public class AreaPolicyConfig extends MacroEventConfig {

    public AreaPolicyConfig() {
        this.macroType = MacroEventType.AREA_POLICY;
        super.setType(getType());
        super.setName(getName());
        super.setTypeEnName(getTypeEnName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = super.getParam();
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        super.setParam(param);
    }

}
