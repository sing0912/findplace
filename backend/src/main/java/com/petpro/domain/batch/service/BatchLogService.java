package com.petpro.domain.batch.service;

import com.petpro.domain.batch.entity.BatchJobLog;
import com.petpro.domain.batch.entity.BatchJobStatus;
import com.petpro.domain.batch.repository.BatchJobLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 배치 작업 로그 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchLogService {

    private final BatchJobLogRepository batchJobLogRepository;

    /**
     * 배치 작업 시작 로그 생성
     */
    @Transactional
    public BatchJobLog startJob(String jobName, String jobType) {
        BatchJobLog jobLog = BatchJobLog.builder()
                .jobName(jobName)
                .jobType(jobType)
                .startedAt(LocalDateTime.now())
                .status(BatchJobStatus.RUNNING)
                .totalCount(0)
                .successCount(0)
                .failCount(0)
                .build();

        BatchJobLog saved = batchJobLogRepository.save(jobLog);
        log.info("배치 작업 시작: jobName={}, id={}", jobName, saved.getId());
        return saved;
    }

    /**
     * 배치 작업 완료 처리
     */
    @Transactional
    public void completeJob(Long jobId, int totalCount, int successCount, int failCount) {
        BatchJobLog jobLog = batchJobLogRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("배치 로그를 찾을 수 없습니다: " + jobId));

        jobLog.complete(totalCount, successCount, failCount);

        log.info("배치 작업 완료: jobId={}, status={}, total={}, success={}, fail={}",
                jobId, jobLog.getStatus(), totalCount, successCount, failCount);
    }

    /**
     * 배치 작업 실패 처리
     */
    @Transactional
    public void failJob(Long jobId, String errorMessage) {
        BatchJobLog jobLog = batchJobLogRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("배치 로그를 찾을 수 없습니다: " + jobId));

        jobLog.fail(errorMessage);
        log.error("배치 작업 실패: jobId={}, error={}", jobId, errorMessage);
    }

    /**
     * 배치 로그 목록 조회
     */
    public Page<BatchJobLog> getLogs(Pageable pageable) {
        return batchJobLogRepository.findAllByOrderByStartedAtDesc(pageable);
    }

    /**
     * 배치 로그 상세 조회
     */
    public Optional<BatchJobLog> getLog(Long id) {
        return batchJobLogRepository.findById(id);
    }

    /**
     * 특정 작업의 최근 로그 조회
     */
    public Optional<BatchJobLog> getLatestLog(String jobName) {
        return batchJobLogRepository.findTopByJobNameOrderByStartedAtDesc(jobName);
    }

    /**
     * 기간별 로그 조회
     */
    public Page<BatchJobLog> getLogsByPeriod(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return batchJobLogRepository.findByStartedAtBetweenOrderByStartedAtDesc(start, end, pageable);
    }

    /**
     * 기간별 상태 카운트
     */
    public long countByStatusAndPeriod(BatchJobStatus status, LocalDateTime start, LocalDateTime end) {
        return batchJobLogRepository.countByStatusAndStartedAtBetween(status, start, end);
    }

}
