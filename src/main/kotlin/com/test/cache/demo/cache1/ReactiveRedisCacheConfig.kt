package com.test.cache.demo.cache1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.benmanes.caffeine.cache.Ticker
import com.test.cache.demo.service.TestCache
import kotlinx.coroutines.channels.ticker
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class ReactiveRedisCacheConfig(
        @Value("\${spring.redis.host}") private val redisHost: String,
        @Value("\${spring.redis.port}") private val redisPort: Int,
        private val ticker: Ticker
) {

    @Bean
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory(redisHost, redisPort)
    }

    @Bean
    fun reactiveRedisTemplate() : ReactiveRedisTemplate<String, TestCache> {
        val keyJsonSerializer = StringRedisSerializer()
        val jsonSerializer = Jackson2JsonRedisSerializer<TestCache>(TestCache::class.java)
        jsonSerializer.setObjectMapper(ObjectMapper().registerModule(KotlinModule()))

        val context = RedisSerializationContext
                .newSerializationContext<String, TestCache>()
                .key(keyJsonSerializer)
                .value(jsonSerializer)
                .hashKey(keyJsonSerializer)
                .hashValue(jsonSerializer)
                .build()
        return ReactiveRedisTemplate<String, TestCache>(reactiveRedisConnectionFactory(), context)
    }

    @Bean
    fun testRedis() = ReactiveRedisCacheManager(reactiveRedisTemplate(), ticker, "redis")
}