package com.haizhi.iap.follow.controller.model;

import lombok.Data;

/**
 * Created by chenbo on 2017/11/4.
 */
@Data
public class InternalWrapper {

    Integer code;

    Integer status;

    String msg;

    Object data;
}
