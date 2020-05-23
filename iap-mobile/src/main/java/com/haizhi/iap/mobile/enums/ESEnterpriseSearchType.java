package com.haizhi.iap.mobile.enums;

/**
 * Created by chenbo on 16/12/23.
 */
public enum ESEnterpriseSearchType
{
    ALL, NAME, KEY_PERSON, ADDRESS, BUSINESS_SCOPE, BRANCH;

    public String getName() {
        return this.name().toLowerCase();
    }
}
