package com.findplace.domain.funeralhome.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 장례식장 동기화 로그 엔티티
 *
 * API 동기화 실행 기록 및 결과 저장
 */
@Entity
@Table(name = "funeral_home_sync_logs", indexes = {
    @Index(name = "idx_sync_logs_sync_type", columnList = "syncType"),
    @Index(name = "idx_sync_logs_started_at", columnList = "startedAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FuneralHomeSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 동기화 유형 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncType syncType;

    /** 동기화 시작 시간 */
    @Column(nullable = false)
    private LocalDateTime startedAt;

    /** 동기화 완료 시간 */
    private LocalDateTime completedAt;

    /** 동기화 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncStatus status;

    /** 총 처리 건수 */
    @Builder.Default
    private Integer totalCount = 0;

    /** 신규 추가 건수 */
    @Builder.Default
    private Integer insertedCount = 0;

    /** 업데이트 건수 */
    @Builder.Default
    private Integer updatedCount = 0;

    /** 삭제(비활성화) 건수 */
    @Builder.Default
    private Integer deletedCount = 0;

    /** 에러 건수 */
    @Builder.Default
    private Integer errorCount = 0;

    /** 에러 메시지 */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // ========== 비즈니스 메서드 ==========

    /**
     * 동기화 시작 로그 생성
     */
    public static FuneralHomeSyncLog start(SyncType syncType) {
        return FuneralHomeSyncLog.builder()
                .syncType(syncType)
                .startedAt(LocalDateTime.now())
                .status(SyncStatus.RUNNING)
                .build();
    }

    /**
     * 동기화 완료 처리
     */
    public void complete(int total, int inserted, int updated, int deleted) {
        this.completedAt = LocalDateTime.now();
        this.status = SyncStatus.COMPLETED;
        this.totalCount = total;
        this.insertedCount = inserted;
        this.updatedCount = updated;
        this.deletedCount = deleted;
    }

    /**
     * 동기화 부분 완료 처리
     */
    public void partial(int total, int inserted, int updated, int deleted, int errors, String errorMsg) {
        this.completedAt = LocalDateTime.now();
        this.status = SyncStatus.PARTIAL;
        this.totalCount = total;
        this.insertedCount = inserted;
        this.updatedCount = updated;
        this.deletedCount = deleted;
        this.errorCount = errors;
        this.errorMessage = errorMsg;
    }

    /**
     * 동기화 실패 처리
     */
    public void fail(String errorMessage) {
        this.completedAt = LocalDateTime.now();
        this.status = SyncStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
