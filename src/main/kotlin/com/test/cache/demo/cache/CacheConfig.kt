package com.test.cache.demo.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
@ConstructorBinding
@ConfigurationProperties(value = "caching")
class CacheConfig {

    class CacheSpec(var timeout: Long, var max: Long = 200)

    lateinit var specs: MutableMap<String, CacheSpec>
    @Bean
    fun cacheManager(ticker: Ticker): CacheManager {
        val simpleCacheManager = SimpleCacheManager()
        val caches = specs.entries.map { entry -> buildCache(entry.key, entry.value, ticker) }
        simpleCacheManager.setCaches(caches)
        return simpleCacheManager
    }

    @Bean
    fun ticker(): Ticker {
        return Ticker.systemTicker()
    }

    private fun buildCache(name: String, cacheSpec: CacheSpec, ticker: Ticker): CaffeineCache {
        return CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(cacheSpec.timeout))
                        .maximumSize(cacheSpec.max)
                        .ticker(ticker)
                        .build()
        )
    }
}