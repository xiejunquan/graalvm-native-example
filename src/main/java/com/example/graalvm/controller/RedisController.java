package com.example.graalvm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author xiejunquan
 * @create 2024/5/13 10:45
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/redis")
public class RedisController {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Qualifier("twoReactiveRedisTemplate")
    private final ReactiveStringRedisTemplate twoRedisTemplate;

    @RequestMapping("/set")
    public Mono<Boolean> set(String key, String value) {
        return redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(60));
    }

    @RequestMapping("/get")
    public Mono<String> get(String key) {
        return twoRedisTemplate.opsForValue().get(key);
    }
}
