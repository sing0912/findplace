package com.petpro.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 시스템 연동 로그 엔티티
 */
@Entity
@Table(name = "system_sync_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SystemSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sync_type", nullable = false, length = 50)
    private String syncType;

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem;

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
    @Column(name = "inserted_count", nullable = false)
    private Integer insertedCount = 0;

    @Builder.Default
    @Column(name = "updated_count", nullable = false)
    private Integer updatedCount = 0;

    @Builder.Default
    @Column(name = "deleted_count", nullable = false)
    private Integer deletedCount = 0;

    @Builder.Default
    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static SystemSyncLog start(String syncType, String sourceSystem) {
        return SystemSyncLog.builder()
                .syncType(syncType)
                .sourceSystem(sourceSystem)
                .startedAt(LocalDateTime.now())
                .status("RUNNING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void complete(int total, int inserted, int updated, int deleted) {
        this.completedAt = LocalDateTime.now();
        this.status = "COMPLETED";
        this.totalCount = total;
        this.insertedCount = inserted;
        this.updatedCount = updated;
        this.deletedCount = deleted;
    }

    public void partial(int total, int inserted, int updated, int deleted, int errors, String errorMsg) {
        this.completedAt = LocalDateTime.now();
        this.status = "PARTIAL";
        this.totalCount = total;
        this.insertedCount = inserted;
        this.updatedCount = updated;
        this.deletedCount = deleted;
        this.errorCount = errors;
        this.errorMessage = errorMsg;
    }

    public void fail(String errorMessage) {
        this.completedAt = LocalDateTime.now();
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }
}
