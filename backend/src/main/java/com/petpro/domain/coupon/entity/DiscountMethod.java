package com.petpro.domain.coupon.entity;

/**
 * 할인 방식
 */
public enum DiscountMethod {
    /** 정액 할인 (discountValue = 금액) */
    FIXED,
    /** 정률 할인 (discountValue = 퍼센트) */
    PERCENT,
    /** 무료 (배송비 등) */
    FREE
}
