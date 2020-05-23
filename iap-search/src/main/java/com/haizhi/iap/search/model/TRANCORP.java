package com.haizhi.iap.search.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TRANCORP {
    Long id;

    @JsonProperty("cust_id")
    String custId;
    
    @JsonProperty("company")
    String company;

    @JsonProperty("rgst_addr")
    String rgstAddr;
    
    @JsonProperty("cont_tel")
    String contTel;

    @JsonProperty("asset_size")
    String assetSize;
    
    @JsonProperty("tran_amt_sname")
    Integer tranAmtSname;
    
    @JsonProperty("tran_count_sname")
    String tranCountSname;
    
    @JsonProperty("tran_amt")
    String tranAmt;

    @JsonProperty("tran_count")
    String tranCount;
    
    @JsonProperty("enterprise_scale")
    String corpSizeCd;//enterpriseScale;
    
    @JsonProperty("listed_place")
    String listedPlace;
    
 /* @JsonProperty("corp_size_cd")
    String  corpSizeCd;*/
    
    Integer type;
}
