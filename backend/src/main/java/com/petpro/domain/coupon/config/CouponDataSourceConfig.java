package com.petpro.domain.coupon.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
 * 쿠폰 데이터베이스 설정
 *
 * 별도의 데이터소스, EntityManagerFactory, TransactionManager 설정
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.petpro.domain.coupon.repository",
        entityManagerFactoryRef = "couponEntityManagerFactory",
        transactionManagerRef = "couponTransactionManager"
)
public class CouponDataSourceConfig {

    @Bean
    @ConfigurationProperties("coupon.datasource")
    public DataSourceProperties couponDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("coupon.datasource.hikari")
    public DataSource couponDataSource() {
        return couponDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean couponEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("couponDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");  // 개발용: 테이블 자동 생성
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        return builder
                .dataSource(dataSource)
                .packages("com.petpro.domain.coupon.entity")
                .persistenceUnit("coupon")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager couponTransactionManager(
            @Qualifier("couponEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
