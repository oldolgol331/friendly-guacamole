package com.example.demo.common.response.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * PackageName : com.example.demo.common.response.annotation
 * FileName    : CustomPageResponse
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 커스텀 페이징 응답
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface CustomPageResponse {

    boolean content() default true;

    boolean totalElements() default true;

    boolean totalPages() default true;

    boolean size() default true;

    boolean number() default true;

    boolean numberOfElements() default true;

    boolean sort() default true;

    boolean empty() default true;

    boolean hasContent() default true;

    boolean first() default true;

    boolean last() default true;

    boolean hasPrevious() default true;

    boolean hasNext() default true;

}
