package com.haizhi.iap.search.factory;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by chenbo on 17/2/18.
 */
@Slf4j
public class LogoFactory {
    public static URL get(String logoUrl) {
        try {
            return new URL(logoUrl);
        } catch (MalformedURLException e) {
            log.error("{}", e);
        }
        return null;
    }

    public static byte[] getFile(String imageName) {
        InputStream inputStream = LogoFactory.class.getClassLoader().getResourceAsStream("/images/" + imageName);
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100]; //buff用于存放循环读取的临时数据
        int rc = 0;
        try {
            while ((rc = inputStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return swapStream.toByteArray();
    }
}
