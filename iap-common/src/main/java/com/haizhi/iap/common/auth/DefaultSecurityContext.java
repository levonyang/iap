package com.haizhi.iap.common.auth;

import com.haizhi.iap.common.exception.ServiceAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.common.security.SimplePrincipal;
import org.json.JSONObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Date;

@Slf4j
public class DefaultSecurityContext implements SecurityContext {

    private static final ThreadLocal<DefaultSecurityContext> currentDefaultSecurityContext = new ThreadLocal<>();

    private Jws<Claims> jws;
    private Principal principal;
    private String accessToken;

    public DefaultSecurityContext(Jws<Claims> jws, Principal principal, String accessToken) {
        this.jws = jws;
        this.principal = principal;
        this.accessToken = accessToken;

        currentDefaultSecurityContext.set(this);
    }

    public static DefaultSecurityContext get() {
        return currentDefaultSecurityContext.get();
    }

    public static Long getUserId() {
        DefaultSecurityContext dsc = DefaultSecurityContext.get();
        if (dsc == null || dsc.getUserPrincipal() == null) {
            return null;
        }
        try {
            return Long.parseLong(dsc.getUserPrincipal().getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getSecretId() {
        return jws.getBody().getId();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getName() {
        if (principal.getName().contains(":")) {
            return principal.getName().split(":")[1];
        }
        return principal.getName();
    }

    public Date getExpireIn() {
        return jws.getBody().getExpiration();
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
