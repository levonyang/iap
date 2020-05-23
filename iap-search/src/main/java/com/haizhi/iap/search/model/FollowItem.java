package com.haizhi.iap.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/1/10.
 */
@Data
@NoArgsConstructor
public class FollowItem {

    Long id;

    String companyName;

    Long userId;

    Long followListId;

    Integer isExistsIn;

    Date createTime;

    Date updateTime;
}