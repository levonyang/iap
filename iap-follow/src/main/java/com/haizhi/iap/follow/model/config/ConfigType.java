package com.haizhi.iap.follow.model.config;

/**
 * Created by chenbo on 2017/12/12.
 */
public enum ConfigType {
    RISK, MARKET, MACRO, CONDUCT;

    public static boolean contains(String typeName) {
        for (ConfigType type : ConfigType.values()) {
            if (type.name().toLowerCase().equals(typeName)) {
                return true;
            }
        }
        return false;
    }
}
