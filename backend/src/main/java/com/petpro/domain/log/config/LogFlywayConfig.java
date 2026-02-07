package com.petpro.domain.log.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 로그 데이터베이스 전용 Flyway 설정
 *
 * MySQL Master에 직접 DDL 마이그레이션을 실행합니다.
 * 기존 PostgreSQL Flyway와 충돌 방지를 위해 별도 테이블명을 사용합니다.
 */
@Configuration
public class LogFlywayConfig {

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(name = "log.flyway.enabled", havingValue = "true", matchIfMissing = true)
    public Flyway logFlyway(@Qualifier("logMasterDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/log-migration")
                .table("flyway_schema_history_log")
                .baselineOnMigrate(true)
                .load();
    }
}
