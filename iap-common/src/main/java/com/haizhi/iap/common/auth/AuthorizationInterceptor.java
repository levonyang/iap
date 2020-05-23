package com.haizhi.iap.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.common.factory.ObjectMapperFactory;
import com.haizhi.iap.common.utils.DeviceUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.security.SimplePrincipal;
import org.json.JSONObject;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.OutputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AuthorizationInterceptor implements HandlerInterceptor {

//    private static List<String> ignorePaths = ConfUtil.getConfList("auth.ignore");

    ObjectMapper objectMapper = ObjectMapperFactory.get();

    public static final String secretKey = "aemI2ZfRnbm";

    @Setter
    private UserSessionRepo userSessionRepo;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        DefaultServletContext.set(req);


        //this.logRequestInfo(req);

        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            NoneAuthorization noneAuthorization = ((HandlerMethod) handler).getMethodAnnotation(NoneAuthorization.class);
            if (noneAuthorization == null) {
                DefaultSecurityContext securityContext = createContext(req);

                if (securityContext.getUserPrincipal() == null) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("status", Response.Status.FORBIDDEN.getStatusCode());
                    obj.put("msg", "access_token must not empty.");
                    resp.setStatus(Response.Status.FORBIDDEN.getStatusCode());
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    //这里不能用writer,不然embed tomcat会报错
                    OutputStream out = resp.getOutputStream();
                    out.write(objectMapper.writeValueAsString(obj).getBytes());
                    out.flush();
                    out.close();
                }
            } else {
                //不需要权限的接口,捕获token过期的异常
                DefaultSecurityContext securityContext = null;
                try {
                    //不需要权限的接口也可能需要获取用户信息,所以要把用户信息设置到threadlocal
                    securityContext = createContext(req);
                } catch (WebApplicationException ex) {
                    log.debug("{}", ex);
                }
            }
        }

        //接口的权限验证也可以用这种方法,比较low
        //过滤resources/conf/auth.ignore文件中配置的路径
        /**if (!(req.getServletPath() == null || ignorePaths.contains(req.getServletPath()))) {

         }**/
        return true;
    }

    private DefaultSecurityContext createContext(HttpServletRequest req) {
        String accessToken = getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity("access_token must not empty.").build()
            );
        }

        Jws<Claims> jws = null;
        Principal principal = null;
        try {
            jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken);
            if (jws.getBody().containsKey("uid")) {
                //和redis中的token做对比,redis中没有该用户的token或者不相等 则抛403
                int deviceType = DeviceUtil.getChannel(req);
                boolean isValidateToken = userSessionRepo.validate(accessToken, Long.parseLong(String.valueOf(jws.getBody().get("uid"))), deviceType);
                if (!isValidateToken) {
                    throw new WebApplicationException(
                            Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity("token已过期,请重新登录").build()
                    );
                }

                principal = new SimplePrincipal(String.valueOf(jws.getBody().get("uid")));
            } else if (jws.getBody().containsKey("passid")) {
                principal = new SimplePrincipal(String.valueOf(jws.getBody().get("passid")));
            } else if (jws.getBody().containsKey("thirdid")) {
                principal = new SimplePrincipal(String.valueOf(jws.getBody().get("thirdid")));
            }
        } catch (JwtException e) {
            log.debug("Access Token Exception. ", e);
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOnce("code", Response.Status.FORBIDDEN);
            jsonObject.putOnce("msg", e.getMessage());
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build()
            );
//            throw new ServiceAccessException(Response.Status.FORBIDDEN.getStatusCode(), jsonObject.toString());
        }
        return new DefaultSecurityContext(jws, principal, accessToken);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) throws Exception {

    }

    protected String getAccessToken() {
        HttpServletRequest request = DefaultServletContext.get();

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isEmpty()) {
            String[] splits = authorization.split(" ");
            if (splits.length > 1) {
                return splits[1];
            }
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // query parameter
        return request.getParameter("access_token");
    }



    /**
    * @description 打印访问请求信息
    * @param request
    * @return 无
    * @author LewisLouis
    * @date 2018/8/17
    */
    private void logRequestInfo(HttpServletRequest request){
        if (!isDebugMode()){
            return;
        }

        String url = request.getRequestURI();

        String params = "";
        //请求参数打印
        Map map = request.getParameterMap();
        if (map != null && !map.isEmpty()) {
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                String[] values = (String[]) map.get(key);
                for (String value : values) {
                    params = params + "&" + key + "=" + value;
                }
            }
        }
        if (!StringUtils.isEmpty(params)){
            params = "?" + StringUtils.substring(params,1);
        }

        log.info(String.format("Access :%s%s", url, params));

    }

    /**
    * @description 判断当前是否处于Debug状态
    * @return 是否处于Debug状态
    * @author LewisLouis
    * @date 2018/8/17
    */
    private boolean isDebugMode(){
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
        return isDebug;
    }
}
