package com.example.demo;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.example.demo
 * FileName    : VisitController
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */

@RestController
@RequiredArgsConstructor
public class VisitController {

    private final RedisTemplate redisTemplate;
    private final DataSource    dataSource;

    @GetMapping("/test/visit")
    public String checkVisit(){
        Long count = redisTemplate.opsForValue().increment("visitorCount");

        String dbStatus = "MySQL 연결 실패";
        try(Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) dbStatus = "MySQL 연결 성공 (OK)";
        } catch (SQLException e) {
            dbStatus = "MySQL 에러: " + e.getMessage();
        }

        return String.format("방문자 수(Redis): %d명 | DB 상태: %s", count, dbStatus);
    }

}
