package dev.fastcampus.webfluxcoroutine.config

import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger {}

@Component
class Locker(
    template: ReactiveRedisTemplate<Any, Any>
) {
    // 요청이 같은 인스턴스에 있을 경우에는 굳이 레디스를 통해 확인할 필요 없이 localLock을 통해서 확인 가능
    // ConcurrentHashMap로 구현하여 thread-safe하게 구현
    // spin-lock 방지
    private val localLock = ConcurrentHashMap<SimpleKey, Boolean>()

    private val ops = template.opsForValue()

    suspend fun <T> lock(key: SimpleKey, work: suspend () -> T): T {
        if(! tryLock(key))
            throw TimeoutException("fail to obtain lock ($key)")
        try {
            return work.invoke()
        } finally {
            unlock(key)
        }
    }



    private suspend fun tryLock(key: SimpleKey): Boolean {
        val start = System.nanoTime()

        // redisson을 사용해 spin lock에서 pub/sub 형태의 lock으로 바꿔 레디스에 부하를 줄일 수 있다.
        // ops.tryLock()
        while(
            ! localLock.contains(key) &&
            ! ops.setIfAbsent(key, true, 10.seconds.toJavaDuration()).awaitSingle()
        ) {
            logger.debug { "- spring lock : $key" }
            delay(100)
            val elapsed = (System.nanoTime() - start).nanoseconds
            if(elapsed >= 10.seconds) {
                return false
            }
        }
        localLock[key] = true
        return true
    }

    private suspend fun unlock(key: SimpleKey) {
        try {
            ops.delete(key).awaitSingle()
        } catch (e: Exception) {
            logger.warn(e.message, e)
        } finally {
            localLock.remove(key)
        }
    }

}