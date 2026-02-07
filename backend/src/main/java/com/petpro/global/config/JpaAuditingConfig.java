package com.petpro.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 *
 * 엔티티의 생성일시(createdAt), 수정일시(updatedAt) 자동 관리를 활성화합니다.
 * 메인 애플리케이션에서 분리하여 @WebMvcTest 시 JPA 관련 오류 방지
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
