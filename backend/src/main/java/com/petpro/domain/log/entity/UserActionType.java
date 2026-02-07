package com.petpro.domain.log.entity;

/**
 * 사용자 행위 유형
 */
public enum UserActionType {
    LOGIN,
    LOGOUT,
    PROFILE_EDIT,
    PASSWORD_CHANGE,
    REWARD_USE,
    DEPOSIT_USE,
    COUPON_USE,
    ORDER_CREATE,
    ORDER_CANCEL,
    RESERVATION_CREATE,
    RESERVATION_CANCEL,
    REVIEW_WRITE,
    INQUIRY_CREATE
}
