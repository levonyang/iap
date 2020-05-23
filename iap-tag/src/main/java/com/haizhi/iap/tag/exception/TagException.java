package com.haizhi.iap.tag.exception;


import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;

/**
 * Created by jianghailong on 2016/9/26.
 */
public enum TagException implements WrapperProvider {

    MISS_KEY_WORD(801, "参数key_word不能为空"),

    PARENT_TAG_NOT_EXIST(802, "父级标签不存在");

    private int code;
    private String msg;

    TagException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Wrapper get() {
        return Wrapper.builder().status(code).msg(msg).build();
    }

}
