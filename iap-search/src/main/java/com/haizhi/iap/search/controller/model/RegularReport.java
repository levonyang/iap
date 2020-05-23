package com.haizhi.iap.search.controller.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by chenbo on 17/2/14.
 */
@Data
@NoArgsConstructor
public class RegularReport {
    List<Object> first;

    List<Object> second;

    List<Object> third;

    List<Object> fourth;

    Long totalCount;
}
