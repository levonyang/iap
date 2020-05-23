package com.haizhi.iap.account.service;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haizhi.iap.account.controller.model.Authorization;
import com.haizhi.iap.account.exception.AccountException;
import com.haizhi.iap.account.model.User;
import com.haizhi.iap.account.repo.RedisRepo;
import com.haizhi.iap.account.repo.UserRepo;
import com.haizhi.iap.account.utils.SecretUtil;
import com.haizhi.iap.common.auth.UserSessionRepo;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.Encoder;

/**
 * Created by chenbo on 17/1/11.
 */
@Service
public class UserServiceImpl implements UserService {

    @Setter
    @Autowired
    UserRepo userRepo;

    @Setter
    @Autowired
    UserSessionRepo userSessionRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    private int MAX_LOGIN_FAILED_COUNT = 5;
    private int LOCK_HOUR = 3;

    public static final Long EXPIRE_TIME = TimeUnit.HOURS.toMillis(12);
    private static final String SECRET_KEY = "aemI2ZfRnbm";

    public Authorization generateAuthorization(User user) {

        Authorization authorized = new Authorization();
        authorized.setAccessToken(getAccessToken(user));
        // TODO update refresh token
        authorized.setRefreshToken(Encoder.random());
        authorized.setExpiresIn(EXPIRE_TIME);
        return authorized;
    }

    public String getAccessToken(User user) {
        JwtBuilder builder = Jwts.builder().setId(Encoder.random())
                .setExpiration(getExpireDate()).signWith(SignatureAlgorithm.HS256, SECRET_KEY);
        builder.claim("uid", user.getId());
        return builder.compact();
    }

    private Date getExpireDate() {
        return new Date(System.currentTimeMillis() + EXPIRE_TIME);
    }

    public void create(User user) {
        User register = User.builder()
                .username(user.getUsername())
                .password(SecretUtil.genHashPassword(user.getPassword()))
                .email(user.getEmail() == null ? "" : user.getEmail())
                .phone(user.getPhone() == null ? "" : user.getPhone())
                .activated(0)
                .build();
        userRepo.create(register);

        //TODO 短信验证
    }

    public Authorization login(User user, int deviceType) {
        User dbUser = userRepo.findByUsername(user.getUsername());
        //判断是否激活
        if (dbUser != null && dbUser.getId() != null) {
            int loginFailed = redisRepo.getLoginFailedCount(dbUser.getId());
            if (loginFailed > 5) {
                throw new ServiceAccessException(AccountException.TOO_MUCH_FAILED);
            }
            if (!SecretUtil.checkHashPassword(user.getPassword(), dbUser.getPassword())) {
                //记录错误次数(redis缓存),超出多少次禁止登录一段时间
                loginFailed += 1;
                redisRepo.setLoginFailedCount(dbUser.getId(), loginFailed);
                if (loginFailed == 1) {
                    throw new ServiceAccessException(AccountException.WRONG_PASS.get().getStatus(),
                            AccountException.WRONG_PASS.get().getMsg() + ",如果连续错误次数超过" + MAX_LOGIN_FAILED_COUNT + "次," +
                                    "该账号将被锁定" + LOCK_HOUR + "小时");
                } else {
                    throw new ServiceAccessException(AccountException.WRONG_PASS.get().getStatus(),
                            AccountException.WRONG_PASS.get().getMsg() + ",还剩" + (MAX_LOGIN_FAILED_COUNT + 1 - loginFailed) + "次尝试机会");
                }
            }

            if (dbUser.getActivated().equals(0)) {
                throw new ServiceAccessException(AccountException.UNACTIVATED);
            } else {
                //清空登录错误次数
                redisRepo.clearLoginFailedCount(dbUser.getId());
            }

            Authorization authorization = generateAuthorization(dbUser);
            //缓存token
            userSessionRepo.saveUserSession(dbUser.getId(), deviceType, authorization.getAccessToken());
            //更新登录次数和上次登录时间
            dbUser.setLoginCount(dbUser.getLoginCount() + 1);
            dbUser.setLastLoginTime(new Date());
            userRepo.update(dbUser);
            return authorization;
        } else {
            //没有此用户
            throw new ServiceAccessException(AccountException.USER_NOT_EXISTS);
        }
    }
    
    public Authorization autoLogin(User user, int deviceType) {
        User dbUser = userRepo.findByUsername(user.getUsername());
        //判断是否激活
        if (dbUser != null && dbUser.getId() != null) {
            
            if (dbUser.getActivated().equals(0)) {
                throw new ServiceAccessException(AccountException.UNACTIVATED);
            }

            Authorization authorization = generateAuthorization(dbUser);
            //缓存token
            userSessionRepo.saveUserSession(dbUser.getId(), deviceType, authorization.getAccessToken());
            //更新登录次数和上次登录时间
            dbUser.setLoginCount(dbUser.getLoginCount() + 1);
            dbUser.setLastLoginTime(new Date());
            userRepo.update(dbUser);
            return authorization;
        } else {
            //没有此用户
            throw new ServiceAccessException(AccountException.USER_NOT_EXISTS);
        }
    }

    public void clearSession(Long userId, int deviceType) {
        userSessionRepo.clearSession(userId, deviceType);
    }
}
