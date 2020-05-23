package com.haizhi.iap.tag.enums;

/**
 * Created by chenbo on 17/1/10.
 */
public enum Keys {

    TAG_PARENT_DES;

    public String get(String key) {
        return name().toLowerCase() + ":" + key;
    }

}
