package com.petpro.domain.log.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그 데이터베이스 JPA 설정
 *
 * EntityManagerFactory + TransactionManager 구성
 * persistenceUnit: "log", dialect: MySQLDialect
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.petpro.domain.log.repository",
        entityManagerFactoryRef = "logEntityManagerFactory",
        transactionManagerRef = "logTransactionManager"
)
public class LogJpaConfig {

    @Value("${log.jpa.hibernate.dialect:org.hibernate.dialect.MySQLDialect}")
    private String dialect;

    @Value("${log.jpa.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Bean
    public LocalContainerEntityManagerFactoryBean logEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("logDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        return builder
                .dataSource(dataSource)
                .packages("com.petpro.domain.log.entity")
                .persistenceUnit("log")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager logTransactionManager(
            @Qualifier("logEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
