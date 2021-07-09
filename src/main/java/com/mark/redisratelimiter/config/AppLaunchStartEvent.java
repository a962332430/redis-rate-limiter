//package com.mark.redisratelimiter.config;
//
//import com.mark.redisratelimiter.ratelimit.RateLimitClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.ApplicationListener;
//import org.springframework.stereotype.Component;
//
///**
// * @Description - 应用启动调用
// *
// * @author qingteng 2021年07月09日
// * @version 1.0
// */
//@Component
//public class AppLaunchStartEvent implements ApplicationListener<ApplicationReadyEvent> {
//
//    @Autowired
//    RateLimitClient rateLimitClient;
//
//    @Override
//    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
//        rateLimitClient.initToken("skynet");
//    }
//}
