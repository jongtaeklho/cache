package com.test.cache.demo.controller

import com.test.cache.demo.cache.ReactorMonoCacheable
import com.test.cache.demo.cache1.MonoCacheable
import com.test.cache.demo.service.CacheService
import com.test.cache.demo.service.TestCache
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class HelloController(private val cacheService: CacheService) {

    @GetMapping("/hello")
    fun helloController(): String {
        return "hello"
    }

    @ReactorMonoCacheable(name = "test")
    @GetMapping("/test/cache")
    fun test(): Mono<Long> {
        return cacheService.test()
    }

    @MonoCacheable(name = "test1")
    @PutMapping("/test1")
    fun testcc(@RequestBody t: String): Mono<Long> {
        return Mono.just(1)
    }

    @ReactorMonoCacheable(name = "cache")
    @PutMapping("/test")
    fun testCache(@RequestBody t: Long): Mono<TestCache> {
        return cacheService.testObject(t)
    }

    @MonoCacheable(name = "redis")
    @PutMapping("/redis")
    fun testRedis(@RequestBody t: String): Mono<List<TestCache>> {
        return Mono.just(listOf(
                TestCache(
                        id = 1,
                        name = "jolho"
                ),TestCache(
                id = 1,
                name = "jolho"
        )
        ))
    }
}