package com.findplace.domain.funeralhome.repository;

import com.findplace.domain.funeralhome.entity.FuneralHomeSyncLog;
import com.findplace.domain.funeralhome.entity.SyncStatus;
import com.findplace.domain.funeralhome.entity.SyncType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 장례식장 동기화 로그 레포지토리
 */
@Repository
public interface FuneralHomeSyncLogRepository extends JpaRepository<FuneralHomeSyncLog, Long> {

    /**
     * 가장 최근 성공한 동기화 로그 조회
     */
    Optional<FuneralHomeSyncLog> findTopByStatusOrderByCompletedAtDesc(SyncStatus status);

    /**
     * 특정 유형의 가장 최근 동기화 로그 조회
     */
    Optional<FuneralHomeSyncLog> findTopBySyncTypeOrderByStartedAtDesc(SyncType syncType);

    /**
     * 실행 중인 동기화 조회
     */
    Optional<FuneralHomeSyncLog> findTopByStatusOrderByStartedAtDesc(SyncStatus status);

    /**
     * 동기화 로그 페이징 조회 (최신순)
     */
    Page<FuneralHomeSyncLog> findAllByOrderByStartedAtDesc(Pageable pageable);

    /**
     * 특정 기간 동기화 로그 조회
     */
    Page<FuneralHomeSyncLog> findByStartedAtBetweenOrderByStartedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 마지막 성공 동기화 시간 조회
     */
    @Query("SELECT MAX(l.completedAt) FROM FuneralHomeSyncLog l WHERE l.status = 'COMPLETED'")
    Optional<LocalDateTime> findLastSuccessfulSyncTime();

    /**
     * 실행 중인 동기화 존재 여부
     */
    boolean existsByStatus(SyncStatus status);
}
