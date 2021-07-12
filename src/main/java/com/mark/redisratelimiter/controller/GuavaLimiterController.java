package com.mark.redisratelimiter.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Description - 单机版
 *
 * @author qingteng 2021年07月12日
 * @version 1.0
 */
@RestController
@Slf4j
public class GuavaLimiterController {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final RateLimiter rateLimiter = RateLimiter.create(2);

    @GetMapping("hello")
    public String sayHello() {
        if(rateLimiter.tryAcquire()){
            System.out.println(sdf.format(new Date()));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("limit");
        }
        return "hello";
    }
}
