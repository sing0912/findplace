package com.findplace.domain.coupon.entity;

/**
 * 자동 발급 이벤트 유형
 */
public enum AutoIssueEvent {
    /** 회원가입 */
    SIGNUP,
    /** 첫 주문 */
    FIRST_ORDER,
    /** 생일 */
    BIRTHDAY,
    /** 휴면 해제 */
    DORMANT_RETURN,
    /** 리뷰 작성 */
    REVIEW_WRITE
}
