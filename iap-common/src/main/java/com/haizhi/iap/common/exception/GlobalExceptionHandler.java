package com.haizhi.iap.common.exception;

import com.alibaba.fastjson.support.spring.FastJsonJsonView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GlobalExceptionHandler implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler, Exception ex) {

        ModelAndView modelAndView = new ModelAndView();
        FastJsonJsonView view = new FastJsonJsonView();
        Map<String, Object> attr = new HashMap<>();

        if (ex instanceof ServiceAccessException) {
            response.setStatus(HttpServletResponse.SC_OK);
            attr.put("code", ((ServiceAccessException) ex).get().getStatus());
            attr.put("msg", ((ServiceAccessException) ex).get().getMsg());
        } else if (ex instanceof WebApplicationException) {
            response.setStatus(((WebApplicationException) ex).getResponse().getStatus());
            attr.put("code", ((WebApplicationException) ex).getResponse().getStatus());
            attr.put("msg", ((WebApplicationException) ex).getResponse().getEntity());
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            attr.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            attr.put("msg", ex.getMessage() == null ? ex : ex.getMessage());
            log.error("{}", ex);
        }
        view.setAttributesMap(attr);
        modelAndView.setView(view);
        return modelAndView;
    }
}

//import com.haizhi.iap.common.Wrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.servlet.controller.HttpServletResponse;
//
///**
// * 只能处理业务代码controller及其调用链抛出的Exception
// */
//@Slf4j
//@ControllerAdvice
////@ControllerAdvice(annotations=RestController.class)
////@ControllerAdvice(basePackages={"com.xxx","com.ooo"})
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ServiceAccessException.class)
//    @ResponseBody
//    public Wrapper exceptionHandler(ServiceAccessException ex, HttpServletResponse response) {
//        return ex.get();
//    }
//
//}
