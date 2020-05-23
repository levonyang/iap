package com.haizhi.iap.configure.enums;

/**
 * Created by chenbo on 2017/10/10.
 */
public enum ImportStatus {
    //未导入   导入中     导入失败    正在终止    导入中止    导入结束
    UNIMPORT(0), IMPORTING(1), FAILED(2), ABORTING(3), ABORTED(4), FINISHED(5);

    Integer code;

    ImportStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }

}
