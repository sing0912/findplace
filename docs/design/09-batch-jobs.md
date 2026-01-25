# 배치잡 설계

## 1. 개요

FindPlace 시스템의 배치 작업을 관리합니다. 쿠폰 자동 발급/만료, 장례식장 데이터 동기화 등의 정기 작업을 처리합니다.

---

## 2. 배치잡 목록

### 2.1 전체 스케줄

| 배치명 | 실행 시간 | 주기 | 설명 |
|--------|----------|------|------|
| BirthdayCouponJob | 00:30 | 매일 | 생일 쿠폰 발급 |
| CouponExpiryJob | 01:00 | 매일 | 만료 쿠폰 처리 |
| FuneralHomeSyncJob (증분) | 02:00 | 매일 | 장례식장 데이터 증분 동기화 |
| FuneralHomeSyncJob (전체) | 03:00 | 매주 일요일 | 장례식장 데이터 전체 동기화 |
| DormantUserJob | 04:00 | 매일 | 휴면 회원 처리 |
| StatisticsJob | 05:00 | 매일 | 일일 통계 집계 |

### 2.2 스케줄 다이어그램

```
시간   00:00  01:00  02:00  03:00  04:00  05:00  06:00
       │      │      │      │      │      │      │
       │      │      │      │      │      │      │
매일   │ 00:30│ 01:00│ 02:00│      │ 04:00│ 05:00│
       │ 생일 │ 만료 │ 증분 │      │ 휴면 │ 통계 │
       │ 쿠폰 │ 쿠폰 │ 동기화│      │ 처리 │ 집계 │
       │      │      │      │      │      │      │
일요일 │      │      │      │ 03:00│      │      │
       │      │      │      │ 전체 │      │      │
       │      │      │      │ 동기화│      │      │
```

---

## 3. 생일 쿠폰 발급 (BirthdayCouponJob)

### 3.1 설정

```java
@Configuration
@EnableScheduling
public class BirthdayCouponScheduler {

    @Scheduled(cron = "0 30 0 * * *")  // 매일 00:30
    public void issueBirthdayCoupons() {
        birthdayCouponService.issueTodayBirthdayCoupons();
    }
}
```

### 3.2 로직

```java
@Service
@Transactional
public class BirthdayCouponService {

    public void issueTodayBirthdayCoupons() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        // 오늘 생일인 회원 조회
        List<User> birthdayUsers = userRepository.findByBirthMonthAndDay(month, day);

        // 생일 쿠폰 타입 조회
        CouponType birthdayCouponType = couponTypeRepository.findByCode("BIRTHDAY")
            .orElseThrow(() -> new IllegalStateException("Birthday coupon type not found"));

        int issuedCount = 0;
        int skipCount = 0;

        for (User user : birthdayUsers) {
            // 올해 이미 발급 받았는지 확인
            if (hasReceivedBirthdayCouponThisYear(user.getId())) {
                skipCount++;
                continue;
            }

            // 쿠폰 발급
            issueCoupon(user, birthdayCouponType);
            issuedCount++;
        }

        // 로그 저장
        saveBatchLog("BIRTHDAY_COUPON", issuedCount, skipCount, birthdayUsers.size());
    }

    private boolean hasReceivedBirthdayCouponThisYear(Long userId) {
        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        return userCouponRepository.existsByUserIdAndCouponTypeCodeAndIssuedAtAfter(
            userId, "BIRTHDAY", startOfYear.atStartOfDay()
        );
    }
}
```

### 3.3 쿼리

```sql
-- 오늘 생일인 회원 조회
SELECT * FROM users
WHERE EXTRACT(MONTH FROM birth_date) = :month
  AND EXTRACT(DAY FROM birth_date) = :day
  AND status = 'ACTIVE'
  AND deleted_at IS NULL;
```

---

## 4. 쿠폰 만료 처리 (CouponExpiryJob)

### 4.1 설정

```java
@Configuration
public class CouponExpiryScheduler {

    @Scheduled(cron = "0 0 1 * * *")  // 매일 01:00
    public void expireCoupons() {
        couponExpiryService.processExpiredCoupons();
    }
}
```

