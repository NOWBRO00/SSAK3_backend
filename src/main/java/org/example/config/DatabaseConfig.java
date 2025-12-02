package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Slf4j
@Configuration
@Profile("production")
public class DatabaseConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;
    private final DataSource dataSource;

    public DatabaseConfig(Environment environment, DataSource dataSource) {
        this.environment = environment;
        this.dataSource = dataSource;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // 데이터베이스 연결 테스트
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                String databaseUrl = environment.getProperty("spring.datasource.url", "");
                String databaseProductName = metaData.getDatabaseProductName();
                String databaseVersion = metaData.getDatabaseProductVersion();
                
                log.info("========================================");
                log.info("✅ 데이터베이스 연결 성공!");
                log.info("데이터베이스 타입: {}", databaseProductName);
                log.info("데이터베이스 버전: {}", databaseVersion);
                log.info("연결 URL: {}", databaseUrl.replaceAll("password=[^&;]*", "password=***"));
                log.info("========================================");
            }
        } catch (Exception e) {
            log.error("❌ 데이터베이스 연결 실패!", e);
            log.error("DATABASE_URL: {}", environment.getProperty("DATABASE_URL", "설정되지 않음"));
            log.error("spring.datasource.url: {}", environment.getProperty("spring.datasource.url", "설정되지 않음"));
        }
    }
}

