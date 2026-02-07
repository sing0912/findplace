package com.petpro.domain.batch.repository;

import com.petpro.domain.batch.entity.BatchJobLog;
import com.petpro.domain.batch.entity.BatchJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 배치 작업 로그 레포지토리
 */
@Repository
public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long> {

    Page<BatchJobLog> findAllByOrderByStartedAtDesc(Pageable pageable);

    List<BatchJobLog> findByJobNameOrderByStartedAtDesc(String jobName);

    Optional<BatchJobLog> findTopByJobNameOrderByStartedAtDesc(String jobName);

    Page<BatchJobLog> findByStartedAtBetweenOrderByStartedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countByStatusAndStartedAtBetween(BatchJobStatus status, LocalDateTime start, LocalDateTime end);
}
