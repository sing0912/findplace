package com.petpro.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * JpaConfig
 *
 * JPA 및 트랜잭션 관련 설정을 담당하는 구성 클래스입니다.
 *
 * 활성화된 기능:
 * - @EnableTransactionManagement: 어노테이션 기반 트랜잭션 관리 활성화
 * - @EnableJpaRepositories: JPA Repository 자동 스캔 및 등록
 *
 * Repository 스캔 범위:
 * - com.petpro.domain 패키지 하위의 Repository 인터페이스
 * - 단, coupon 패키지는 별도 DataSource를 사용하므로 제외 (CouponDataSourceConfig에서 관리)
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.petpro.domain.user.repository",
                "com.petpro.domain.auth.repository",
                "com.petpro.domain.inquiry.repository",
                "com.petpro.domain.admin.repository",
                "com.petpro.domain.funeralhome.repository",
                "com.petpro.domain.region.repository",
                "com.petpro.domain.pet.repository",
                "com.petpro.domain.batch.repository"
        },
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class JpaConfig {

    /**
     * 메인 EntityManagerFactory 생성
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");  // 개발용: 테이블/컬럼 자동 생성
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        return builder
                .dataSource(dataSource)
                .packages(
                        "com.petpro.domain.user.entity",
                        "com.petpro.domain.auth.entity",
                        "com.petpro.domain.inquiry.entity",
                        "com.petpro.domain.admin.entity",
                        "com.petpro.domain.funeralhome.entity",
                        "com.petpro.domain.region.entity",
                        "com.petpro.domain.pet.entity",
                        "com.petpro.domain.batch.entity"
                )
                .persistenceUnit("main")
                .properties(properties)
                .build();
    }

    /**
     * 메인 TransactionManager 생성
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
