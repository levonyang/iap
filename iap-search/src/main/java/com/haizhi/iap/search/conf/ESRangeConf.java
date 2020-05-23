package com.haizhi.iap.search.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 16/12/26.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESRangeConf {
    private String key;

    private Comparable from;

    private Comparable to;

}
