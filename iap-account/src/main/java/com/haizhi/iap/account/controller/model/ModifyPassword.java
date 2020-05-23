package com.haizhi.iap.account.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by chenbo on 17/1/12.
 */
@Data
public class ModifyPassword {

    @JsonProperty("old_password")
    String oldPassword;

    @JsonProperty("new_password")
    String newPassword;
}
