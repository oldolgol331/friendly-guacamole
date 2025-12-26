package com.example.demo.infra.aop;

import static com.example.demo.infra.redis.lock.LockKeyGenerator.generateLockKey;
import static lombok.AccessLevel.PRIVATE;

import com.example.demo.infra.annotation.CustomLock;
import com.example.demo.infra.redis.lock.DistributedLockExecutor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * PackageName : com.example.demo.infra.aop
 * FileName    : AppAspect
 * Author      : oldolgol331
 * Date        : 25. 12. 26.
 * Description : 인프라 AOP 모음
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 26.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Slf4j
public abstract class AppAspect {

    @Aspect
    @RequiredArgsConstructor
    public static class DistributedLockAspect {

        private final DistributedLockExecutor lockExecutor;

        @Around("@annotation(customLock)")
        public Object execute(ProceedingJoinPoint joinPoint, final CustomLock customLock) throws Throwable {
            String lockKey = generateLockKey(joinPoint, customLock.key());
            return lockExecutor.execute(
                    lockKey,
                    customLock.maxWaitTime(),
                    customLock.leaseTime(),
                    customLock.timeUnit(),
                    () -> {
                        try {
                            return joinPoint.proceed();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

    }

}
