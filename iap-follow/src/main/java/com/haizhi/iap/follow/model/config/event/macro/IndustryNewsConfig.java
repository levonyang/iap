package com.haizhi.iap.follow.model.config.event.macro;

import com.haizhi.iap.follow.model.Tag;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
@Data
public class IndustryNewsConfig extends MacroEventConfig {

    List<Tag> keywords;

    public IndustryNewsConfig() {
        this.macroType = MacroEventType.INDUSTRY_NEWS;
        super.setType(getType());
        super.setName(getName());
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
