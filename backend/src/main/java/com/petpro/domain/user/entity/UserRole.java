package com.petpro.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 역할 열거형
 *
 * PetPro 플랫폼 사용자 권한을 정의하는 열거형
 * Spring Security의 권한(Authority)과 매핑됨
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    /** 반려인 - 시터 검색, 예약, 결제, 돌봄 조회, 채팅, 커뮤니티 */
    CUSTOMER("ROLE_CUSTOMER", "반려인"),

    /** 펫시터 - 프로필/자격 관리, 예약 수락/거절, 돌봄 일지, 정산 */
    PARTNER("ROLE_PARTNER", "펫시터"),

    /** 관리자 - 일반 관리 기능 */
    ADMIN("ROLE_ADMIN", "관리자"),

    /** 최고 관리자 - 모든 권한 보유 */
    SUPER_ADMIN("ROLE_SUPER_ADMIN", "최고 관리자");

    /** Spring Security 권한 문자열 */
    private final String authority;

    /** 역할 설명 */
    private final String description;
}
