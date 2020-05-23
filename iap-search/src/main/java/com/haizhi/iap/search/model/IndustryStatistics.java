package com.haizhi.iap.search.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class IndustryStatistics {


    String industry;
    
    @JsonProperty("year_month")
    String yearMonth;

    @JsonProperty("caibao_type")
    String caibaoType;
    
    @JsonProperty("min_company")
    String minCompany;

    String index;
    
    Double value;
    
    @JsonProperty("max_company")
    String maxCompany;

    String year;
    
    Double  max;
    
    Double  min;
    
    Double  average;
    
    Double  median;
    
    Double  upper_quartile;
    
    Double  lower_quartile;
    
    Integer num;
    
    Double  wind;
    
    @JsonProperty("is_risk")
    Boolean isRisk;
    
    @JsonProperty("has_value")
    Boolean hasValue;
    
    @JsonProperty("has_wind")
    Boolean hasWind;
    
}
