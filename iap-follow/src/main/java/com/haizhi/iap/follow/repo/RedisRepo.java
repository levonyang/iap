package com.haizhi.iap.follow.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.haizhi.iap.common.utils.Encoder;
import com.haizhi.iap.follow.enums.Keys;
import com.haizhi.iap.follow.controller.model.CompanyImportAck;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenbo on 17/1/10.
 */
@Repository
public class RedisRepo {
    @Setter
    @Autowired
    private ObjectMapper objectMapper;

    @Setter
    @Autowired
    private JedisPool jedisPool;

    private Integer CACHE_HALF_DAY = 12 * 60 * 60;
    private static long EXPIRE_TIME = TimeUnit.DAYS.toMillis(10 * 365);
    private static final String SECRET_KEY = "aemI2ZfRnbm";

    public String pushImportCache(CompanyImportAck ack) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = Keys.IMPORT.get(UUID.randomUUID().toString());
            jedis.setex(redisKey, CACHE_HALF_DAY, objectMapper.writeValueAsString(ack));
            return redisKey;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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

    public CompanyImportAck getImportCache(String cacheKey) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String response = jedis.get(cacheKey);
            return Strings.isNullOrEmpty(response) ? null :
                    objectMapper.readValue(response, CompanyImportAck.class);
        } catch (Exception e) {
            e.printStackTrace();
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

    public String pushTaskCache(Long taskId) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = Keys.EXPORT_TASK.get(taskId.toString());
//            jedis.setnx(redisKey, objectMapper.writeValueAsString(taskID));
            jedis.setex(redisKey, CACHE_HALF_DAY, objectMapper.writeValueAsString(taskId));
            return redisKey;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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

    public Long getTaskCache(Long taskId) {
        if (taskId == null) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = Keys.EXPORT_TASK.get(taskId.toString());
            String response = jedis.get(redisKey);
            return Strings.isNullOrEmpty(response) ? null :
                    objectMapper.readValue(response, Long.class);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void removeTaskCache(Long taskId) {
        if (taskId == null) {
            return;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = Keys.EXPORT_TASK.get(taskId.toString());
            String response = jedis.get(redisKey);
            if (!Strings.isNullOrEmpty(response))
                jedis.del(redisKey);
        } catch (Exception e) {
            e.printStackTrace();
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

    public String getPermanentToken() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = generateKey(0L, 0);
            return jedis.get(redisKey);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void savePermanentToken() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = generateKey(0L, 0);
            String token = generateToken();
            jedis.setnx(redisKey, token);
        } catch (Exception e) {
            e.printStackTrace();
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

    private String generateToken(){
        JwtBuilder builder = Jwts.builder().setId(Encoder.random())
                .setExpiration(getExpireDate()).signWith(SignatureAlgorithm.HS256, SECRET_KEY);
        builder.claim("uid", 0);
        return builder.compact();
    }

    private Date getExpireDate() {
        return new Date(System.currentTimeMillis() + EXPIRE_TIME);
    }

    private String generateKey(Long userId, int deviceType) {
        return "user_session_" + deviceType + "_" + userId;
    }
}
