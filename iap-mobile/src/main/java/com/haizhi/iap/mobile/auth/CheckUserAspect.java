package com.haizhi.iap.mobile.auth;

import com.haizhi.iap.mobile.bean.param.ParamWithUserInfo;
import com.haizhi.iap.mobile.exception.ExceptionStatus;
import com.haizhi.iap.mobile.repo.UserRepo;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

/**
 * Created by thomas on 18/4/16.
 */
@Component
@Aspect
@EnableAspectJAutoProxy
public class CheckUserAspect
{
    @Autowired
    private UserRepo userRepo;

    @Around(value = "execution(* com.haizhi.iap.mobile.controller.*.*(..)) && args(paramWithUserInfo)")
    public Object isUserExisted(ProceedingJoinPoint joinPoint, ParamWithUserInfo paramWithUserInfo) throws Throwable
    {
        if(paramWithUserInfo != null && userRepo.findOneByName(paramWithUserInfo.getUsername()) != null)
            return joinPoint.proceed();
        return ExceptionStatus.USER_NOT_EXISTS.get();
    }

    @Around(value = "execution(* com.haizhi.iap.mobile.controller.*.*(..)) && args(username, ..)")
    public Object isUserExisted(ProceedingJoinPoint joinPoint, String username) throws Throwable
    {
        if(StringUtils.isNotBlank(username) && userRepo.findOneByName(username) != null)
            return joinPoint.proceed();
        return ExceptionStatus.USER_NOT_EXISTS.get();
    }
}
