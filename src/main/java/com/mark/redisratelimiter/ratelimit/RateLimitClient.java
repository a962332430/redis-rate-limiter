package com.mark.redisratelimiter.ratelimit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

@Service
public class RateLimitClient {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Qualifier("getRedisScript")
    @Resource
    RedisScript<Long> ratelimitLua;
    @Qualifier("getInitRedisScript")
    @Resource
    RedisScript<Long> ratelimitInitLua;

    public Token initToken(String key) {
        return initToken(key, "100");
    }

    public Token initToken(String key, String rate){
        Token token = Token.SUCCESS;
        Long currMillSecond = stringRedisTemplate.execute(
                (RedisCallback<Long>) redisConnection -> redisConnection.time()
        );
        /**
         * redis.pcall("HMSET",KEYS[1],
         "last_mill_second",ARGV[1],
         "curr_permits",ARGV[2],
         "max_burst",ARGV[3],
         "rate",ARGV[4],
         "app",ARGV[5])
         */
        Long acquire = stringRedisTemplate.execute(ratelimitInitLua,
                Collections.singletonList(getKey(key)), currMillSecond.toString(), "1", "10", rate, "test");
        if (acquire == 1) {
            token = Token.SUCCESS;
        } else if (acquire == 0) {
            token = Token.SUCCESS;
        } else {
            token = Token.FAILED;
        }
        return token;
    }
    /**
     * 获得key操作
     *
     * @param key
     * @return
     */
    public Token acquireToken(String key) {
        return acquireToken(key, 1);
    }

    public Token acquireToken(String key, Integer permits) {
        Token token = Token.SUCCESS;
        Long currMillSecond = stringRedisTemplate.execute(
                (RedisCallback<Long>) redisConnection -> redisConnection.time()
        );

        Long acquire = stringRedisTemplate.execute(ratelimitLua,
                Collections.singletonList(getKey(key)), currMillSecond.toString(), permits.toString());
        if (acquire == 1) {
            token = Token.SUCCESS;
        } else {
            token = Token.FAILED;
        }
        return token;
    }

    public String getKey(String key) {
        return Constants.RATE_LIMIT_KEY + key;
    }

}
