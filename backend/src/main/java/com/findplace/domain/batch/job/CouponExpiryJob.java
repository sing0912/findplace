package com.findplace.domain.batch.job;

import com.findplace.domain.batch.entity.BatchJobLog;
import com.findplace.domain.batch.service.BatchLogService;
import com.findplace.domain.coupon.entity.CouponStatus;
import com.findplace.domain.coupon.entity.UserCoupon;
import com.findplace.domain.coupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 만료 쿠폰 처리 배치
 * 매일 01:00 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpiryJob {

    private static final String JOB_NAME = "CouponExpiryJob";
    private static final String JOB_TYPE = "COUPON";

    private final BatchLogService batchLogService;
    private final UserCouponRepository userCouponRepository;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional("couponTransactionManager")
    public void expireCoupons() {
        log.info("만료 쿠폰 처리 배치 시작");
        BatchJobLog jobLog = batchLogService.startJob(JOB_NAME, JOB_TYPE);

        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            LocalDateTime now = LocalDateTime.now();

            // 만료 대상 쿠폰 조회 (AVAILABLE 상태이면서 만료일이 지난 쿠폰)
            List<UserCoupon> expiredCoupons = userCouponRepository.findByStatusAndExpiredAtBefore(
                    CouponStatus.AVAILABLE, now);

            totalCount = expiredCoupons.size();

            if (expiredCoupons.isEmpty()) {
                log.info("만료 처리할 쿠폰이 없습니다.");
                batchLogService.completeJob(jobLog.getId(), 0, 0, 0);
                return;
            }

            // 각 쿠폰 만료 처리
            for (UserCoupon userCoupon : expiredCoupons) {
                try {
                    userCoupon.expire();
                    successCount++;
                } catch (Exception e) {
                    log.error("쿠폰 만료 처리 실패: userCouponId={}, error={}",
                            userCoupon.getId(), e.getMessage());
                    failCount++;
                }
            }

            batchLogService.completeJob(jobLog.getId(), totalCount, successCount, failCount);
            log.info("만료 쿠폰 처리 배치 완료: total={}, success={}, fail={}",
                    totalCount, successCount, failCount);

        } catch (Exception e) {
            log.error("만료 쿠폰 처리 배치 실패", e);
            batchLogService.failJob(jobLog.getId(), e.getMessage());
        }
    }
}
