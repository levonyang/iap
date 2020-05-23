package com.haizhi.iap.account.exception;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;

/**
 * Created by chenbo on 17/1/11.
 */
public enum AccountException implements WrapperProvider {
    WRONG_PASS(601, "密码错误"),

    USER_PASS_NOT_PROVIDED(602, "用户名或密码未提供"),

    UNACTIVATED(603, "账号未激活"),

    MISSING_PASSWORD(605, "密码参数缺失"),

    WRONG_ORIGINAL_PASS(606, "原密码错误"),

    ILLEGAL_NEW_PASSWORD(607, "不合法的新密码"),

    USER_NOT_EXISTS(608, "用户不存在"),

    USERNAME_EXISTS(609, "用户名已被占用"),

    USER_GROUP_EXISTS(610, "用户分组名已存在"),

    USER_GROUP_MISS(611, "用户分组名未提供"),

    USER_GROUP_NOT_EXISTS(612, "用户分组不存在"),

    MISS_ID(613, "参数id不能为空或0"),

    WRONG_DELETE_MYSELF(614, "不能删除自己"),

    SYS_GROUP_NAME(615, "\"全部\"为系统分组"),

    OLD_PW_EQ_NEW_PW(616, "新旧密码一致，信息未更改"),

    NEED_ADMIN_ACCESS(617, "需要管理员权限"),

    NAME_LIMIT(618, "组名不能超过16个字符"),

    OVER_LIMIT(619, "用户数已超限制(500),请联系管理员"),

    TOO_MUCH_FAILED(620, "密码错误次数已超限制,请过3小时后再尝试");

    private Integer status;
    private String msg;

    AccountException(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    @Override
    public Wrapper get() {
        return Wrapper.builder().status(status).msg(msg).build();
    }
}
