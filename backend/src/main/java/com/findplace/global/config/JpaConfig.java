package com.findplace.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
 * - com.findplace.domain 패키지 하위의 모든 Repository 인터페이스
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.findplace.domain")
public class JpaConfig {
}
