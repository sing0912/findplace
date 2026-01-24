package com.findplace.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 역할 열거형
 *
 * 시스템 내 사용자 권한을 정의하는 열거형
 * Spring Security의 권한(Authority)과 매핑됨
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    /** 일반 사용자 - 기본 서비스 이용 */
    USER("ROLE_USER", "일반 사용자"),

    /** 업체 관리자 - 장례 업체 관리 권한 */
    COMPANY_ADMIN("ROLE_COMPANY_ADMIN", "업체 관리자"),

    /** 공급사 관리자 - 상품 공급사 관리 권한 */
    SUPPLIER_ADMIN("ROLE_SUPPLIER_ADMIN", "공급사 관리자"),

    /** 관리자 - 시스템 관리 권한 */
    ADMIN("ROLE_ADMIN", "관리자"),

    /** 최고 관리자 - 모든 권한 보유 */
    SUPER_ADMIN("ROLE_SUPER_ADMIN", "최고 관리자");

    /** Spring Security 권한 문자열 */
    private final String authority;

    /** 역할 설명 */
    private final String description;
}
