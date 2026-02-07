package com.petpro.domain.coupon.entity;

/**
 * 쿠폰 발급 유형
 */
public enum IssueType {
    /** 관리자 수동 발급 */
    MANUAL,
    /** 쿠폰 코드 입력 */
    CODE,
    /** 이벤트 자동 발급 */
    AUTO
}
