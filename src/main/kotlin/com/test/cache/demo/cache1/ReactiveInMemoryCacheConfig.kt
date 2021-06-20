package com.test.cache.demo.cache1

import com.github.benmanes.caffeine.cache.Ticker
import com.test.cache.demo.service.TestCache
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.annotation.Documented

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Documented
annotation class MonoCacheable(val name: String)

@Configuration
@EnableCaching
class ReactiveInMemoryCacheConfig {

    @Bean
    fun tickere(): Ticker {
        return Ticker.systemTicker()
    }

    @Bean
    fun testInMemoryCache() = ReactiveInmemoryCacheManager (
            "test1",4, 3, tickere()
    )


}