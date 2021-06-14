package com.test.cache.demo.cache1

import reactor.core.publisher.Mono

interface ReactiveCacheManager {
    fun isSupport(name: String): Boolean
    fun <T> findCachedMono(key: Any, retriver: () -> Mono<*>, returnType: Class<T>): Mono<*>
}