### 4.2 로직

```java
@Service
@Transactional
public class CouponExpiryService {

    public void processExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();

        // 만료된 쿠폰 조회 (배치 처리)
        int totalExpired = 0;
        int page = 0;
        int pageSize = 1000;

        while (true) {
            List<UserCoupon> expiredCoupons = userCouponRepository
                .findExpiredCoupons(now, PageRequest.of(page, pageSize));

            if (expiredCoupons.isEmpty()) {
                break;
            }

            // 상태 변경
            for (UserCoupon coupon : expiredCoupons) {
                coupon.setStatus(CouponStatus.EXPIRED);
                coupon.setExpiredAt(now);
            }

            userCouponRepository.saveAll(expiredCoupons);
            totalExpired += expiredCoupons.size();
            page++;
        }

        // 로그 저장
        saveBatchLog("COUPON_EXPIRY", totalExpired, 0, totalExpired);
    }
}
```

### 4.3 만료 예정 알림 (선택)

```java
@Scheduled(cron = "0 0 10 * * *")  // 매일 10:00
public void notifyExpiringCoupons() {
    // 3일 내 만료 예정 쿠폰 보유자에게 알림
    LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3);

    List<UserCoupon> expiringCoupons = userCouponRepository
        .findExpiringCoupons(LocalDateTime.now(), threeDaysLater);

    Map<Long, List<UserCoupon>> byUser = expiringCoupons.stream()
        .collect(Collectors.groupingBy(uc -> uc.getUserId()));

    for (Map.Entry<Long, List<UserCoupon>> entry : byUser.entrySet()) {
        notificationService.sendCouponExpiryNotice(entry.getKey(), entry.getValue());
    }
}
```

---

## 5. 장례식장 동기화 (FuneralHomeSyncJob)

### 5.1 설정

```java
@Configuration
public class FuneralHomeSyncScheduler {

    @Scheduled(cron = "0 0 2 * * *")  // 매일 02:00
    public void incrementalSync() {
        funeralHomeSyncService.runIncrementalSync();
    }

    @Scheduled(cron = "0 0 3 * * SUN")  // 매주 일요일 03:00
    public void fullSync() {
        funeralHomeSyncService.runFullSync();
    }
}
```

### 5.2 증분 동기화 로직

```java
@Service
public class FuneralHomeSyncService {

    private static final int API_PAGE_SIZE = 100;
    private static final int DAILY_API_LIMIT = 10000;

    @Transactional
    public void runIncrementalSync() {
        FuneralHomeSyncLog log = createSyncLog(SyncType.INCREMENTAL);

        try {
            int insertedCount = 0;
            int updatedCount = 0;
            int page = 1;
            int totalCount = 0;

            // API 전체 데이터 조회
            while (apiRateLimiter.canCall()) {
                GovApiResponse response = govApiService.fetchFuneralHomes(page, API_PAGE_SIZE);

                if (response.getBody().getItems() == null) {
                    break;
                }

                List<GovFuneralHomeItem> items = response.getBody().getItems().getItem();
                totalCount = response.getBody().getTotalCount();

                for (GovFuneralHomeItem item : items) {
                    Optional<FuneralHome> existing = funeralHomeRepository
                        .findByNameAndRoadAddress(item.getNm(), item.getRoadAddr());

                    if (existing.isPresent()) {
                        updateFuneralHome(existing.get(), item);
                        updatedCount++;
                    } else {
                        insertFuneralHome(item);
                        insertedCount++;
                    }
                }

                apiRateLimiter.incrementCount();

                if (page * API_PAGE_SIZE >= totalCount) {
                    break;
                }
                page++;
            }

            // 좌표 없는 항목 Geocoding
            geocodeMissingCoordinates();

            // 로그 완료
            completeSyncLog(log, insertedCount, updatedCount, 0, totalCount);

        } catch (Exception e) {
            failSyncLog(log, e.getMessage());
            throw e;
        }
    }

    @Async
    public void geocodeMissingCoordinates() {
        List<FuneralHome> homesWithoutCoords = funeralHomeRepository
            .findByLatitudeIsNull();

        for (FuneralHome home : homesWithoutCoords) {
            try {
                GeocodingResult result = locationService.geocode(home.getRoadAddress());
                if (result != null) {
                    home.setLatitude(result.getLatitude());
                    home.setLongitude(result.getLongitude());
                    home.setGeocodedAt(LocalDateTime.now());
                    funeralHomeRepository.save(home);
                }
                Thread.sleep(50); // Rate limiting
            } catch (Exception e) {
                log.warn("Geocoding failed for: {}", home.getName(), e);
            }
        }
    }
}
```

