package com.mark.redisratelimiter.controller;

import com.mark.redisratelimiter.annotation.RedisRateLimiter;
import com.mark.redisratelimiter.ratelimit.RateLimitClient;
import com.mark.redisratelimiter.ratelimit.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Hello world!
 */
@RestController
@Slf4j
public class TokenController {

    private static final Integer REQUEST_NUM = 50;

    AtomicInteger count = new AtomicInteger(0);

    @Autowired
    RateLimitClient rateLimitClient;


    @GetMapping(value = "/getToken")
    @ResponseBody
    public String getToken(String key) {
        Token token = rateLimitClient.acquireToken(rateLimitClient.getKey(key));
        if (token.isSuccess()) {
            return "success";
        } else {
            return "failed";
        }
    }


    @GetMapping(value = "/mockMultiRequest")
    @ResponseBody
    public int mockMultiRequest(@RequestParam String key) {
        CountDownLatch countDownLatch = new CountDownLatch(REQUEST_NUM);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(REQUEST_NUM);
        AtomicInteger atomicInteger = new AtomicInteger(0);

        for(int i=0; i<REQUEST_NUM; i++) {
            new Thread(()->{
                try {
                    // 模拟同时并发REQUEST_NUM个请求
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }

                if("success".equals(getToken(key))) {
                    atomicInteger.getAndIncrement();
                } else {
                    System.out.println("too many request, has been limited to access..");
                }
                countDownLatch.countDown();
            }).start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return atomicInteger.get();
    }

    @GetMapping("test")
    @ResponseBody
    @RedisRateLimiter(rate = "20")
    public int test() {
        count.getAndIncrement();
        int curRequest = count.get();
        log.info("===> this is the {} request", curRequest);

        return curRequest;
    }
}
