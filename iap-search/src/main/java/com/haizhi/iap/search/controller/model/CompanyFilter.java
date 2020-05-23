package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CompanyFilter{
    NumberRange capital;

    @JsonProperty("company_type")
    List<String> companyType;

    @JsonProperty("operation_startdate")
    StringRange operationStartDate;
}