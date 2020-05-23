package com.haizhi.iap.follow.model.config.event.risk;

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
public class JudgementConfig extends RiskEventConfig {

    @JsonProperty("case_cause_list")
    List<String> caseCauseList;

    String role;

    public JudgementConfig() {
        this.riskEventType = RiskEventType.JUDGEMENT;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("date_range", getDateRange());
        param.put("role", getRole());
        param.put("case_cause_list", getCaseCauseList());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("date_range") != null) {
            setDateRange((String) param.get("date_range"));
        }else {
            setDateRange(DateRange.ONE_MONTH.getName());
        }
        if (param.get("role") != null && LawSuitsRole.contains(param.get("role").toString())) {
            setRole((String) param.get("role"));
        }else {
            setRole(LawSuitsRole.ALL.getName());
        }
        if(param.get("case_cause_list") != null && param.get("case_cause_list") instanceof List){
            setCaseCauseList((List<String>) param.get("case_cause_list"));
        }else {
            setCaseCauseList(Lists.newArrayList("all"));
        }
    }
}
