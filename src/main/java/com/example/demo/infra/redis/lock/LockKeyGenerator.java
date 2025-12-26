package com.example.demo.infra.redis.lock;

import static lombok.AccessLevel.PRIVATE;

import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * PackageName : com.example.demo.infra.redis.lock
 * FileName    : LockKeyGenerator
 * Author      : oldolgol331
 * Date        : 25. 12. 26.
 * Description : 락 키 생성기
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 26.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class LockKeyGenerator {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static String generateLockKey(final ProceedingJoinPoint joinPoint, @NotNull final String spelExpression) {
        MethodSignature signature      = (MethodSignature) joinPoint.getSignature();
        Object[]        args           = joinPoint.getArgs();
        String[]        parameterNames = signature.getParameterNames();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) context.setVariable(parameterNames[i], args[i]);

        Object value = PARSER.parseExpression(spelExpression).getValue(context);

        if (value == null) throw new IllegalArgumentException("Lock key cannot be null. Expression: " + spelExpression);

        return "%s:%s".formatted(signature.getMethod().getName(), convertToKey(value));
    }

    // ========================= 내부 메서드 =========================

    private static String convertToKey(final Object value) {
        if (value == null)
            return null;
        if (value instanceof String)
            return (String) value;
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>)
            return String.valueOf(value);
        if (value instanceof LocalDateTime)
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format((LocalDateTime) value);
        if (value instanceof Date)
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
        if (value.getClass().isAnnotation())
            return arrayToString(value);
        if (value instanceof Collection<?>)
            return collectionToString((Collection<?>) value);
        if (value instanceof Map<?, ?>)
            return mapToString((Map<?, ?>) value);
        else
            return value.toString();
    }

    private static String arrayToString(final Object array) {
        return IntStream.range(0, Array.getLength(array))
                        .mapToObj(i -> convertToKey(Array.get(array, i)))
                        .collect(Collectors.joining(","));
    }

    private static String collectionToString(final Collection<?> collection) {
        return collection.stream()
                         .map(LockKeyGenerator::convertToKey)
                         .collect(Collectors.joining(","));
    }

    private static String mapToString(final Map<?, ?> map) {
        return map.entrySet()
                  .stream()
                  .map(e -> convertToKey(e.getKey() + "=" + e.getValue()))
                  .collect(Collectors.joining(","));
    }

}
