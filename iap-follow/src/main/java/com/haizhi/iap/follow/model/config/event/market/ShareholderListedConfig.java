package com.haizhi.iap.follow.model.config.event.market;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/8.
 */
@Data
public class ShareholderListedConfig extends MarketEventConfig {
    @JsonProperty("contribute_ratio")
    Double contributeRatio;

//    @JsonProperty("list_sector")
//    List<String> listSector;

    public ShareholderListedConfig() {
        this.marketEventType = MarketEventType.SHAREHOLDER_LISTED;
        super.setType(getType());
        super.setName(getName());
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("contribute_ratio", getContributeRatio());
//        param.put("list_sector", getListSector());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("contribute_ratio") != null) {
            try {
                this.contributeRatio = Double.parseDouble(param.get("contribute_ratio").toString());
            } catch (NumberFormatException ex) {
                this.contributeRatio = 30d;
            }
        } else {
            setContributeRatio(30d);
        }

//        if (param.get("list_sector") != null && param.get("list_sector") instanceof List) {
//            this.listSector = (List<String>) param.get("list_sector");
//        }else {
//            this.listSector = Lists.newArrayList("all");
//        }
    }
}
