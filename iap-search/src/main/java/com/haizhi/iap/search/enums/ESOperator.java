package com.haizhi.iap.search.enums;

/**
 * Created by chenbo on 16/12/23.
 */
public enum ESOperator {
    GTE,GT,LT,LTE,BOOST;

    public String getName(){
        return this.name().toLowerCase();
    }
}
