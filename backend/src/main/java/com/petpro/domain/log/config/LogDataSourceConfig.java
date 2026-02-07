package com.petpro.domain.log.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그 데이터베이스 DataSource 설정
 *
 * MySQL Master-Slave 라우팅을 구현합니다.
 * - Master: 로그 쓰기 (CUD)
 * - Slave: 통계 조회 (Read Only)
 */
@Configuration
public class LogDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "log.datasource.master")
    public DataSource logMasterDataSource() {
        return new HikariDataSource();
    }

    @Bean
    @ConfigurationProperties(prefix = "log.datasource.slave")
    public DataSource logSlaveDataSource() {
        return new HikariDataSource();
    }

    @Bean
    public DataSource logRoutingDataSource(
            @Qualifier("logMasterDataSource") DataSource master,
            @Qualifier("logSlaveDataSource") DataSource slave) {

        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                        ? "SLAVE" : "MASTER";
            }
        };

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("MASTER", master);
        dataSourceMap.put("SLAVE", slave);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(master);

        return routingDataSource;
    }

    @Bean
    public DataSource logDataSource(
            @Qualifier("logRoutingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
