package com.test.cache.demo.cache

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import reactor.cache.CacheMono
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.lang.annotation.Documented
import java.util.function.BiFunction

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Documented
annotation class ReactorMonoCacheable(val name: String)

@Component
class ReactiveCacheManager(private val cacheManager: CacheManager) {

    fun <T> findCachedMono(cacheName: String, key: String, retriever: () -> Mono<*>, classType: Class<T>): Mono<*> {
        val cache = cacheManager.getCache(cacheName)
        return CacheMono
                .lookup(readerGeneric(cache, classType), key)
                .onCacheMissResume(
                        Mono.defer {
                            print("cache missed")
                            retriever.invoke()
                        }
                ).andWriteWith(writer(cacheName))
    }

    fun <T> readerGeneric(cache: Cache?, classType: Class<T>) = { key: String ->
        val result = cache?.get(key, classType)
        Mono.justOrEmpty(result).map<Signal<*>> { Signal.next(it) }
    }

    fun writer(cacheName: String): BiFunction<String, Signal<out Any>, Mono<Void>> = BiFunction { key, signal ->
        Mono.fromRunnable {
            if(!signal.isOnError) {
                cacheManager.getCache(cacheName)?.put(key, signal.get())
            }
        }
    }
}