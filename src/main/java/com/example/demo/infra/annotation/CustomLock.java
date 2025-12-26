package com.example.demo.infra.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * PackageName : com.example.demo.infra.annotation
 * FileName    : CustomLock
 * Author      : oldolgol331
 * Date        : 25. 12. 26.
 * Description : 커스텀 Redisson 락 어노테이션
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 26.   oldolgol331          Initial creation
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface CustomLock {

    String key();

    long maxWaitTime() default 0L;

    long leaseTime() default 5000L;

    TimeUnit timeUnit() default MILLISECONDS;

}
