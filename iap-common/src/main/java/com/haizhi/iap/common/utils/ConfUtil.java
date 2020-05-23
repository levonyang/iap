package com.haizhi.iap.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.common.factory.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 读取classpath*:/conf目录下的配置文件
 * Created by chenbo on 17/4/7.
 */
@Slf4j
public class ConfUtil {
    private static ObjectMapper objectMapper = ObjectMapperFactory.get();

    //配置文件映射以,分隔的key value
    public static Map<String, String> getConfMap(String confFile) {
        if (confFile == null || confFile.equals("")) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap();
        try {
            BufferedReader reader = new BufferedReader(buildReader(getInputStream(confFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                //过滤注释行
                if(!line.startsWith("#")){
                    String[] keyValue = line.split(",");
                    if (keyValue.length == 2) {
                        result.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            log.error("{}", e);
        }
        return result;
    }

    //配置文件一行一串字符
    public static List<String> getConfList(String confFile) {
        if (confFile == null || confFile.equals("")) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList();

        try {
            BufferedReader reader = new BufferedReader(buildReader(getInputStream(confFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                //去掉空行和注释行
                if(!line.equals("") && !line.startsWith("#")){
                    result.add(line.trim());
                }
            }
        } catch (IOException e) {
            log.error("{}", e);
        }
        return result;
    }

    //配置文件是json
    public static Map<String, Object> readJson(String confFile) {
        Map<String, Object> result = new HashMap<>();
        if (confFile != null && !confFile.equals("")) {
            InputStream inputStream = getInputStream(confFile);
            try {
                return objectMapper.readValue(buildReader(inputStream), Map.class);
            } catch (IOException e) {
                log.error("{}", e);
            }
        }

        return result;
    }

    private static InputStreamReader buildReader(InputStream inputStream) throws UnsupportedEncodingException {
        return new InputStreamReader(inputStream,"UTF-8");
    }

    public static InputStream getInputStream(String confFile) {
        String path = "classpath*:/conf/" + confFile;
        String tmpPath = path.substring("classpath*:".length(), path.length());
        ClassRelativeResourceLoader loader = new ClassRelativeResourceLoader(ConfUtil.class);
        Resource res = loader.getResource(tmpPath);
        InputStream inputStream = null;
        try {
            inputStream = res.getInputStream();
        } catch (IOException e) {
            log.error("{}", e);
        }
        return inputStream;
    }

    public static String getAbsolutePath(String classPathFile){
        String path = "classpath*:" + classPathFile;
        String tmpPath = path.substring("classpath*:".length(), path.length());
        ClassRelativeResourceLoader loader = new ClassRelativeResourceLoader(ConfUtil.class);
        Resource res = loader.getResource(tmpPath);
        try {
            return res.getFile().getAbsolutePath();
        } catch (IOException e) {
            log.error("{}", e);
        }
        return null;
    }
}