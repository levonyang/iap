package com.haizhi.iap.common.auth;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenbo on 2017/9/13.
 */
public class UserSessionRepo {

    @Setter
    JedisPool jedisPool;

    private Integer CACHE_HOURS = 12;

    //这里会出现老token还可以最多用5min的问题
    private static Cache<Long, String> localCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public void saveUserSession(Long userId, int deviceType, String token) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = generateKey(userId, deviceType);
            jedis.setex(redisKey, CACHE_HOURS * 60 * 60, token);
            if (!Strings.isNullOrEmpty(token)) {
                localCache.put(userId, token);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public String getUserSession(Long userId, int deviceType) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = generateKey(userId, deviceType);
            String session = jedis.get(redisKey);
            if (!Strings.isNullOrEmpty(session)) {
                localCache.put(userId, session);
            }
            return session;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public void clearSession(Long userId, int deviceType) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = generateKey(userId, deviceType);
            jedis.del(redisKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private String generateKey(Long userId, int deviceType) {
        return "user_session_" + deviceType + "_" + userId;
    }

    /**
     * 检验token是否有效
     *
     * @param accessToken
     * @param userId
     * @param deviceType
     * @return
     */
    public boolean validate(String accessToken, long userId, int deviceType) {
        if (accessToken == null) {
            return false;
        }
        //先查本地缓存，没有再去查redis，减少redis的访问次数
        String localSession = localCache.getIfPresent(userId);
        if (Strings.isNullOrEmpty(localSession)) {
            //本地没有缓存，去查redis
            return accessToken.equals(getUserSession(userId, deviceType));
        } else {
            //本地有缓存
            if (localSession.equals(accessToken)) {
                //token与本地缓存一样，放行
                return true;
            } else {
                //token与本地缓存不一样，去查redis
                return accessToken.equals(getUserSession(userId, deviceType));
            }
        }
    }
}
