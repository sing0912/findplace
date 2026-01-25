package com.findplace.domain.batch.entity;

/**
 * 배치 작업 상태
 */
public enum BatchJobStatus {
    /** 실행 중 */
    RUNNING,
    /** 완료 */
    COMPLETED,
    /** 실패 */
    FAILED,
    /** 부분 완료 */
    PARTIAL
}