### 5.3 전체 동기화 로직

```java
@Transactional
public void runFullSync() {
    FuneralHomeSyncLog log = createSyncLog(SyncType.FULL);

    try {
        // 1. 임시 테이블에 전체 데이터 수집
        Set<String> apiDataKeys = new HashSet<>();
        int page = 1;

        while (apiRateLimiter.canCall()) {
            GovApiResponse response = govApiService.fetchFuneralHomes(page, API_PAGE_SIZE);
            // ... 데이터 수집
        }

        // 2. 기존 데이터와 비교
        List<FuneralHome> allHomes = funeralHomeRepository.findAll();
        int deletedCount = 0;

        for (FuneralHome home : allHomes) {
            String key = home.getName() + "|" + home.getRoadAddress();
            if (!apiDataKeys.contains(key)) {
                // API에서 삭제된 데이터
                home.setIsActive(false);
                deletedCount++;
            }
        }

        // 3. 전체 좌표 재검증
        validateAllCoordinates();

        completeSyncLog(log, insertedCount, updatedCount, deletedCount, totalCount);

    } catch (Exception e) {
        failSyncLog(log, e.getMessage());
        throw e;
    }
}
```

---

## 6. 휴면 회원 처리 (DormantUserJob)

### 6.1 설정

```java
@Configuration
public class DormantUserScheduler {

    @Scheduled(cron = "0 0 4 * * *")  // 매일 04:00
    public void processDormantUsers() {
        dormantUserService.processDormantUsers();
    }
}
```

### 6.2 로직

```java
@Service
public class DormantUserService {

    private static final int DORMANT_DAYS = 365;  // 1년

    @Transactional
    public void processDormantUsers() {
        LocalDateTime dormantThreshold = LocalDateTime.now().minusDays(DORMANT_DAYS);

        // 1년 이상 로그인하지 않은 활성 회원
        List<User> dormantCandidates = userRepository
            .findByStatusAndLastLoginAtBefore(UserStatus.ACTIVE, dormantThreshold);

        int processedCount = 0;

        for (User user : dormantCandidates) {
            // 휴면 처리
            user.setStatus(UserStatus.INACTIVE);

            // 휴면 알림 발송
            notificationService.sendDormantNotice(user);

            processedCount++;
        }

        saveBatchLog("DORMANT_USER", processedCount, 0, dormantCandidates.size());
    }

    // 휴면 해제 시 복귀 쿠폰 발급
    @EventListener
    public void onUserReactivated(UserReactivatedEvent event) {
        couponService.issueAutoIssueCoupon(event.getUserId(), AutoIssueEvent.DORMANT_RETURN);
    }
}
```

---

## 7. 자동 쿠폰 발급 이벤트

### 7.1 이벤트 타입

| 이벤트 | 설명 | 발급 시점 |
|--------|------|----------|
| SIGNUP | 회원가입 | 가입 즉시 |
| FIRST_ORDER | 첫 주문 | 첫 주문 완료 시 |
| BIRTHDAY | 생일 | 생일 당일 00:30 |
| DORMANT_RETURN | 휴면 복귀 | 휴면 해제 시 |
| REVIEW_WRITE | 리뷰 작성 | 리뷰 등록 시 |

### 7.2 이벤트 리스너

