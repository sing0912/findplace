package com.petpro.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * AuditingConfig
 *
 * JPA Auditing을 위한 감사자(Auditor) 정보 제공 설정 클래스입니다.
 * 엔티티의 생성자/수정자 정보를 자동으로 추적하기 위해 사용됩니다.
 *
 * 동작 방식:
 * - Spring Security의 SecurityContext에서 현재 인증된 사용자 정보를 조회
 * - 인증된 사용자의 ID(Long)를 @CreatedBy, @LastModifiedBy 필드에 자동 설정
 * - 미인증 상태거나 익명 사용자인 경우 empty 반환
 */
@Configuration
public class AuditingConfig {

    /**
     * 감사자 정보 제공자를 생성합니다.
     * 현재 인증된 사용자의 ID를 반환하여 엔티티 변경 이력을 추적합니다.
     *
     * @return AuditorAware 인스턴스 (사용자 ID를 Long 타입으로 반환)
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }

            try {
                return Optional.of(Long.parseLong(authentication.getName()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        };
    }
}
