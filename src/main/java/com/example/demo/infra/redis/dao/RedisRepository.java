package com.example.demo.infra.redis.dao;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

/**
 * PackageName : com.example.demo.infra.redis.dao
 * FileName    : RedisRepository
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Redis DAO 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public interface RedisRepository {

    //==================================================
    //== Generic / Key Operations
    //==================================================

    boolean hasKey(String key);

    boolean deleteData(String key);

    Long deleteData(Collection<String> keys);

    void expire(String key, Duration duration);

    void expire(String key, long timeout, TimeUnit unit);

    Long getExpire(String key);

    Long getExpire(String key, TimeUnit timeUnit);

    Set<String> keys(String pattern);

    Cursor<String> scan(ScanOptions options);

    //==================================================
    //== String (Value) Operations
    //==================================================

    void setValue(String key, Object value);

    void setValue(String key, Object value, long offset);

    void setValue(String key, Object value, Duration duration);

    void setValue(String key, Object value, long timeout, TimeUnit unit);

    boolean setValueIfAbsent(String key, Object value);

    boolean setValueIfAbsent(String key, Object value, Duration duration);

    boolean setValueIfAbsent(String key, Object value, long timeout, TimeUnit unit);

    boolean setValueIfPresent(String key, Object value);

    boolean setValueIfPresent(String key, Object value, Duration duration);

    boolean setValueIfPresent(String key, Object value, long timeout, TimeUnit unit);

    void multiSetValue(Map<String, Object> map);

    boolean multiSetValueIfAbsent(Map<String, Object> map);

    <T> Optional<T> getValue(String key, Class<T> type);

    <T> List<T> multiGetValues(Collection<String> keys, Class<T> type);

    Long increment(String key);

    Long increment(String key, long delta);

    Double increment(String key, double delta);

    Long decrement(String key);

    Long decrement(String key, long delta);

    //==================================================
    //== Hash Operations
    //==================================================

    void setHash(String key, String hashKey, Object value);

    void putAllToHash(String key, Map<String, Object> map);

    <T> Optional<T> getHash(String key, String hashKey, Class<T> type);

    <T> List<T> multiGetHash(String key, Collection<String> hashKeys, Class<T> type);

    Map<Object, Object> getAllHash(String key);

    Long deleteHash(String key, Object... hashKeys);

    Long getHashSize(String key);

    Long incrementHash(String key, String hashKey, long delta);

    Double incrementHash(String key, String hashKey, double delta);

    //==================================================
    //== Set Operations
    //==================================================

    Long addToSet(String key, Object... values);

    Set<Object> getSetMembers(String key);

    Long removeFromSet(String key, Object... values);

    Long getSetSize(String key);

    boolean isSetMember(String key, Object value);

    Map<Object, Boolean> isSetMember(String key, Object... values);

    //==================================================
    //== List Operations
    //==================================================

    Long leftPushToList(String key, Object value);

    Long leftPushToList(String key, Object pivot, Object value);

    Long leftPushIfPresentToList(String key, Object value);

    Long leftPushAllToList(String key, Object... values);

    Long leftPushAllToList(String key, Collection<Object> values);

    Long rightPushToList(String key, Object value);

    Long rightPushToList(String key, Object pivot, Object value);

    Long rightPushIfPresentToList(String key, Object value);

    Long rightPushAllToList(String key, Object... values);

    Long rightPushAllToList(String key, Collection<Object> values);

    <T> Optional<T> leftPopFromList(String key, Class<T> type);

    <T> List<T> leftPopFromList(String key, long count, Class<T> type);

    <T> Optional<T> leftPopFromList(String key, Duration timeout, Class<T> type);

    <T> Optional<T> leftPopFromList(String key, long timeout, TimeUnit unit, Class<T> type);

    <T> Optional<T> rightPopFromList(String key, Class<T> type);

    <T> List<T> rightPopFromList(String key, long count, Class<T> type);

    <T> Optional<T> rightPopFromList(String key, Duration timeout, Class<T> type);

    <T> Optional<T> rightPopFromList(String key, long timeout, TimeUnit unit, Class<T> type);

    List<Object> getListRange(String key, long start, long end);

    Long getListSize(String key);

    //==================================================
    //== Sorted Set (ZSet) Operations
    //==================================================

    boolean addToZSet(String key, Object value, double score);

    Long addToZSet(String key, Set<TypedTuple<Object>> tuples);

    boolean addToZSetIfAbsent(String key, Object value, double score);

    Long addToZSetIfAbsent(String key, Set<TypedTuple<Object>> tuples);

    Double incrementZSetScore(String key, Object value, double delta);

    Long removeFromZSet(String key, Object... values);

    Long removeRangeFromZSet(String key, long start, long end);

    Long getZSetRank(String key, Object value);

    Long getZSetReverseRank(String key, Object value);

    Set<Object> getZSetRange(String key, long start, long end);

    Set<Object> getZSetReverseRange(String key, long start, long end);

    Set<TypedTuple<Object>> getZSetRangeWithScores(String key, long start, long end);

    Set<TypedTuple<Object>> getZSetReverseRangeWithScores(String key, long start, long end);

    Set<Object> getZSetRangeByScore(String key, double min, double max);

    Set<Object> getZSetReverseRangeByScore(String key, double min, double max);

    Set<Object> getZSetRangeByScore(String key, double min, double max, long offset, long count);

    Set<Object> getZSetReverseRangeByScore(String key, double min, double max, long offset, long count);

    Long getZSetSize(String key);

}
