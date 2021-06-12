package com.test.cache.demo.cache

import reactor.core.publisher.Mono

interface ReactiveCacheManage {
    fun<T> findCachedMono(cacheName: String, key: String, retriever: () -> Mono<*>, classType: Class<T>): Mono<*>
}