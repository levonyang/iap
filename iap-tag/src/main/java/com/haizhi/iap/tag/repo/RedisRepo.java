package com.haizhi.iap.tag.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.haizhi.iap.tag.enums.Keys;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/11/4 下午5:42.
 */
@Slf4j
@Repository
public class RedisRepo {

    @Setter
    @Autowired
    private ObjectMapper objectMapper;

    @Setter
    @Autowired
    private JedisPool jedisPool;

    @Value("${redis.timeout}")
    private int timeout;

    public <T> void pushTagParentInfos(String pdKey, List<T> parentIds) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = Keys.TAG_PARENT_DES.get(pdKey);
            jedis.setex(redisKey, timeout, objectMapper.writeValueAsString(parentIds));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
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

    public <T> List<T> getTagParentInfos(String pdKey) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = Keys.TAG_PARENT_DES.get(pdKey);
            String response = jedis.get(redisKey);
            return Strings.isNullOrEmpty(response) ? null :
                    objectMapper.readValue(response, List.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
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
