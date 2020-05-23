package com.haizhi.iap.common.auth;

import javax.servlet.http.HttpServletRequest;

public class DefaultServletContext {

    private static final ThreadLocal<HttpServletRequest> currentDefaultContext = new ThreadLocal<>();

    public static void set(HttpServletRequest request) {
        currentDefaultContext.set(request);
    }

    public static HttpServletRequest get() {
        return currentDefaultContext.get();
    }

    public static String getContextPath() {
        HttpServletRequest request = currentDefaultContext.get();
        if (request == null) {
            return null;
        }

        String requestUrl = request.getRequestURL().toString();
        String scheme = request.getHeader("X-Scheme");
        int separatorIndex = requestUrl.indexOf("://");
        int index = requestUrl.indexOf("/", separatorIndex + 3);

        String response;
        if (scheme != null && !scheme.equals("")) {
            response = requestUrl.substring(0, index);
            response = scheme + response.substring(separatorIndex);
        } else {
            response = requestUrl.substring(0, index);
        }
        return response + request.getContextPath();
    }

    public static String getRemoteAddr() {
        HttpServletRequest request = currentDefaultContext.get();
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("x-forwarded-for");
        return (forwardedFor == null || forwardedFor.equals("")) ? request.getRemoteAddr() : forwardedFor.split(",")[0];
    }
}
