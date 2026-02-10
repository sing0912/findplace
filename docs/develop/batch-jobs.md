# Batch Jobs 도메인 영구지침

## 1. 개요

배치 작업 시스템은 주기적으로 실행되는 자동화 작업을 관리합니다.

### 1.1 주요 배치잡
| 작업명 | 실행 시간 | 설명 |
|--------|----------|------|
| BirthdayCouponJob | 00:30 | 생일 쿠폰 자동 발급 |
| CouponExpiryJob | 01:00 | 만료 쿠폰 상태 변경 |
| FuneralHomeSyncJob | 02:00 | 장례식장 데이터 동기화 |
| DormantUserJob | 04:00 | 휴면 계정 처리 |

---

## 2. 데이터 모델

### 2.1 BatchJobLog 엔티티 (V9 마이그레이션)
```java
@Entity
@Table(name = "batch_job_logs")
public class BatchJobLog {
    @Id @GeneratedValue
    private Long id;

    private String jobName;           // 작업명
    private String jobType;           // 작업 유형
    private LocalDateTime startedAt;  // 시작 시간
    private LocalDateTime completedAt; // 완료 시간
    private BatchJobStatus status;    // 상태

    private Integer totalCount;       // 총 처리 건수
    private Integer successCount;     // 성공 건수
    private Integer failCount;        // 실패 건수
    private String errorMessage;      // 에러 메시지
    private Long executionTimeMs;     // 실행 시간 (ms)
}
```

### 2.2 BatchJobStatus
| 값 | 설명 |
|----|------|
| RUNNING | 실행 중 |
| COMPLETED | 완료 |
| FAILED | 실패 |
| PARTIAL | 부분 완료 |

---

## 3. 배치잡 상세

### 3.1 생일 쿠폰 발급 (00:30)
```java
@Scheduled(cron = "0 30 0 * * *")
public void issueBirthdayCoupons() {
    // 1. 오늘 생일인 회원 조회 (Main DB)
    // 2. 생일 쿠폰 조회 (Coupon DB, autoIssueEvent=BIRTHDAY)
    // 3. 각 회원에게 쿠폰 발급
    // 4. 올해 이미 발급받은 회원은 스킵
}
```

### 3.2 만료 쿠폰 처리 (01:00)
```java
@Scheduled(cron = "0 0 1 * * *")
public void expireCoupons() {
    // AVAILABLE 상태 && expiredAt < 현재시간
    // → status = EXPIRED 로 일괄 변경
}
```

### 3.3 장례식장 동기화 (02:00)

> **참고**: FuneralHomeSyncJob은 `batch` 패키지가 아닌 `funeralhome.scheduler` 패키지(`FuneralHomeSyncScheduler`)에 구현되어 있습니다.

```java
@Scheduled(cron = "0 0 2 * * *")
public void syncFuneralHomes() {
    // 공공 API 데이터 동기화
}
```

### 3.4 휴면 계정 처리 (04:00)
```java
@Scheduled(cron = "0 0 4 * * *")
public void processDormantUsers() {
    // 1년 미접속 계정 → INACTIVE 처리
    // 사전 알림 메일 발송 (30일 전, 7일 전)
}
```

---

## 4. 모니터링

### 4.1 관리자 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/v1/admin/batch/logs | 배치 로그 목록 |
| GET | /api/v1/admin/batch/logs/{id} | 배치 로그 상세 |
| POST | /api/v1/admin/batch/{jobName}/run | 수동 실행 |

### 4.2 알림 조건
- 배치 실패 시 Slack/Email 알림
- 실행 시간 5분 초과 시 경고
- 에러율 10% 초과 시 경고

---

## 5. 구현 상태

### 5.1 완료
- BatchJobLog 엔티티
- BatchJobStatus enum
- BatchJobLogRepository
- V9 마이그레이션 (batch_job_logs 테이블)
- FuneralHomeSyncScheduler (장례식장 동기화)
- BirthdayCouponJob (생일 쿠폰 발급)
- CouponExpiryJob (만료 쿠폰 처리)
- DormantUserJob (휴면 계정 처리)
- BatchLogService
- AdminBatchController

### 5.2 추가 구현 필요
- 모니터링 알림 시스템 (Slack/Email 연동)

---

## 6. 주의사항

### 6.1 분산 환경
- 여러 서버에서 실행 시 중복 실행 방지 필요
- Redis Lock 또는 DB Lock 활용
- ShedLock 라이브러리 권장

### 6.2 트랜잭션 관리
- 쿠폰 관련 작업은 couponTransactionManager 사용
- 대량 처리 시 청크 단위 커밋

### 6.3 에러 처리
- 개별 처리 실패 시 전체 롤백하지 않음
- 에러 로그 기록 후 다음 항목 처리
- 최종 결과에 에러 카운트 포함

---

## 7. 관련 도메인

- **User**: 휴면 계정, 생일 정보
- **Coupon**: 생일 쿠폰 발급, 만료 처리
- **FuneralHome**: 데이터 동기화
