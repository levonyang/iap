package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/27.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    String region;

    String phone;

    @JsonProperty("legal_man")
    String legalMan;

    String fax;

    String email;
}
