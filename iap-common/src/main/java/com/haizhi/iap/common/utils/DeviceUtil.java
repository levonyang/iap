package com.haizhi.iap.common.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chenbo on 17/4/6.
 */
public class DeviceUtil {

    public static final String Device_ID = "DeviceID";

    public static final String User_Agent = "User-Agent";

    public static int getChannel(HttpServletRequest request) {
        String ua = request.getHeader(User_Agent);
        if (ua == null) {
            return DeviceType.PC.ordinal();
        } else {
            ua = ua.toLowerCase();
        }
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("ios") || ua.contains("iphone")) {
            return DeviceType.MOBILE.ordinal();
        }
        return DeviceType.PC.ordinal();
    }

    public static String getDeviceId(HttpServletRequest request) {
        return request.getHeader(Device_ID);
    }

}
