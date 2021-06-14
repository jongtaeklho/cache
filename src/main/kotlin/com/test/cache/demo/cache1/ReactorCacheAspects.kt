package com.test.cache.demo.cache1

import com.test.cache.demo.cache.ReactorMonoCacheable
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
class ReactorCacheAspects(private val cacheManagers: List<ReactiveCacheManager>) {

    @Pointcut("@annotation(com.test.cache.demo.cache1.MonoCacheable)")
    fun cachePointcut() {
    }

    @Around("cachePointcut()")
    fun cacheAround(joinPoint: ProceedingJoinPoint): Any {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val parameterizedType = method.genericReturnType as ParameterizedType
        val rawType = parameterizedType.rawType

        if(rawType != Mono::class.java) {
            throw(IllegalArgumentException("The return type is not Mono\n"))
        }
        val reactorCacheable = method.getAnnotation(MonoCacheable::class.java)
        val cacheName = reactorCacheable.name
        val args = joinPoint.args
        print(args.size)
        print(args[0])
        val retriever = {
            try{
                joinPoint.proceed(args) as Mono<*>
            } catch (th: Throwable) {
                throw RuntimeException(th)
            }
        }

        val returnTypeInsideMono = parameterizedType.actualTypeArguments[0]
        val returnClass = ResolvableType.forType(returnTypeInsideMono).resolve() as Class<*>
        return cacheManagers.findLast { it.isSupport(cacheName) }?.findCachedMono(args[0], retriever, returnClass)!!
    }
}