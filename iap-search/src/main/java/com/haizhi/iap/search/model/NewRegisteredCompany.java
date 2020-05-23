package com.haizhi.iap.search.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class NewRegisteredCompany {

    @JsonProperty("_id")
    private String id;

    private String company;
    
    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("organization_code")
    private String organizationCode;
    
    @JsonProperty("company_type")
    private String companyType;

    private String corporation;
    
    @JsonProperty("registered_capital")
    private String registeredCapital;
    
    
    @JsonProperty("main_industry")
    private String mainIndustry;
    
    @JsonProperty("registered_address")
    private String registeredAddress;
    
    private String region;

    private String contact;
    
    private Integer type;
    
    @JsonProperty("enterprise_scale")
    private String enterpriseScale;
    
    @JsonProperty("listed_place")
    String listedPlace;

    @JsonProperty("page_rank")
    private String pageRank;
    
}
