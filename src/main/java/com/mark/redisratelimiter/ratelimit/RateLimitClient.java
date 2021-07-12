package com.mark.redisratelimiter.ratelimit;


import com.mark.redisratelimiter.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component()
public class RateLimitClient {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Qualifier("getRedisScript")
    @Resource
    RedisScript<Long> ratelimitLua;
    @Qualifier("getInitRedisScript")
    @Resource
    RedisScript<Long> ratelimitInitLua;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 每次获取一个令牌，默认每秒限流100个令牌
     *
     * @param key
     * @return
     */
    public Boolean tryAcquire(String key) {
        return tryAcquire(key, 100);
    }

    /**
     * 每次获取一个令牌，可指定每秒限流100个令牌
     *
     * @param key
     * @return
     */
    public Boolean tryAcquire(String key, Integer rate) {
        if(!redisUtil.hasKey(this.getKey(key))) {
            this.initToken(key, String.valueOf(rate));
        }

        Token token = this.acquireToken(key, 1);

        return token.isSuccess();
    }


    /**
     * 拼接限流器key(rate-limit:xxx)
     *
     * @param key
     * @return
     */
    private String getKey(String key) {
        return Constants.RATE_LIMIT_KEY + key;
    }

    /**
     * 初始化限流器参数
     *
     * @param key
     * @return
     */
    private Token initToken(String key, String rate){
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
                Collections.singletonList(getKey(key)), currMillSecond.toString(), "1", "1000", rate, "test");
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
    private Token acquireToken(String key) {
        return acquireToken(key, 1);
    }

    private Token acquireToken(String key, Integer permits) {
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

}
