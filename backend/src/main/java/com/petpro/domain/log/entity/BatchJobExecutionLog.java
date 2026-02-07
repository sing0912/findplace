package com.petpro.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 배치 실행 로그 엔티티
 */
@Entity
@Table(name = "batch_job_execution_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BatchJobExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Builder.Default
    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Builder.Default
    @Column(name = "success_count", nullable = false)
    private Integer successCount = 0;

    @Builder.Default
    @Column(name = "fail_count", nullable = false)
    private Integer failCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static BatchJobExecutionLog start(String jobName, String jobType) {
        return BatchJobExecutionLog.builder()
                .jobName(jobName)
                .jobType(jobType)
                .startedAt(LocalDateTime.now())
                .status("RUNNING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void complete(int total, int success, int fail) {
        this.completedAt = LocalDateTime.now();
        this.status = fail > 0 ? "PARTIAL" : "COMPLETED";
        this.totalCount = total;
        this.successCount = success;
        this.failCount = fail;
        this.executionTimeMs = Duration.between(startedAt, completedAt).toMillis();
    }

    public void fail(String errorMessage) {
        this.completedAt = LocalDateTime.now();
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.executionTimeMs = Duration.between(startedAt, completedAt).toMillis();
    }
}
