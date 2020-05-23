package com.haizhi.iap.common.auth;

import java.util.Set;

public interface PermissionProvider {

    Set<String> getPermissionByUserId(Long userId);

}
