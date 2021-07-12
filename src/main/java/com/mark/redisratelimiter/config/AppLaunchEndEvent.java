package com.mark.redisratelimiter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * @Description - 应用启动调用
 *
 * @author qingteng 2021年07月09日
 * @version 1.0
 */
@Component
@Slf4j
public class AppLaunchEndEvent implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        log.info("app stopped..");
    }
}
