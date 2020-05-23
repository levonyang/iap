package com.haizhi.iap.configure.enums;

/**
 * Created by chenbo on 2017/10/10.
 */
public enum DataType {
    DETAIL, GRAPH;

    public String getName(){
        return this.name().toLowerCase();
    }
}
