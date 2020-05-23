package com.haizhi.iap.follow.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/27 23:23
 */
public class UrlUtils {

    /**
     * 从url连接获取文件流
     * @param urlPath 文件路径
     * @return 返回下载文件
     */
    private static InputStream forInputStream(String urlPath,String requestMethod) throws IOException {
        // 统一资源
        URL url = new URL(urlPath);
        // 连接类的父类，抽象类
        URLConnection urlConnection = url.openConnection();
        // http的连接类
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        //设置超时
        httpURLConnection.setConnectTimeout(1000*5);
        //设置请求方式，默认是GET
        httpURLConnection.setRequestMethod(requestMethod);
        // 设置字符编码
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
        httpURLConnection.connect();
        // 文件大小
        int fileLength = httpURLConnection.getContentLength();

        // 建立链接从请求中获取数据
        URLConnection con = url.openConnection();
        InputStream inputStream = httpURLConnection.getInputStream();
        return inputStream;
    }

    public static InputStream postForInputStream(String urlPath) throws IOException {
        return forInputStream(urlPath,"POST");
    }

    public static InputStream getForInputStream(String urlPath) throws IOException {
        return forInputStream(urlPath,"GET");
    }
}
