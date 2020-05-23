package com.haizhi.iap.search.enums;

/**
 * Created by chenbo on 16/12/23.
 */
public enum ESEnterpriseSearchType {
    _ALL, REGISTERED_CODE, UNIFIED_SOCIAL_CREDIT_CODE, NAME, KEY_PERSON, TRADEMARK, ADDRESS, BUSINESS_SCOPE;

    public String getName() {
        return this.name().toLowerCase();
    }
}
