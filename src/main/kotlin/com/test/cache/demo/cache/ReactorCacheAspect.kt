package com.test.cache.demo.cache

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.ResolvableType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType

@Aspect
@Component
class ReactorCacheAspect(private val reactiveCacheManager: ReactiveCacheManager) {

    @Pointcut("@annotation(com.test.cache.demo.cache.ReactorMonoCacheable)")
    fun cachePointcut() {
    }

    @Around("cachePointcut()")
    fun cacheAround(joinPoint: ProceedingJoinPoint): Any {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val parameterizedType = method.genericReturnType as ParameterizedType
        val rawType = parameterizedType.rawType

        if(!rawType.equals(Mono::class.java)) {
            throw(IllegalArgumentException("The return type is not Mono"))
        }
        val reactorCacheable = method.getAnnotation(ReactorMonoCacheable::class.java)
        val cacheName = reactorCacheable.name
        val args = joinPoint.args
        print(args)
        val retriever = {
            try{
                joinPoint.proceed(args) as Mono<*>
            } catch (th: Throwable) {
                throw RuntimeException(th)
            }
        }

        val returnTypeInsideMono = parameterizedType.actualTypeArguments[0]
        val returnClass = ResolvableType.forType(returnTypeInsideMono).resolve() as Class<*>

        return reactiveCacheManager
                .findCachedMono(cacheName, generateKey(*args),retriever, returnClass)
                .doOnError { e ->
                    print(e)
                }
    }

    private fun generateKey(vararg objects: Any): String {
        print(objects)
        return objects.map{ obj -> obj.toString()}.joinToString { ":" }
    }
}