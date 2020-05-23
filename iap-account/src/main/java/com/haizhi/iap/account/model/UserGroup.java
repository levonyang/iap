package com.haizhi.iap.account.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/2/8.
 */
@Data
@NoArgsConstructor
public class UserGroup {
    Long id;

    String name;

    Long userCount;

    Date updateTime;

    Date createTime;
}
