//package com.haizhi.iap.common.aop;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.haizhi.iap.common.factory.ObjectMapperFactory;
//import com.haizhi.iap.common.auth.DefaultSecurityContext;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.text.SimpleDateFormat;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Aspect
//@Component
//public class HttpAspect {
//    private String requestPath = null; // 请求地址
//    private Long userId = null; // 用户名
//    private Map<?, ?> inputParamMap = null; // 传入参数
//    private Map<String, Object> outputParamMap = null; // 存放输出结果
//    private long startTimeMillis = 0; // 开始时间
//    private long endTimeMillis = 0; // 结束时间
//
//    private ObjectMapper objectMapper = ObjectMapperFactory.get();
//
//    /**
//     * @param joinPoint
//     * @Title：doBeforeInServiceLayer
//     * @Description: 方法调用前触发
//     * 记录开始时间
//     * @author shaojian.yu
//     * @date 2014年11月2日 下午4:45:53
//     */
//    @Before("execution(* com.haizhi.iap.*.controller..*.*(..))")
//    public void doBeforeInServiceLayer(JoinPoint joinPoint) {
//        startTimeMillis = System.currentTimeMillis(); // 记录方法开始执行的时间
//    }
//
//    /**
//     * @param joinPoint
//     * @Title：doAfterInServiceLayer
//     * @Description: 方法调用后触发
//     * 记录结束时间
//     * @author shaojian.yu
//     * @date 2014年11月2日 下午4:46:21
//     */
//    @After("execution(* com.haizhi.iap.*.controller..*.*(..))")
//    public void doAfterInServiceLayer(JoinPoint joinPoint) {
//        endTimeMillis = System.currentTimeMillis(); // 记录方法执行完成的时间
//        this.printOptLog();
//    }
//
//    /**
//     * @param pjp
//     * @return
//     * @throws Throwable
//     * @Title：doAround
//     * @Description: 环绕触发
//     * @author shaojian.yu
//     * @date 2014年11月3日 下午1:58:45
//     */
//    @Around("execution(* com.haizhi.iap.*.controller..*.*(..))")
//    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
//
//        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
//        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
//        HttpServletRequest request = sra.getRequest();
//
//        userId = DefaultSecurityContext.getUserId();
//        // 获取输入参数
//        inputParamMap = request.getParameterMap();
//        // 获取请求地址
//        requestPath = request.getRequestURI();
//
//        // 执行完方法的返回值：调用proceed()方法，就会触发切入点方法执行
//        outputParamMap = new HashMap<String, Object>();
//        Object result = pjp.proceed();// result的值就是被拦截方法的返回值
//        outputParamMap.put("result", result);
//
//        return result;
//    }
//
//    /**
//     * @Title：printOptLog
//     * @Description: 输出日志
//     * @author shaojian.yu
//     * @date 2014年11月2日 下午4:47:09
//     */
//    private void printOptLog() {
//        String optTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTimeMillis);
//        try {
//            log.info("\n User: {} ;"
//                    + "\n Url: {} ;"
//                    + "\n Start time: {} , Cost time: {} ms ;"
//                    + "\n Param: {} ;"
//                    + "\n Result: {} .",
//                    userId, requestPath, optTime, endTimeMillis - startTimeMillis,
//                    objectMapper.writeValueAsString(inputParamMap), objectMapper.writeValueAsString(outputParamMap));
//        } catch (JsonProcessingException ex) {
//            ex.printStackTrace();
//        }
//
//    }
//}
