package com.test.cache.demo.cache1

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import org.springframework.cache.Cache
import org.springframework.cache.caffeine.CaffeineCache
import reactor.cache.CacheMono
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.justOrEmpty
import reactor.core.publisher.Signal
import java.time.Duration
import java.util.function.BiFunction
import javax.annotation.PostConstruct

class ReactiveInmemoryCacheManager (
        private val name: String,
        private val max: Long,
        private val timeout: Long,
        private val ticker: Ticker
        ): ReactiveCacheManager {
    lateinit var cache: CaffeineCache

    @PostConstruct
    fun init() {
        cache = CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(timeout))
                        .maximumSize(max)
                        .ticker(ticker)
                        .build()
        )
    }

    override fun <T> findCachedMono(key: Any, retriver: () -> Mono<*>, returnType: Class<T>): Mono<*> {
        return CacheMono
                .lookup(reader(returnType), key)
                .onCacheMissResume(
                        Mono.defer {
                            print("cache missed\n")
                            retriver.invoke()
                        }
                ).andWriteWith(
                        writer()
                )
    }

    private fun <T> reader(returnType: Class<T>) = { key: Any ->
        val result = cache.get(key!!, returnType)

        print(result)
        Mono.justOrEmpty(result).map<Signal<*>> {

            Signal.next(it) }
    }
    private fun writer(): BiFunction<Any, Signal<*>, Mono<Void>> = BiFunction { key, signal ->
        Mono.fromRunnable {
            if(!signal.isOnError) {
                cache.put(key!!, signal.get())
            }
        }
    }

    override fun isSupport(name: String): Boolean {
        return name == this.name
    }
}