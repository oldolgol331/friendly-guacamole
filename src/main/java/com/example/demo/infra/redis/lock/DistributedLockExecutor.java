package com.example.demo.infra.redis.lock;

import static com.example.demo.common.response.ErrorCode.LOCK_ACQUISITION_FAILED;
import static com.example.demo.common.response.ErrorCode.LOCK_THREAD_INTERRUPTED;

import com.example.demo.common.error.BusinessException;
import jakarta.validation.constraints.Min;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PackageName : com.example.demo.infra.redis.lock
 * FileName    : DistributedLockExecutor
 * Author      : oldolgol331
 * Date        : 25. 12. 26.
 * Description : 락 관련 컴포넌트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 26.   oldolgol331          Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockExecutor {

    private final RedissonClient redissonClient;

    public <T> T execute(final String lockKey,
                         @Min(0) final long waitTime,
                         @Min(0) final long leaseTime,
                         final TimeUnit timeUnit,
                         final Supplier<T> callback) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean available = lock.tryLock(waitTime, leaseTime, timeUnit);

            if (!available) {
                log.warn("락 획득 실패: {}", lockKey);
                throw new BusinessException(LOCK_ACQUISITION_FAILED);
            }

            return callback.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(LOCK_THREAD_INTERRUPTED);
        } finally {
            unlock(lock);
        }
    }

    public void execute(final String lockKey,
                        @Min(0) final long waitTime,
                        @Min(0) final long leaseTime,
                        final TimeUnit timeUnit,
                        final Runnable callback) {
        execute(lockKey, waitTime, leaseTime, timeUnit, () -> {
            callback.run();
            return null;
        });
    }

    // ========================= 내부 메서드 =========================

    private void unlock(final RLock lock) {
        if (TransactionSynchronizationManager.isActualTransactionActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    safeUnlock(lock);
                }
            });
        else safeUnlock(lock);
    }

    private void safeUnlock(final RLock lock) {
        try {
            if (lock.isLocked() && lock.isHeldByCurrentThread())
                lock.unlock();
        } catch (IllegalMonitorStateException e) {
            log.warn("이미 락이 해제되었거나, 현재 스레드가 점유하지 않음: {}", lock.getName());
        } catch (Exception e) {
            log.error("락 해제 중 오류 발생: {}", lock.getName(), e);
        }
    }

}