```java
@Component
public class AutoCouponIssueListener {

    @TransactionalEventListener
    public void onUserSignup(UserSignupEvent event) {
        couponService.issueAutoIssueCoupon(event.getUserId(), AutoIssueEvent.SIGNUP);
    }

    @TransactionalEventListener
    public void onFirstOrder(FirstOrderEvent event) {
        couponService.issueAutoIssueCoupon(event.getUserId(), AutoIssueEvent.FIRST_ORDER);
    }

    @TransactionalEventListener
    public void onReviewWrite(ReviewWriteEvent event) {
        couponService.issueAutoIssueCoupon(event.getUserId(), AutoIssueEvent.REVIEW_WRITE);
    }
}
```

### 7.3 쿠폰 발급 서비스

```java
@Service
public class AutoCouponIssueService {

    @Transactional
    public void issueAutoIssueCoupon(Long userId, AutoIssueEvent event) {
        // 해당 이벤트에 대한 활성 쿠폰 타입 조회
        List<CouponType> couponTypes = couponTypeRepository
            .findActiveByAutoIssueEvent(event);

        for (CouponType type : couponTypes) {
            // 중복 발급 체크
            if (isDuplicateIssue(userId, type, event)) {
                continue;
            }

            // 쿠폰 생성 및 발급
            Coupon coupon = createCoupon(type);
            UserCoupon userCoupon = issueCouponToUser(userId, coupon);

            // 발급 알림
            notificationService.sendCouponIssuedNotice(userId, userCoupon);
        }
    }

    private boolean isDuplicateIssue(Long userId, CouponType type, AutoIssueEvent event) {
        // 이벤트별 중복 체크 로직
        switch (event) {
            case SIGNUP:
            case FIRST_ORDER:
                // 1회만 발급
                return userCouponRepository.existsByUserIdAndCouponTypeId(userId, type.getId());

            case BIRTHDAY:
                // 연 1회
                LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
                return userCouponRepository.existsByUserIdAndCouponTypeIdAndIssuedAtAfter(
                    userId, type.getId(), startOfYear);

            case REVIEW_WRITE:
                // 중복 발급 허용 (제한 없음)
                return false;

            default:
                return false;
        }
    }
}
```

---

## 8. 통계 집계 (StatisticsJob)

### 8.1 설정

```java
@Configuration
public class StatisticsScheduler {

    @Scheduled(cron = "0 0 5 * * *")  // 매일 05:00
    public void aggregateDailyStatistics() {
        statisticsService.aggregateDailyStats();
    }
}
```

### 8.2 로직

```java
@Service
public class StatisticsService {

    @Transactional
    public void aggregateDailyStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        DailyStatistics stats = new DailyStatistics();
        stats.setStatDate(yesterday);

        // 회원 통계
        stats.setNewUserCount(userRepository.countByCreatedAtBetween(
            yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay()));
        stats.setActiveUserCount(userRepository.countByLastLoginAtBetween(
            yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay()));

        // 쿠폰 통계
        stats.setIssuedCouponCount(userCouponRepository.countByIssuedAtBetween(
            yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay()));
        stats.setUsedCouponCount(userCouponRepository.countByUsedAtBetween(
            yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay()));

        // 주문 통계
        stats.setOrderCount(orderRepository.countByCreatedAtBetween(
            yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay()));
        stats.setTotalOrderAmount(orderRepository.sumAmountByCreatedAtBetween(
            yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay()));

        dailyStatisticsRepository.save(stats);
    }
}
```

---

## 9. 배치 로그 관리

### 9.1 BatchJobLog 엔티티

```
┌─────────────────────────────────────────────────────────────┐
│                       BatchJobLog                           │
├─────────────────────────────────────────────────────────────┤
│  id                  BIGINT PK AUTO_INCREMENT               │
│  jobName             VARCHAR(100) NOT NULL                  │
│  status              VARCHAR(20) NOT NULL                   │
│  startedAt           TIMESTAMP NOT NULL                     │
│  completedAt         TIMESTAMP                              │
│  processedCount      INTEGER                                │
│  successCount        INTEGER                                │
│  failCount           INTEGER                                │
│  errorMessage        TEXT                                   │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 로그 저장

```java
@Service
public class BatchLogService {

