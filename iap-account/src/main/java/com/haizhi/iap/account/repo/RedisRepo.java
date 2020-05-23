package com.haizhi.iap.account.repo;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by chenbo on 2017/9/13.
 */
@Repository
public class RedisRepo {

    @Setter
    @Autowired
    JedisPool jedisPool;

    private int QUARTER_OF_HOUR = 3 * 60 * 60;

    public void setLoginFailedCount(Long userId, Integer count) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.setex(Keys.LOGIN_FAILED.getKey(userId), QUARTER_OF_HOUR, count.toString());
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

    public int getLoginFailedCount(Long userId) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String cache = jedis.get(Keys.LOGIN_FAILED.getKey(userId));
            if (cache == null) {
                return 0;
            } else {
                return Integer.parseInt(cache);
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
        return 0;
    }

    public void clearLoginFailedCount(Long userId) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(Keys.LOGIN_FAILED.getKey(userId));
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

    private enum Keys {
        LOGIN_FAILED;

        public String getName() {
            return this.name().toLowerCase();
        }

        public String getKey(Long userId) {
            return getName() + "_" + userId;
        }
    }

}
