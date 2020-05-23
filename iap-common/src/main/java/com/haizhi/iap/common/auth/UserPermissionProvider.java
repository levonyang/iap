package com.haizhi.iap.common.auth;

import java.util.Collections;
import java.util.Set;

public class UserPermissionProvider implements PermissionProvider {

    @Override
    public Set<String> getPermissionByUserId(Long userId) {
        return Collections.emptySet();
    }

}
