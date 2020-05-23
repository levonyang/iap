package com.haizhi.iap.proxy.exception;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;

/**
 * Created by chenbo on 17/2/17.
 */
public enum ProxyException implements WrapperProvider {
    USER_NOT_EXIST(901, "此用户不存在"),

    CRAWLER_SCHEDULE_SERVER_ERROR(902, "抓取调度失败"),

    NO_COMPANY_PROVIDED(903, "参数company不能为空"),

    MISS_DATA(904, "参数data不能为空"),

    CONTAINS_SPECIAL_CHAR(905, "不能含有特殊字符!"),

    ILLEGAL_COMPANY_NAME(906, "不是一个有效的公司名!"),

    RESUBMIT(907, "请勿重复上报!");

    private Integer status;
    private String msg;

    ProxyException(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    @Override
    public Wrapper get() {
        return Wrapper.builder().status(status).msg(msg).build();
    }
}
