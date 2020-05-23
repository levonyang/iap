package com.haizhi.iap.follow.service;

import com.haizhi.iap.common.utils.Encoder;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenbo on 2017/10/24.
 */
public class AccessTokenTest {
    private static long EXPIRE_TIME = TimeUnit.DAYS.toMillis(10 * 365);
    private static final String SECRET_KEY = "aemI2ZfRnbm";

    @Test
    public void test() {
        JwtBuilder builder = Jwts.builder().setId(Encoder.random())
                .setExpiration(getExpireDate()).signWith(SignatureAlgorithm.HS256, SECRET_KEY);
        builder.claim("uid", 0);
        System.out.println(builder.compact());
    }

    private Date getExpireDate() {
        return new Date(System.currentTimeMillis() + EXPIRE_TIME);
    }
}
