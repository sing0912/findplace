package com.petpro.domain.log.repository;

import com.petpro.domain.log.entity.BatchJobExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * 배치 실행 로그 Repository
 */
public interface BatchJobExecutionLogRepository extends JpaRepository<BatchJobExecutionLog, Long> {

    Page<BatchJobExecutionLog> findByStartedAtBetween(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<BatchJobExecutionLog> findByJobNameAndStartedAtBetween(
            String jobName, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
