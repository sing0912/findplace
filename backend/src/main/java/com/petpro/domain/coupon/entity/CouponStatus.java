package com.petpro.domain.coupon.entity;

/**
 * 사용자 쿠폰 상태
 */
public enum CouponStatus {
    /** 사용 가능 */
    AVAILABLE,
    /** 사용 완료 */
    USED,
    /** 만료됨 */
    EXPIRED,
    /** 회수됨 */
    REVOKED
}
