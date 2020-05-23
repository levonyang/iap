package com.haizhi.iap.search.controller.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/14.
 */
@Data
@NoArgsConstructor
public class AnnualReport {
    Integer year;

    Object report;

    Integer count;
}
