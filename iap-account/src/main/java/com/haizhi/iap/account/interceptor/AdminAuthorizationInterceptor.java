package com.haizhi.iap.account.interceptor;

import com.haizhi.iap.account.exception.AccountException;
import com.haizhi.iap.account.model.User;
import com.haizhi.iap.account.repo.UserRepo;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.exception.ServiceAccessException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    @Setter
    private UserRepo userRepo;

    private AdminAuthorizationInterceptor(){

    }

    public AdminAuthorizationInterceptor(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    private boolean isAdmin(Long userId) {
        User user = userRepo.findById(userId);
        return userId != null && user.getRoleId().equals(0l);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = DefaultSecurityContext.getUserId();

        if (!isAdmin(userId)) {
            throw new ServiceAccessException(AccountException.NEED_ADMIN_ACCESS);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