    public BatchJobLog startJob(String jobName) {
        BatchJobLog log = new BatchJobLog();
        log.setJobName(jobName);
        log.setStatus(BatchStatus.RUNNING);
        log.setStartedAt(LocalDateTime.now());
        return batchJobLogRepository.save(log);
    }

    public void completeJob(BatchJobLog log, int processed, int success, int fail) {
        log.setStatus(BatchStatus.COMPLETED);
        log.setCompletedAt(LocalDateTime.now());
        log.setProcessedCount(processed);
        log.setSuccessCount(success);
        log.setFailCount(fail);
        batchJobLogRepository.save(log);
    }

    public void failJob(BatchJobLog log, String errorMessage) {
        log.setStatus(BatchStatus.FAILED);
        log.setCompletedAt(LocalDateTime.now());
        log.setErrorMessage(errorMessage);
        batchJobLogRepository.save(log);
    }
}
```

---

## 10. 모니터링 및 알림

### 10.1 실패 알림

```java
@Aspect
@Component
public class BatchJobMonitoringAspect {

    @AfterThrowing(pointcut = "@annotation(Scheduled)", throwing = "ex")
    public void onBatchJobFailed(JoinPoint joinPoint, Exception ex) {
        String jobName = joinPoint.getSignature().getName();
        String errorMessage = ex.getMessage();

        // Slack 알림
        slackNotificationService.sendAlert(
            "배치 작업 실패",
            String.format("Job: %s\nError: %s", jobName, errorMessage)
        );

        // 이메일 알림
        emailService.sendBatchFailureAlert(jobName, errorMessage);
    }
}
```

### 10.2 관리자 대시보드

```
┌─────────────────────────────────────────────────────────────────────────┐
│  배치 작업 현황                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  오늘 실행된 작업                                                         │
│  ─────────────────────────────────────────────────────────────────────  │
│  작업명               │ 상태   │ 시작 시간     │ 처리 │ 성공 │ 실패    │
│  ─────────────────────────────────────────────────────────────────────  │
│  BirthdayCouponJob   │ ✅ 완료│ 00:30:01     │ 150 │ 148 │ 2       │
│  CouponExpiryJob     │ ✅ 완료│ 01:00:00     │ 523 │ 523 │ 0       │
│  FuneralHomeSyncJob  │ ✅ 완료│ 02:00:05     │ 45  │ 43  │ 2       │
│  DormantUserJob      │ ✅ 완료│ 04:00:00     │ 12  │ 12  │ 0       │
│  StatisticsJob       │ ✅ 완료│ 05:00:01     │ 1   │ 1   │ 0       │
│                                                                          │
│  [수동 실행] [로그 상세]                                                   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 11. 수동 실행 API

### 11.1 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | /admin/batch/birthday-coupon | 생일 쿠폰 수동 실행 | ADMIN |
| POST | /admin/batch/coupon-expiry | 쿠폰 만료 수동 실행 | ADMIN |
| POST | /admin/batch/funeral-home-sync | 장례식장 동기화 수동 실행 | ADMIN |
| GET | /admin/batch/logs | 배치 로그 조회 | ADMIN |

### 11.2 수동 실행 구현

```java
@RestController
@RequestMapping("/admin/batch")
@PreAuthorize("hasRole('ADMIN')")
public class BatchJobController {

    @PostMapping("/birthday-coupon")
    public ResponseEntity<BatchJobResponse> runBirthdayCoupon() {
        BatchJobLog log = birthdayCouponService.issueTodayBirthdayCoupons();
        return ResponseEntity.ok(BatchJobResponse.from(log));
    }

    @PostMapping("/funeral-home-sync")
    public ResponseEntity<BatchJobResponse> runFuneralHomeSync(
            @RequestParam(defaultValue = "INCREMENTAL") SyncType type) {

        BatchJobLog log;
        if (type == SyncType.FULL) {
            log = funeralHomeSyncService.runFullSync();
        } else {
            log = funeralHomeSyncService.runIncrementalSync();
        }
        return ResponseEntity.ok(BatchJobResponse.from(log));
    }
}
```

