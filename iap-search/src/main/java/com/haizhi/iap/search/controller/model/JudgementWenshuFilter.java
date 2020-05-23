package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by chenbo on 17/3/16.
 */
@Data
public class JudgementWenshuFilter {
    @JsonProperty("case_date")
    StringRange caseDate;
}
