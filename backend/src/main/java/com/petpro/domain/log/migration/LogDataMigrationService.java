package com.petpro.domain.log.migration;

import com.petpro.domain.admin.entity.UserRoleChangeLog;
import com.petpro.domain.admin.entity.UserStatusChangeLog;
import com.petpro.domain.admin.repository.UserRoleChangeLogRepository;
import com.petpro.domain.admin.repository.UserStatusChangeLogRepository;
import com.petpro.domain.batch.entity.BatchJobLog;
import com.petpro.domain.batch.repository.BatchJobLogRepository;
import com.petpro.domain.funeralhome.entity.FuneralHomeSyncLog;
import com.petpro.domain.funeralhome.repository.FuneralHomeSyncLogRepository;
import com.petpro.domain.log.entity.*;
import com.petpro.domain.log.repository.AdminActionLogRepository;
import com.petpro.domain.log.repository.BatchJobExecutionLogRepository;
import com.petpro.domain.log.repository.SystemSyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 기존 PostgreSQL 로그 → MySQL 일괄 이관 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogDataMigrationService {

    // 기존 PostgreSQL 리포지토리 (읽기용)
    private final UserStatusChangeLogRepository statusChangeLogRepository;
    private final UserRoleChangeLogRepository roleChangeLogRepository;
    private final BatchJobLogRepository batchJobLogRepository;
    private final FuneralHomeSyncLogRepository funeralHomeSyncLogRepository;

    // 신규 MySQL 리포지토리 (쓰기용)
    private final AdminActionLogRepository adminActionLogRepository;
    private final BatchJobExecutionLogRepository batchJobExecutionLogRepository;
    private final SystemSyncLogRepository systemSyncLogRepository;

    /**
     * 전체 이관 실행
     */
    public MigrationResult executeFullMigration() {
        log.info("로그 데이터 이관 시작");
        MigrationResult result = new MigrationResult();

        try {
            int statusCount = migrateStatusChangeLogs();
            result.setStatusChangeLogCount(statusCount);
            log.info("상태 변경 로그 이관 완료: {} 건", statusCount);
        } catch (Exception e) {
            log.error("상태 변경 로그 이관 실패: {}", e.getMessage(), e);
            result.setStatusChangeLogError(e.getMessage());
        }

        try {
            int roleCount = migrateRoleChangeLogs();
            result.setRoleChangeLogCount(roleCount);
            log.info("역할 변경 로그 이관 완료: {} 건", roleCount);
        } catch (Exception e) {
            log.error("역할 변경 로그 이관 실패: {}", e.getMessage(), e);
            result.setRoleChangeLogError(e.getMessage());
        }

        try {
            int batchCount = migrateBatchJobLogs();
            result.setBatchJobLogCount(batchCount);
            log.info("배치 작업 로그 이관 완료: {} 건", batchCount);
        } catch (Exception e) {
            log.error("배치 작업 로그 이관 실패: {}", e.getMessage(), e);
            result.setBatchJobLogError(e.getMessage());
        }

        try {
            int syncCount = migrateSyncLogs();
            result.setSyncLogCount(syncCount);
            log.info("동기화 로그 이관 완료: {} 건", syncCount);
        } catch (Exception e) {
            log.error("동기화 로그 이관 실패: {}", e.getMessage(), e);
            result.setSyncLogError(e.getMessage());
        }

        log.info("로그 데이터 이관 완료: {}", result);
        return result;
    }

    @Transactional("logTransactionManager")
    protected int migrateStatusChangeLogs() {
        List<UserStatusChangeLog> logs = statusChangeLogRepository.findAll();
        int count = 0;

        for (UserStatusChangeLog srcLog : logs) {
            AdminActionLog target = AdminActionLog.create(
                    srcLog.getChangedBy(),
                    AdminActionType.USER_STATUS_CHANGE,
                    TargetType.USER,
                    srcLog.getUserId(),
                    String.format("%s → %s: %s",
                            srcLog.getPreviousStatus(), srcLog.getNewStatus(), srcLog.getReason()),
                    null,
                    null,
                    null
            );
            adminActionLogRepository.save(target);
            count++;
        }
        return count;
    }

    @Transactional("logTransactionManager")
    protected int migrateRoleChangeLogs() {
        List<UserRoleChangeLog> logs = roleChangeLogRepository.findAll();
        int count = 0;

        for (UserRoleChangeLog srcLog : logs) {
            AdminActionLog target = AdminActionLog.create(
                    srcLog.getChangedBy(),
                    AdminActionType.USER_ROLE_CHANGE,
                    TargetType.USER,
                    srcLog.getUserId(),
                    String.format("%s → %s: %s",
                            srcLog.getPreviousRole(), srcLog.getNewRole(), srcLog.getReason()),
                    null,
                    null,
                    null
            );
            adminActionLogRepository.save(target);
            count++;
        }
        return count;
    }

    @Transactional("logTransactionManager")
    protected int migrateBatchJobLogs() {
        List<BatchJobLog> logs = batchJobLogRepository.findAll();
        int count = 0;

        for (BatchJobLog srcLog : logs) {
            BatchJobExecutionLog target = BatchJobExecutionLog.builder()
                    .jobName(srcLog.getJobName())
                    .jobType(srcLog.getJobType())
                    .startedAt(srcLog.getStartedAt())
                    .completedAt(srcLog.getCompletedAt())
                    .status(srcLog.getStatus().name())
                    .totalCount(srcLog.getTotalCount())
                    .successCount(srcLog.getSuccessCount())
                    .failCount(srcLog.getFailCount())
                    .errorMessage(srcLog.getErrorMessage())
                    .executionTimeMs(srcLog.getExecutionTimeMs())
                    .build();
            batchJobExecutionLogRepository.save(target);
            count++;
        }
        return count;
    }

    @Transactional("logTransactionManager")
    protected int migrateSyncLogs() {
        List<FuneralHomeSyncLog> logs = funeralHomeSyncLogRepository.findAll();
        int count = 0;

        for (FuneralHomeSyncLog srcLog : logs) {
            SystemSyncLog target = SystemSyncLog.builder()
                    .syncType(srcLog.getSyncType().name())
                    .sourceSystem("FUNERAL_HOME_API")
                    .startedAt(srcLog.getStartedAt())
                    .completedAt(srcLog.getCompletedAt())
                    .status(srcLog.getStatus().name())
                    .totalCount(srcLog.getTotalCount())
                    .insertedCount(srcLog.getInsertedCount())
                    .updatedCount(srcLog.getUpdatedCount())
                    .deletedCount(srcLog.getDeletedCount())
                    .errorCount(srcLog.getErrorCount())
                    .errorMessage(srcLog.getErrorMessage())
                    .build();
            systemSyncLogRepository.save(target);
            count++;
        }
        return count;
    }

    /**
     * 이관 결과 DTO
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.ToString
    public static class MigrationResult {
        private int statusChangeLogCount;
        private String statusChangeLogError;
        private int roleChangeLogCount;
        private String roleChangeLogError;
        private int batchJobLogCount;
        private String batchJobLogError;
        private int syncLogCount;
        private String syncLogError;
    }
}
