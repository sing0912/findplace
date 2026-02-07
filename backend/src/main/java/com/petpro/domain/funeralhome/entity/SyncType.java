package com.petpro.domain.funeralhome.entity;

/**
 * 동기화 유형
 */
public enum SyncType {
    /** 증분 동기화 - 매일 02:00 */
    INCREMENTAL,
    /** 전체 동기화 - 매주 일요일 03:00 */
    FULL
}
