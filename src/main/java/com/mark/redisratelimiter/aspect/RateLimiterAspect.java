package com.mark.redisratelimiter.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.redisratelimiter.RedisUtil;
import com.mark.redisratelimiter.annotation.RedisRateLimiter;
import com.mark.redisratelimiter.ratelimit.RateLimitClient;
import com.mark.redisratelimiter.ratelimit.Token;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;


/**
 * @Description - TODO
 *
 * @author qingteng 2021年07月09日
 * @version 1.0
 */
@Component
@Aspect
@Order(1)
public class RateLimiterAspect {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RateLimitClient rateLimitClient;

    @Pointcut("@annotation(com.mark.redisratelimiter.annotation.RedisRateLimiter)")
    public void checkHasAcquire() {

    }

    @Before("checkHasAcquire()")
    public void before(JoinPoint joinPoint) throws Throwable {

        // 获取目标对象对应的字节码对象
        Class<?> targetCls = joinPoint.getTarget().getClass();

        String clsName = targetCls.getName();

        // 获取方法签名信息
        Signature signature = joinPoint.getSignature();

        // 将方法签名强转成MethodSignature类型，方便调用
        MethodSignature ms = (MethodSignature)signature;

        // 通过字节码对象以及方法签名获取目标方法对象
        Method targetMethod = targetCls.getDeclaredMethod(ms.getName(),ms.getParameterTypes());

        // 获取方法的md5
        String methodName = targetMethod.getName();
        Object[] args = joinPoint.getArgs();
        String params = "";
        ObjectMapper objectMapper = new ObjectMapper();
        for(Object arg : args) {
            params = params + objectMapper.writeValueAsString(arg);
        }
        String methodSignStr = clsName + "." + methodName;
        String key = DigestUtils.md5DigestAsHex(methodSignStr.getBytes());

        // 获取目标方法对象上注解中的属性值
        RedisRateLimiter redisRateLimiter= targetMethod.getAnnotation(RedisRateLimiter.class);

        // 获取自定义注解中rate属性的值(每秒钟产生令牌数量，即并发量)
        String rate = redisRateLimiter.rate();
        if(!redisUtil.hasKey(rateLimitClient.getKey(key))) {
            rateLimitClient.initToken(key, rate);
        }

        Token token = rateLimitClient.acquireToken(key);
        if (!token.isSuccess()) {
            throw new RuntimeException("this request has been limited to access..");
        }

    }

}
