package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * Created by haizhi on 2017/9/1.
 */
@Data
public class MonitorCard {

    private String company;
    private String time;

    private int risk;
    private int marketing;
    private int closelyRisk;
    private int closelyMarketing;

    @JsonIgnore
    private boolean riskNotify;
    @JsonIgnore
    private boolean marketingNotify;
    @JsonIgnore
    private boolean closelyRiskNotify;
    @JsonIgnore
    private boolean closelyMarketingNotify;
}
