package com.petpro.domain.funeralhome.entity;

/**
 * 동기화 상태
 */
public enum SyncStatus {
    /** 실행 중 */
    RUNNING,
    /** 완료 */
    COMPLETED,
    /** 실패 */
    FAILED,
    /** 부분 완료 */
    PARTIAL
}
