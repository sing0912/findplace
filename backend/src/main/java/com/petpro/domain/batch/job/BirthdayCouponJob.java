package com.petpro.domain.batch.job;

import com.petpro.domain.batch.entity.BatchJobLog;
import com.petpro.domain.batch.service.BatchLogService;
import com.petpro.domain.coupon.entity.AutoIssueEvent;
import com.petpro.domain.coupon.entity.Coupon;
import com.petpro.domain.coupon.entity.CouponStatus;
import com.petpro.domain.coupon.entity.UserCoupon;
import com.petpro.domain.coupon.repository.CouponRepository;
import com.petpro.domain.coupon.repository.UserCouponRepository;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 생일 쿠폰 발급 배치
 * 매일 00:30 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayCouponJob {

    private static final String JOB_NAME = "BirthdayCouponJob";
    private static final String JOB_TYPE = "COUPON";

    private final BatchLogService batchLogService;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Scheduled(cron = "0 30 0 * * *")
    @Transactional("couponTransactionManager")
    public void issueBirthdayCoupons() {
        log.info("생일 쿠폰 발급 배치 시작");
        BatchJobLog jobLog = batchLogService.startJob(JOB_NAME, JOB_TYPE);

        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 오늘 날짜
            LocalDate today = LocalDate.now();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();

            // 오늘 생일인 활성 회원 조회
            List<User> birthdayUsers = findBirthdayUsers(month, day);
            totalCount = birthdayUsers.size();

            if (birthdayUsers.isEmpty()) {
                log.info("오늘 생일인 회원이 없습니다.");
                batchLogService.completeJob(jobLog.getId(), 0, 0, 0);
                return;
            }

            // 생일 쿠폰 조회
            List<Coupon> birthdayCoupons = couponRepository.findByAutoIssueEventAndIsActiveTrue(AutoIssueEvent.BIRTHDAY);

            if (birthdayCoupons.isEmpty()) {
                log.warn("발급 가능한 생일 쿠폰이 없습니다.");
                batchLogService.completeJob(jobLog.getId(), totalCount, 0, 0);
                return;
            }

            // 각 회원에게 쿠폰 발급
            for (User user : birthdayUsers) {
                try {
                    for (Coupon coupon : birthdayCoupons) {
                        // 올해 이미 발급받은 경우 스킵
                        if (hasReceivedThisYear(user.getId(), coupon.getId(), today.getYear())) {
                            log.debug("이미 발급받은 쿠폰: userId={}, couponId={}", user.getId(), coupon.getId());
                            continue;
                        }

                        // 쿠폰 발급
                        issueCoupon(user.getId(), coupon);
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("쿠폰 발급 실패: userId={}, error={}", user.getId(), e.getMessage());
                    failCount++;
                }
            }

            batchLogService.completeJob(jobLog.getId(), totalCount, successCount, failCount);
            log.info("생일 쿠폰 발급 배치 완료: total={}, success={}, fail={}", totalCount, successCount, failCount);

        } catch (Exception e) {
            log.error("생일 쿠폰 발급 배치 실패", e);
            batchLogService.failJob(jobLog.getId(), e.getMessage());
        }
    }

    private List<User> findBirthdayUsers(int month, int day) {
        return userRepository.findByBirthdayMonthAndDay(month, day);
    }

    private boolean hasReceivedThisYear(Long userId, Long couponId, int year) {
        LocalDateTime yearStart = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime yearEnd = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        return userCouponRepository.existsByUserIdAndCouponIdAndIssuedAtBetween(
                userId, couponId, yearStart, yearEnd);
    }

    private void issueCoupon(Long userId, Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusDays(coupon.getValidDays() != null ? coupon.getValidDays() : 30);

        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .coupon(coupon)
                .status(CouponStatus.AVAILABLE)
                .issuedAt(now)
                .expiredAt(expiredAt)
                .build();

        userCouponRepository.save(userCoupon);
        log.info("생일 쿠폰 발급: userId={}, couponId={}", userId, coupon.getId());
    }
}
