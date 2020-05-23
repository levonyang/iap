package com.haizhi.iap.follow.model.config;

/**
 * Created by chenbo on 2017/12/8.
 */
public enum DateRange {
    ONE_MONTH, THREE_MONTH, SIX_MONTH, ONE_YEAR;

    public String getName() {
        return this.name().toUpperCase();
    }

    public static boolean contains(String rangeType) {
        for (DateRange range : DateRange.values()) {
            if(range.name().equalsIgnoreCase(rangeType)){
                return true;
            }
        }
        return false;
    }
}
