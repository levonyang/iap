package com.haizhi.iap.configure.enums;

/**
 * @Author dmy
 * @Date 2017/5/18 下午12:58.
 */
public enum CountForm {
    SUM, AVG;
    public String getName(){
        return this.name().toLowerCase();
    }
}
