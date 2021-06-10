package com.test.cache.demo.controller

import com.test.cache.demo.cache.ReactorMonoCacheable
import com.test.cache.demo.service.CacheService
import com.test.cache.demo.service.TestCache
import org.springframework.web.bind.annotation.GetMapping
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

    @ReactorMonoCacheable(name = "cachee")
    @GetMapping("/test")
    fun testCache(): Mono<TestCache> {
        return cacheService.testObject()
    }
}