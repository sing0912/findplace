package com.petpro.domain.log.entity;

/**
 * 운영자 행위 유형
 */
public enum AdminActionType {
    USER_INFO_EDIT,
    USER_STATUS_CHANGE,
    USER_ROLE_CHANGE,
    COUPON_ISSUE,
    COUPON_CANCEL,
    ORDER_PROCESS,
    ORDER_CANCEL,
    PRODUCT_REGISTER,
    PRODUCT_EDIT,
    PRODUCT_DELETE,
    RESERVATION_PROCESS,
    SETTLEMENT_PROCESS,
    SYSTEM_CONFIG_CHANGE
}
