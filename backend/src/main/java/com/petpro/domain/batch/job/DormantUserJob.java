package com.petpro.domain.batch.job;

import com.petpro.domain.batch.entity.BatchJobLog;
import com.petpro.domain.batch.service.BatchLogService;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 휴면 계정 처리 배치
 * 매일 04:00 실행
 * 1년 미접속 계정 → INACTIVE 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DormantUserJob {

    private static final String JOB_NAME = "DormantUserJob";
    private static final String JOB_TYPE = "USER";

    private final BatchLogService batchLogService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void processDormantUsers() {
        log.info("휴면 계정 처리 배치 시작");
        BatchJobLog jobLog = batchLogService.startJob(JOB_NAME, JOB_TYPE);

        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

            // 1년 이상 미접속 활성 계정 조회
            List<User> dormantUsers = findDormantUsers(oneYearAgo);
            totalCount = dormantUsers.size();

            if (dormantUsers.isEmpty()) {
                log.info("휴면 처리할 계정이 없습니다.");
                batchLogService.completeJob(jobLog.getId(), 0, 0, 0);
                return;
            }

            // 각 계정 휴면 처리
            for (User user : dormantUsers) {
                try {
                    // 상태를 INACTIVE로 변경
                    user.inactivate();
                    successCount++;
                    log.info("휴면 처리 완료: userId={}, lastLoginAt={}",
                            user.getId(), user.getLastLoginAt());
                } catch (Exception e) {
                    log.error("휴면 처리 실패: userId={}, error={}", user.getId(), e.getMessage());
                    failCount++;
                }
            }

            batchLogService.completeJob(jobLog.getId(), totalCount, successCount, failCount);
            log.info("휴면 계정 처리 배치 완료: total={}, success={}, fail={}",
                    totalCount, successCount, failCount);

        } catch (Exception e) {
            log.error("휴면 계정 처리 배치 실패", e);
            batchLogService.failJob(jobLog.getId(), e.getMessage());
        }
    }

    private List<User> findDormantUsers(LocalDateTime lastLoginBefore) {
        return userRepository.findDormantUsers(lastLoginBefore);
    }
}
