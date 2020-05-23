package com.haizhi.iap.search.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Beneficiary {

    String company;
    
    @JsonProperty("actual_control_ratio")
    String actualControlRatio;

    @JsonProperty("benefit_type")
    String benefitType;

    String person;
    
    @JsonProperty("is_controller")
    String isController;
    
    @JsonProperty("cert_type")
    String certType;
    
    @JsonProperty("cert_no")
    String certNo;
    
    @JsonProperty("cust_id")
    String custId;
    
}
