package com.haizhi.iap.mobile.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class User
{
    Long id;

    String username;

    @JsonIgnore
    String password;

    String email;

    String phone;

    /**
     * 删除状态0 正常 1 已删除
     */
    @JsonProperty("is_deleted")
    Integer isDeleted;

    /**
     * 激活状态0 未激活 1 已激活
     */
    Integer activated;

    @JsonProperty("role_id")
    Long roleId;

    @JsonProperty("last_login_time")
    Date lastLoginTime;

    @JsonProperty("login_count")
    Integer loginCount;

    @JsonProperty("group_id")
    Long groupId;

    @JsonProperty("register_time")
    Date registerTime;

    @JsonProperty("update_time")
    Date updateTime;

    public void mask() {
        this.setPassword(null);
    }
}
