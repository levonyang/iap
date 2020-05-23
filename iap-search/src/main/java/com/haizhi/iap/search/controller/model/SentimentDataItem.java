package com.haizhi.iap.search.controller.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/15.
 */
@Data
@NoArgsConstructor
public class SentimentDataItem extends DataItem {
    Long positive ;

    Long negative;

    Long neutral;
}
