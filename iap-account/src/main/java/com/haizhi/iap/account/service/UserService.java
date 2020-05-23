package com.haizhi.iap.account.service;

import com.haizhi.iap.account.controller.model.Authorization;
import com.haizhi.iap.account.model.User;

/**
 * Created by chenbo on 2017/9/13.
 */
public interface UserService {

    Authorization generateAuthorization(User user);

    Authorization login(User user, int deviceType);
    
    Authorization autoLogin(User user, int deviceType);

    void clearSession(Long userId, int deviceType);

    void create(User register);
}
