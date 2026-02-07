package com.petpro.domain.log.repository;

import com.petpro.domain.log.entity.SystemSyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * 시스템 연동 로그 Repository
 */
public interface SystemSyncLogRepository extends JpaRepository<SystemSyncLog, Long> {

    Page<SystemSyncLog> findByStartedAtBetween(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<SystemSyncLog> findBySyncTypeAndStartedAtBetween(
            String syncType, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
