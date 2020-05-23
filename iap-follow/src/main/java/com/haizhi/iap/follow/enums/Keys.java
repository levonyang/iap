package com.haizhi.iap.follow.enums;

/**
 * Created by chenbo on 17/1/10.
 */
public enum Keys {

    IMPORT,
    EXPORT_TASK;

    public String get(String key) {
        return name().toLowerCase() + ":" + key;
    }

}
