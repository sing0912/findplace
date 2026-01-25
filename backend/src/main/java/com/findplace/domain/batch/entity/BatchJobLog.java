package com.findplace.domain.batch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 배치 작업 로그 엔티티
 */
@Entity
@Table(name = "batch_job_logs", indexes = {
    @Index(name = "idx_batch_job_logs_job_name", columnList = "jobName"),
    @Index(name = "idx_batch_job_logs_started_at", columnList = "startedAt"),
    @Index(name = "idx_batch_job_logs_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BatchJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작업명 */
    @Column(nullable = false, length = 100)
    private String jobName;

    /** 작업 유형 */
    @Column(nullable = false, length = 50)
    private String jobType;

    /** 시작 시간 */
    @Column(nullable = false)
    private LocalDateTime startedAt;

    /** 완료 시간 */
    private LocalDateTime completedAt;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchJobStatus status;

    /** 총 처리 건수 */
    @Builder.Default
    private Integer totalCount = 0;

    /** 성공 건수 */
    @Builder.Default
    private Integer successCount = 0;

    /** 실패 건수 */
    @Builder.Default
    private Integer failCount = 0;

    /** 에러 메시지 */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** 실행 시간 (ms) */
    private Long executionTimeMs;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 배치 작업 시작 로그 생성
     */
    public static BatchJobLog start(String jobName, String jobType) {
        return BatchJobLog.builder()
                .jobName(jobName)
                .jobType(jobType)
                .startedAt(LocalDateTime.now())
                .status(BatchJobStatus.RUNNING)
                .build();
    }

    /**
     * 배치 작업 완료 처리
     */
    public void complete(int total, int success, int fail) {
        this.completedAt = LocalDateTime.now();
        this.status = fail > 0 ? BatchJobStatus.PARTIAL : BatchJobStatus.COMPLETED;
        this.totalCount = total;
        this.successCount = success;
        this.failCount = fail;
        this.executionTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
    }

    /**
     * 배치 작업 실패 처리
     */
    public void fail(String errorMessage) {
        this.completedAt = LocalDateTime.now();
        this.status = BatchJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.executionTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
    }
}
