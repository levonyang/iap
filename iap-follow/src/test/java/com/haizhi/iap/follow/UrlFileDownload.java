package com.haizhi.iap.follow;

import com.haizhi.iap.follow.service.CustdigExportProcess;
import org.junit.Test;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/27 23:19
 */
public class UrlFileDownload {
    public static void main(String[] args) throws IOException {
        ClassRelativeResourceLoader loader = new ClassRelativeResourceLoader(CustdigExportProcess.class);
        File classpath = loader.getResource("classpath:").getFile();
        File file = new File(classpath,"generatepdf/");
        if(!file.exists()){
            file.mkdir();
        }
        String absolutePath = file.getAbsolutePath();
        System.out.println(absolutePath);
    }

    @Test
    public void testPattern(){

        Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z_]+)\\}");
        String src = "投资金额：${subconam}万，比例：${conprop}%";
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()){
            String key = matcher.group(1);
            System.out.println(key);
        }
    }

    @Test
    public void testEncode(){
        String batchid = "MTQyMTAyMDIwMDQwODIxMDAyNg==";
        try {
            String result = URLEncoder.encode(batchid,"utf8");
            System.out.println(result);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSplit(){
        String str = "123213|324343|";
        String[] split = str.split("\\|");
        System.out.println(split.length);
    }

    @Test
    public void testListSort(){
        List<Integer> list = new ArrayList<>();
        list.add(6);
        list.add(4);
        list.add(8);
        list.sort((o1,o2)->{
            return o1.compareTo(o2);
        });
        list.forEach(item->{
            System.out.println(item);
        });
    }

    @Test
    public void testMd5(){
        String result = DigestUtils.md5DigestAsHex("刘园014210".getBytes(Charset.forName("UTF-8")));
        System.out.println(result);
    }

}
