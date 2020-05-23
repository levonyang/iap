package com.haizhi.iap.mobile.exception;


import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;
import lombok.Getter;

/**
 * Created by thomas on 18/4/13.
 */
public enum ExceptionStatus implements WrapperProvider {

    FAIL_TO_MONITOR(801, "添加监控失败"),
    USER_NOT_EXISTS(802, "用户不存在");

    @Getter
    private int code;
    @Getter
    private String msg;

    ExceptionStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Wrapper get() {
        return Wrapper.builder().status(code).msg(msg).build();
    }

}
