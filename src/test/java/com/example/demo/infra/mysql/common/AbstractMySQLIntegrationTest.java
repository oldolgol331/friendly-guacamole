package com.example.demo.infra.mysql.common;

import com.example.demo.infra.redis.config.TestRedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * PackageName : com.example.demo.infra.mysql.common
 * FileName    : AbstractMySQLIntegrationTest
 * Author      : oldolgol331
 * Date        : 26. 1. 2.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 1. 2.     oldolgol331          Initial creation
 */
@DisplayNameGeneration(ReplaceUnderscores.class)
@Testcontainers
@SpringBootTest
@Import(TestRedisConfig.class)
@Slf4j
public abstract class AbstractMySQLIntegrationTest {

    private static final String MYSQL_IMAGE = "groonga/mroonga:mysql-8.0.44-15.21";
    private static final int MYSQL_PORT = 3306;

    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(
            DockerImageName.parse(MYSQL_IMAGE).asCompatibleSubstituteFor("mysql")
    )
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
            .withUrlParam("useUnicode", "true")
            .withUrlParam("characterEncoding", "UTF-8")
            .withUrlParam("serverTimezone", "Asia/Seoul")
            .withUrlParam("useSSL", "false")
            .withUrlParam("allowPublicKeyRetrieval", "true")
            .withExposedPorts(MYSQL_PORT)
            .withNetworkAliases("mysql")
            //.withInitScript("schema.sql")
            .withReuse(true);

    static {
        MYSQL_CONTAINER.withLogConsumer(new Slf4jLogConsumer(log).withPrefix("MYSQL"));
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    private static void setProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);

        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");

        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
    }

}
