package com.test.cache.demo.cache1

import com.test.cache.demo.service.TestCache
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RedisService(private val redisTemplate: ReactiveRedisTemplate<String, TestCache>) {


    fun get(key: String): Mono<TestCache> {
        print("ddd\n")
        return redisTemplate.opsForValue().get(key)
    }

    fun set(key: String, value: TestCache) {
        redisTemplate.opsForValue().set(key, value)
    }
}