package com.test.cache.demo.cache1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import com.test.cache.demo.service.TestCache
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import reactor.cache.CacheMono
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.lang.RuntimeException
import java.time.Duration
import java.util.function.BiFunction
import javax.annotation.PostConstruct
import kotlin.math.sign


class ReactiveRedisCacheManager(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, TestCache>, private val ticker: Ticker, private val name: String) : ReactiveCacheManager{
    override fun isSupport(name: String): Boolean {
        return name == this.name
    }

    override fun <T> findCachedMono(key: Any, retriver: () -> Mono<*>, returnType: Class<T>): Mono<*> {
        return CacheMono.lookup(
                reader(), key as String
        ).onCacheMissResume(
                Mono.defer{
                    print("cache missed redis\n")
                    retriver.invoke()
                }
        ).andWriteWith(writer())
    }

    private fun reader() = {key:String ->
        print("fuck")
        print(key)
        val result = reactiveRedisTemplate.opsForList().range(key, 0, -1).collectList()
        result.flatMap {
            if(it.isEmpty()) {
                Mono.empty()
            }
            else {
                Mono.just(it)
            }
        }.map<Signal<*>> {
            Signal.next(it)
        }
    }

    private fun writer(): BiFunction<String, Signal<*>, Mono<Void>> = BiFunction { key, signal ->
        val result = signal.get() as List<TestCache>
        print(result)
        print("fuck\n")
        Mono.fromRunnable {
            if(!signal.isOnError) {
                print("yes\n")
                try {
                    reactiveRedisTemplate.opsForList().leftPushAll(key, result)
                } catch (ex: Exception) {
                    print(ex)
                }

            }
        }
    }
}