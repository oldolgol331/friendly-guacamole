package com.example.demo.infra.redis.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

/**
 * PackageName : com.example.demo.infra.redis.dao
 * FileName    : RedisRepositoryImpl
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Redis DAO 인터페이스 구현체
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper                  objectMapper;

    //==================================================
    //== Generic / Key Operations
    //==================================================

    @Override
    public boolean hasKey(final String key) {
        return Optional.ofNullable(redisTemplate.hasKey(key)).orElse(false);
    }

    @Override
    public boolean deleteData(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public Long deleteData(final Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public void expire(final String key, final Duration duration) {
        redisTemplate.expire(key, duration);
    }

    @Override
    public void expire(final String key, final long timeout, final TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    @Override
    public Long getExpire(final String key) {
        return redisTemplate.getExpire(key);
    }

    @Override
    public Long getExpire(final String key, final TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    @Override
    public Set<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public Cursor<String> scan(final ScanOptions options) {
        return redisTemplate.scan(options);
    }

    //==================================================
    //== String (Value) Operations
    //==================================================

    @Override
    public void setValue(final String key, final Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setValue(final String key, final Object value, final long offset) {
        redisTemplate.opsForValue().set(key, value, offset);
    }

    @Override
    public void setValue(final String key, final Object value, final Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    @Override
    public void setValue(final String key, final Object value, final long timeout, final TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public boolean setValueIfAbsent(final String key, final Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    }

    @Override
    public boolean setValueIfAbsent(final String key, final Object value, final Duration duration) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, duration));
    }

    @Override
    public boolean setValueIfAbsent(final String key, final Object value, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit));
    }

    @Override
    public boolean setValueIfPresent(final String key, final Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfPresent(key, value));
    }

    @Override
    public boolean setValueIfPresent(final String key, final Object value, final Duration duration) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfPresent(key, value, duration));
    }

    @Override
    public boolean setValueIfPresent(final String key, final Object value, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfPresent(key, value, timeout, unit));
    }

    @Override
    public void multiSetValue(final Map<String, Object> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    @Override
    public boolean multiSetValueIfAbsent(final Map<String, Object> map) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().multiSetIfAbsent(map));
    }

    @Override
    public <T> Optional<T> getValue(final String key, final Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public <T> List<T> multiGetValues(final Collection<String> keys, final Class<T> type) {
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null || values.isEmpty()) return Collections.emptyList();
        return values.stream()
                     .map(value -> value != null ? objectMapper.convertValue(value, type) : null)
                     .toList();
    }

    @Override
    public Long increment(final String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long increment(final String key, final long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Double increment(final String key, final double delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long decrement(final String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    @Override
    public Long decrement(final String key, final long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    //==================================================
    //== Hash Operations
    //==================================================

    @Override
    public void setHash(final String key, final String hashKey, final Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void putAllToHash(final String key, final Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public <T> Optional<T> getHash(final String key, final String hashKey, final Class<T> type) {
        Object value = redisTemplate.opsForHash().get(key, hashKey);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> multiGetHash(final String key, final Collection<String> hashKeys, final Class<T> type) {
        List<Object> values = redisTemplate.opsForHash().multiGet(key, (Collection<Object>) (Collection<?>) hashKeys);
        if (values.isEmpty()) return Collections.emptyList();
        return values.stream()
                     .map(value -> value != null ? objectMapper.convertValue(value, type) : null)
                     .toList();
    }

    @Override
    public Map<Object, Object> getAllHash(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public Long deleteHash(final String key, final Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    @Override
    public Long getHashSize(final String key) {
        return redisTemplate.opsForHash().size(key);
    }

    @Override
    public Long incrementHash(final String key, final String hashKey, final long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    @Override
    public Double incrementHash(final String key, final String hashKey, final double delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    //==================================================
    //== Set Operations
    //==================================================

    @Override
    public Long addToSet(final String key, final Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Set<Object> getSetMembers(final String key) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return members != null ? members : Collections.emptySet();
    }

    @Override
    public Long removeFromSet(final String key, final Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public Long getSetSize(final String key) {
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public boolean isSetMember(final String key, final Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    @Override
    public Map<Object, Boolean> isSetMember(final String key, final Object... values) {
        Map<Object, Boolean> map = redisTemplate.opsForSet().isMember(key, values);
        if (map == null || map.isEmpty()) return Collections.emptyMap();
        return map;
    }

    //==================================================
    //== List Operations
    //==================================================

    @Override
    public Long leftPushToList(final String key, final Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    public Long leftPushToList(final String key, final Object pivot, final Object value) {
        return redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    @Override
    public Long leftPushIfPresentToList(final String key, final Object value) {
        return redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    @Override
    public Long leftPushAllToList(final String key, final Object... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    @Override
    public Long leftPushAllToList(final String key, final Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    @Override
    public Long rightPushToList(final String key, final Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Long rightPushToList(final String key, final Object pivot, final Object value) {
        return redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    @Override
    public Long rightPushIfPresentToList(final String key, final Object value) {
        return redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    @Override
    public Long rightPushAllToList(final String key, final Object... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public Long rightPushAllToList(final String key, final Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public <T> Optional<T> leftPopFromList(final String key, final Class<T> type) {
        Object value = redisTemplate.opsForList().leftPop(key);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public <T> List<T> leftPopFromList(final String key, final long count, final Class<T> type) {
        List<Object> values = redisTemplate.opsForList().leftPop(key, count);
        if (values == null || values.isEmpty()) return Collections.emptyList();
        return values.stream()
                     .map(value -> objectMapper.convertValue(value, type))
                     .toList();
    }

    @Override
    public <T> Optional<T> leftPopFromList(final String key, final Duration timeout, final Class<T> type) {
        Object value = redisTemplate.opsForList().leftPop(key, timeout);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public <T> Optional<T> leftPopFromList(
            final String key, final long timeout, final TimeUnit unit, final Class<T> type
    ) {
        Object value = redisTemplate.opsForList().leftPop(key, timeout, unit);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public <T> Optional<T> rightPopFromList(final String key, final Class<T> type) {
        Object value = redisTemplate.opsForList().rightPop(key);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public <T> List<T> rightPopFromList(final String key, final long count, final Class<T> type) {
        List<Object> values = redisTemplate.opsForList().rightPop(key, count);
        if (values == null || values.isEmpty()) return Collections.emptyList();
        return values.stream()
                     .map(value -> objectMapper.convertValue(value, type))
                     .toList();
    }

    @Override
    public <T> Optional<T> rightPopFromList(final String key, final Duration timeout, final Class<T> type) {
        Object value = redisTemplate.opsForList().rightPop(key, timeout);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public <T> Optional<T> rightPopFromList(final String key,
                                            final long timeout,
                                            final TimeUnit unit,
                                            final Class<T> type) {
        Object value = redisTemplate.opsForList().rightPop(key, timeout, unit);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public List<Object> getListRange(final String key, final long start, final long end) {
        List<Object> list = redisTemplate.opsForList().range(key, start, end);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list;
    }

    @Override
    public Long getListSize(final String key) {
        return redisTemplate.opsForList().size(key);
    }

    //==================================================
    //== Sorted Set (ZSet) Operations
    //==================================================

    @Override
    public boolean addToZSet(final String key, final Object value, final double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
    }

    @Override
    public Long addToZSet(final String key, final Set<TypedTuple<Object>> tuples) {
        return redisTemplate.opsForZSet().add(key, tuples);
    }

    @Override
    public boolean addToZSetIfAbsent(final String key, final Object value, final double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().addIfAbsent(key, value, score));
    }

    @Override
    public Long addToZSetIfAbsent(final String key, final Set<TypedTuple<Object>> tuples) {
        return redisTemplate.opsForZSet().addIfAbsent(key, tuples);
    }

    @Override
    public Double incrementZSetScore(final String key, final Object value, final double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    @Override
    public Long removeFromZSet(final String key, final Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    @Override
    public Long removeRangeFromZSet(final String key, final long start, final long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    @Override
    public Long getZSetRank(final String key, final Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    @Override
    public Long getZSetReverseRank(final String key, final Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    @Override
    public Set<Object> getZSetRange(final String key, final long start, final long end) {
        Set<Object> set = redisTemplate.opsForZSet().range(key, start, end);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Set<Object> getZSetReverseRange(final String key, final long start, final long end) {
        Set<Object> set = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Set<TypedTuple<Object>> getZSetRangeWithScores(final String key, final long start, final long end) {
        Set<TypedTuple<Object>> set = redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Set<TypedTuple<Object>> getZSetReverseRangeWithScores(final String key, final long start, final long end) {
        Set<TypedTuple<Object>> set = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Set<Object> getZSetRangeByScore(final String key, final double min, final double max) {
        Set<Object> set = redisTemplate.opsForZSet().rangeByScore(key, min, max);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Set<Object> getZSetReverseRangeByScore(final String key, final double min, final double max) {
        Set<Object> set = redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Set<Object> getZSetRangeByScore(final String key,
                                           final double min,
                                           final double max,
                                           final long offset,
                                           final long count) {
        Set<Object> set = redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Set<Object> getZSetReverseRangeByScore(final String key,
                                                  final double min,
                                                  final double max,
                                                  final long offset,
                                                  final long count) {
        Set<Object> set = redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
        if (set == null || set.isEmpty()) return Collections.emptySet();
        return set;
    }

    @Override
    public Long getZSetSize(final String key) {
        return redisTemplate.opsForZSet().size(key);
    }

}
