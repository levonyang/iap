package com.haizhi.iap.configure.repo;

import com.google.common.base.Strings;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Repository
public class RedisRepo {

    @Setter
    @Autowired
    JedisPool jedisPool;

    public void pushLock() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = getLockKey();
            jedis.setex(redisKey, 10 * 60, "1");
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

    public Integer getLock() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = getLockKey();
            String value = jedis.get(redisKey);
            return Strings.isNullOrEmpty(value) ? null : Integer.parseInt(value);
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

    public String getLockKey() {
        return "config_dispatcher_lock";
    }

    public void releaseLock() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = getLockKey();
            jedis.del(redisKey);
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
}
