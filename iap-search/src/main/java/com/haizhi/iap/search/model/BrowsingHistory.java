package com.haizhi.iap.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/2/21.
 */
@Data
@NoArgsConstructor
public class BrowsingHistory {
    Long id;

    Long userId;

    String company;

    Date createTime;

    Date updateTime;
}
