package com.haizhi.iap.search.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@JsonInclude(Include.ALWAYS)
public class CompanyRelate {
    Long id;

    @JsonProperty("company")
    String company;
    
    @JsonProperty("relate_company")
    String relateCompany;

    @JsonProperty("relate_type")
    String relateType;
    
    @JsonProperty("capital")
    String capital;

    @JsonProperty("company_type")
    String companyType;
    
    @JsonProperty("region")
    String region;
    
    @JsonProperty("contact")
    String contact;
    
    @JsonProperty("enterprise_scale")
    String enterpriseScale;
    
    @JsonProperty("listed_place")
    String listedPlace;
    
    @JsonProperty("reg_address")
    String regAddress;
    
}
