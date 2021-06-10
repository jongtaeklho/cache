package com.test.cache.demo.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CacheService {

    fun test(): Mono<Long> {
        print("--------no hit")
        return Mono.just(1)
    }

    fun testObject(): Mono<TestCache> {
        print("---------no hit")
        return Mono.just(
                TestCache(
                        id = 1,
                        name = "test"
                )
        )
    }
}