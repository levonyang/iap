package com.haizhi.iap.proxy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/1/11.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    Long id;

    String username;

    String password;

    String email;

    String phone;

    /**
     * 删除状态0 正常 1 已删除
     */
    Integer isDeleted;

    /**
     * 激活状态0 未激活 1 已激活
     */
    Integer activated;

    Integer roleId;

    Date lastLoginTime;

    Integer loginCount;

    Integer groupId;

    Date registerTime;

    Date updateTime;
}
