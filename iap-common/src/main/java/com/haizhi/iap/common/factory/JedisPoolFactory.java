package com.haizhi.iap.common.factory;

import com.google.common.base.Strings;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolFactory {

    public static JedisPool get(JedisPoolConfig config,
                                String host,
                                Integer port,
                                Integer timeout,
                                String password) {

        return Strings.isNullOrEmpty(password) ?
                new JedisPool(config, host, port, timeout) :
                new JedisPool(config, host, port, timeout, password);
    }
}