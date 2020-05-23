package com.haizhi.iap.tag.enums;

/**
 * @Author dmy
 * @Date 2017/11/3 下午3:12.
 */
public enum TagType {

    NONE,   //无

    ENUM,   //枚举

    INTEGER,    //整型

    LONG ;       //长整型


    public String getName(){
        return this.name().toLowerCase();
    }

}
