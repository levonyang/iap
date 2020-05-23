package com.haizhi.iap.mobile.auth;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.mobile.bean.param.ParamValidator;
import com.haizhi.iap.mobile.bean.param.SearchParam;
import com.haizhi.iap.mobile.util.ParamValidatorUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by thomas on 18/4/16.
 */
@Component
@Aspect
@EnableAspectJAutoProxy
public class ParamValidationAspect
{
    /**
     * 这些方法允许keyword为空
     */
    public final Set<String> ALLOW_EMPTY_KEYWORD_METHODS = new HashSet<>();

    @PostConstruct
    public void init()
    {
        ALLOW_EMPTY_KEYWORD_METHODS.add("followees");
        ALLOW_EMPTY_KEYWORD_METHODS.add("marketEvent");
        ALLOW_EMPTY_KEYWORD_METHODS.add("myCustomers");
    }

    @Around(value = "execution(* com.haizhi.iap.mobile.controller.*.*(..)) && args(param)")
    public Object validate(ProceedingJoinPoint joinpoint, ParamValidator param) throws Throwable
    {
        MethodSignature methodSignature = (MethodSignature) joinpoint.getSignature();
        try {
            if (ALLOW_EMPTY_KEYWORD_METHODS.contains(methodSignature.getMethod().getName()))
            {
                SearchParam searchParam = (SearchParam) param;
                ParamValidatorUtil.validate(new SearchParam(searchParam.getUsername(), searchParam.getKeyword(), searchParam.getOffset(), searchParam.getSize())
                {
                    @Override
                    public Pair<String, String> doValidate()
                    {
                        Pair<String, String> pair = super.doValidate();
                        //keyword can be empty here
                        if (pair != null && "keyword".equalsIgnoreCase(pair.getLeft())) return null;
                        return pair;
                    }
                });
            }
            else ParamValidatorUtil.validate(param);
        } catch (Exception e) {
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
        return joinpoint.proceed();
    }
}
