package com.petpro.domain.coupon.entity;

/**
 * 조건 연산자
 */
public enum ConditionOperator {
    /** 같음 */
    EQ,
    /** 이상 */
    GTE,
    /** 이하 */
    LTE,
    /** 초과 */
    GT,
    /** 미만 */
    LT,
    /** 포함 */
    IN,
    /** 미포함 */
    NOT_IN,
    /** 범위 */
    BETWEEN,
    /** 포함(문자열) */
    LIKE
}
