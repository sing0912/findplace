# 쿠폰 도메인 설계

## 1. 개요

쿠폰(Coupon) 도메인은 할인 쿠폰의 생성, 발급, 사용, 만료를 관리합니다. **별도의 데이터베이스**를 사용하여 마이크로서비스로 분리 가능하도록 설계되었습니다.

### 1.1 핵심 특징

- **별도 DB**: `petpro_coupon` (Port: 5435)
- **EAV 패턴**: 유연한 쿠폰 조건 설정
- **자동 발급**: 이벤트 기반 쿠폰 발급
- **배치 처리**: 생일 쿠폰, 만료 쿠폰 자동 회수

---

## 2. 데이터베이스 구성

```
┌─────────────────────────────────────────────────────────────┐
│                    Coupon DB (별도 분리)                     │
│                    Port: 5435                                │
├─────────────────────────────────────────────────────────────┤
│  • coupon_types          쿠폰 유형 정의                      │
│  • coupons               쿠폰 마스터                         │
│  • coupon_conditions     쿠폰 조건 (EAV)                     │
│  • user_coupons          회원 보유 쿠폰                      │
│  • coupon_usage_histories 사용 이력                          │
└─────────────────────────────────────────────────────────────┘

참조 관계 (FK 아님, ID만 참조):
  - user_coupons.user_id → Main DB users.id
  - user_coupons.order_id → Main DB orders.id
```

---

## 3. 엔티티 설계

### 3.1 CouponType (쿠폰 유형)

```
┌─────────────────────────────────────────────────────────────┐
│                       CouponType                             │
├─────────────────────────────────────────────────────────────┤
│  id              BIGINT PK AUTO_INCREMENT                   │
│  code            VARCHAR(50) NOT NULL UNIQUE                │
│  name            VARCHAR(100) NOT NULL                      │
│  description     TEXT                                       │
│  isActive        BOOLEAN DEFAULT TRUE                       │
│  createdAt       TIMESTAMP NOT NULL                         │
│  updatedAt       TIMESTAMP NOT NULL                         │
└─────────────────────────────────────────────────────────────┘
```

**기본 제공 유형:**

| code | name | 설명 |
|------|------|------|
| FIXED | 정액 할인 | 고정 금액 할인 (예: 5,000원) |
| PERCENT | 정률 할인 | 퍼센트 할인 (예: 10%) |
| SHIPPING | 배송비 할인 | 배송비 무료/할인 |
| PERIOD | 기간 할인 | 특정 기간 동안 할인 |
| BULK | 대량구매 할인 | N개 이상 구매 시 할인 |
| AMOUNT | 금액별 할인 | N원 이상 구매 시 할인 |
| FIRST_ORDER | 첫 주문 할인 | 첫 주문 고객 전용 |
| BIRTHDAY | 생일 할인 | 생일 기념 할인 |

### 3.2 Coupon (쿠폰 마스터)

```
┌─────────────────────────────────────────────────────────────┐
│                         Coupon                               │
├─────────────────────────────────────────────────────────────┤
│  id                      BIGINT PK AUTO_INCREMENT           │
│                                                              │
│  [기본 정보]                                                  │
│  code                    VARCHAR(50) NOT NULL UNIQUE        │
│  name                    VARCHAR(200) NOT NULL              │
│  description             TEXT                               │
│  couponTypeId            BIGINT FK (→ CouponType)           │
│                                                              │
│  [할인 설정]                                                  │
│  discountMethod          VARCHAR(20) NOT NULL               │
│  discountValue           DECIMAL(10,2) NOT NULL             │
│  maxDiscountAmount       DECIMAL(10,2)                      │
│                                                              │
│  [발급 설정]                                                  │
│  issueType               VARCHAR(20) DEFAULT 'MANUAL'       │
│  autoIssueEvent          VARCHAR(30)                        │
│  maxIssueCount           INT                                │
│  issuedCount             INT DEFAULT 0                      │
│  maxPerUser              INT DEFAULT 1                      │
│                                                              │
│  [유효 기간]                                                  │
│  validStartDate          DATE                               │
│  validEndDate            DATE                               │
│  validDays               INT                                │
│                                                              │
│  [옵션]                                                       │
│  isStackable             BOOLEAN DEFAULT FALSE              │
│  isActive                BOOLEAN DEFAULT TRUE               │
│                                                              │
│  [Audit]                                                     │
│  createdAt               TIMESTAMP NOT NULL                 │
│  updatedAt               TIMESTAMP NOT NULL                 │
└─────────────────────────────────────────────────────────────┘
```

**discountMethod 값:**

| 값 | 설명 |
|----|------|
| FIXED | 정액 할인 (discountValue = 금액) |
| PERCENT | 정률 할인 (discountValue = 퍼센트) |
| FREE | 무료 (배송비 등) |

**issueType 값:**

| 값 | 설명 |
|----|------|
| MANUAL | 관리자 수동 발급 |
| CODE | 쿠폰 코드 입력 |
| AUTO | 이벤트 자동 발급 |

**autoIssueEvent 값:**

| 값 | 설명 | 처리 방식 |
|----|------|----------|
| SIGNUP | 회원가입 | 실시간 (이벤트) |
| FIRST_ORDER | 첫 주문 | 실시간 (이벤트) |
| BIRTHDAY | 생일 | 배치잡 (매일) |
| DORMANT_RETURN | 휴면 해제 | 실시간 (이벤트) |
| REVIEW_WRITE | 리뷰 작성 | 실시간 (이벤트) |

### 3.3 CouponCondition (EAV 패턴)

```
┌─────────────────────────────────────────────────────────────┐
│                    CouponCondition                           │
├─────────────────────────────────────────────────────────────┤
│  id                  BIGINT PK AUTO_INCREMENT               │
│  couponId            BIGINT FK (→ Coupon) ON DELETE CASCADE │
│  conditionKey        VARCHAR(50) NOT NULL                   │
│  conditionOperator   VARCHAR(20) NOT NULL                   │
│  conditionValue      VARCHAR(500) NOT NULL                  │
│  createdAt           TIMESTAMP NOT NULL                     │
└─────────────────────────────────────────────────────────────┘
```

**conditionKey 예시:**

| Key | 설명 | 예시 |
|-----|------|------|
| MIN_ORDER_AMOUNT | 최소 주문금액 | 30000 |
| MIN_QUANTITY | 최소 수량 | 3 |
| CATEGORY | 적용 카테고리 | SERVICE,PRODUCT |
| PRODUCT_ID | 적용 상품 | 1,2,3 |
| USER_ROLE | 적용 회원등급 | USER,COMPANY_ADMIN |
| DAY_OF_WEEK | 적용 요일 | MON,TUE,WED |
| TIME_RANGE | 적용 시간대 | 09:00,18:00 |
| FIRST_ORDER | 첫 주문 여부 | true |
| REGION | 적용 지역 | 서울,경기 |

**conditionOperator 값:**

| 값 | 설명 | 사용 예 |
|----|------|--------|
| EQ | 같음 | FIRST_ORDER EQ true |
| GTE | 이상 | MIN_ORDER_AMOUNT GTE 30000 |
| LTE | 이하 | - |
| GT | 초과 | - |
| LT | 미만 | - |
| IN | 포함 | CATEGORY IN SERVICE,PRODUCT |
| NOT_IN | 미포함 | - |
| BETWEEN | 범위 | TIME_RANGE BETWEEN 09:00,18:00 |
| LIKE | 포함(문자열) | - |

### 3.4 UserCoupon (회원 보유 쿠폰)

```
┌─────────────────────────────────────────────────────────────┐
│                       UserCoupon                             │
├─────────────────────────────────────────────────────────────┤
│  id              BIGINT PK AUTO_INCREMENT                   │
│  couponId        BIGINT FK (→ Coupon)                       │
│  userId          BIGINT NOT NULL (Main DB 참조)              │
│                                                              │
│  [상태]                                                       │
│  status          VARCHAR(20) DEFAULT 'AVAILABLE'            │
│                                                              │
│  [일시]                                                       │
│  issuedAt        TIMESTAMP NOT NULL                         │
│  expiredAt       TIMESTAMP NOT NULL                         │
│  usedAt          TIMESTAMP                                  │
│  revokedAt       TIMESTAMP                                  │
│                                                              │
│  [사용 정보]                                                  │
│  orderId         BIGINT (Main DB 참조)                       │
│                                                              │
│  [Audit]                                                     │
│  createdAt       TIMESTAMP NOT NULL                         │
│  updatedAt       TIMESTAMP NOT NULL                         │
└─────────────────────────────────────────────────────────────┘
```

**status 값:**

| 값 | 설명 |
|----|------|
| AVAILABLE | 사용 가능 |
| USED | 사용 완료 |
| EXPIRED | 만료됨 |
| REVOKED | 회수됨 |

### 3.5 CouponUsageHistory (사용 이력)

```
┌─────────────────────────────────────────────────────────────┐
│                   CouponUsageHistory                         │
├─────────────────────────────────────────────────────────────┤
│  id              BIGINT PK AUTO_INCREMENT                   │
│  userCouponId    BIGINT FK (→ UserCoupon)                   │
│  userId          BIGINT NOT NULL                            │
│  orderId         BIGINT NOT NULL                            │
│  discountAmount  DECIMAL(10,2) NOT NULL                     │
│  usedAt          TIMESTAMP NOT NULL                         │
│  createdAt       TIMESTAMP NOT NULL                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. API 설계

### 4.1 고객용 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /coupons/my | 내 쿠폰 목록 |
| GET | /coupons/my/available | 사용 가능한 쿠폰 |
| POST | /coupons/register | 쿠폰 코드 등록 |
| GET | /coupons/applicable | 주문에 적용 가능한 쿠폰 |

### 4.2 관리자 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /admin/coupons | 쿠폰 목록 |
| GET | /admin/coupons/{id} | 쿠폰 상세 |
| POST | /admin/coupons | 쿠폰 생성 |
| PUT | /admin/coupons/{id} | 쿠폰 수정 |
| DELETE | /admin/coupons/{id} | 쿠폰 삭제 |
| POST | /admin/coupons/{id}/issue | 특정 회원에게 발급 |
| POST | /admin/coupons/{id}/issue-bulk | 일괄 발급 |
| GET | /admin/coupon-types | 쿠폰 유형 목록 |

---

## 5. 요청/응답 DTO

### 5.1 쿠폰 생성 요청

```json
{
  "code": "WELCOME2025",
  "name": "신규가입 환영 쿠폰",
  "description": "신규 가입 고객을 위한 5,000원 할인 쿠폰입니다.",
  "couponTypeId": 1,
  "discountMethod": "FIXED",
  "discountValue": 5000,
  "maxDiscountAmount": null,
  "issueType": "AUTO",
  "autoIssueEvent": "SIGNUP",
  "maxIssueCount": null,
  "maxPerUser": 1,
  "validDays": 30,
  "validStartDate": null,
  "validEndDate": "2025-12-31",
  "isStackable": false,
  "conditions": [
    {
      "conditionKey": "MIN_ORDER_AMOUNT",
      "conditionOperator": "GTE",
      "conditionValue": "30000"
    },
    {
      "conditionKey": "CATEGORY",
      "conditionOperator": "IN",
      "conditionValue": "SERVICE"
    }
  ]
}
```

### 5.2 쿠폰 응답

```json
{
  "id": 1,
  "code": "WELCOME2025",
  "name": "신규가입 환영 쿠폰",
  "description": "신규 가입 고객을 위한 5,000원 할인 쿠폰입니다.",
  "couponType": {
    "id": 1,
    "code": "FIXED",
    "name": "정액 할인"
  },
  "discountMethod": "FIXED",
  "discountValue": 5000,
  "discountText": "5,000원 할인",
  "maxDiscountAmount": null,
  "issueType": "AUTO",
  "autoIssueEvent": "SIGNUP",
  "maxIssueCount": null,
  "issuedCount": 150,
  "maxPerUser": 1,
  "validDays": 30,
  "validStartDate": null,
  "validEndDate": "2025-12-31",
  "isStackable": false,
  "isActive": true,
  "conditions": [
    {
      "key": "MIN_ORDER_AMOUNT",
      "operator": "GTE",
      "value": "30000",
      "displayText": "3만원 이상 구매 시"
    },
    {
      "key": "CATEGORY",
      "operator": "IN",
      "value": "SERVICE",
      "displayText": "장례 서비스에만 적용"
    }
  ],
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-20T10:00:00"
}
```

### 5.3 내 쿠폰 응답

```json
{
  "id": 1,
  "coupon": {
    "id": 1,
    "code": "WELCOME2025",
    "name": "신규가입 환영 쿠폰",
    "discountText": "5,000원 할인",
    "conditions": [
      "3만원 이상 구매 시",
      "장례 서비스에만 적용"
    ]
  },
  "status": "AVAILABLE",
  "issuedAt": "2025-01-20T14:30:00",
  "expiredAt": "2025-02-19T23:59:59",
  "daysUntilExpiry": 25,
  "usedAt": null,
  "orderId": null
}
```

### 5.4 쿠폰 코드 등록 요청

```json
{
  "code": "SPRING2025"
}
```

---

## 6. 비즈니스 로직

### 6.1 쿠폰 발급 프로세스

```
1. 쿠폰 활성화 상태 확인
2. 발급 수량 한도 확인 (maxIssueCount)
3. 사용자별 발급 횟수 확인 (maxPerUser, 올해 기준)
4. 유효기간 계산
   - validDays 있으면: 발급일 + validDays
   - 없으면: validEndDate
5. UserCoupon 생성 (status: AVAILABLE)
6. 발급 카운트 증가 (issuedCount++)
```

### 6.2 쿠폰 사용 프로세스

```
1. 쿠폰 상태 확인 (AVAILABLE)
2. 유효기간 확인
3. 사용 조건 검증 (CouponCondition)
   - 최소 주문금액
   - 카테고리
   - 첫 주문 여부
   - 등등...
4. 할인금액 계산
   - FIXED: discountValue
   - PERCENT: 주문금액 * (discountValue / 100)
     - maxDiscountAmount 초과 시 제한
5. 쿠폰 상태 변경 (USED)
6. 사용 이력 저장
```

### 6.3 할인금액 계산 예시

```java
public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
    BigDecimal discount;

    switch (coupon.getDiscountMethod()) {
        case FIXED:
            discount = coupon.getDiscountValue();
            break;
        case PERCENT:
            discount = orderAmount.multiply(coupon.getDiscountValue())
                                  .divide(BigDecimal.valueOf(100));
            if (coupon.getMaxDiscountAmount() != null) {
                discount = discount.min(coupon.getMaxDiscountAmount());
            }
            break;
        case FREE:
            discount = orderAmount;  // 전액 (배송비 등)
            break;
        default:
            discount = BigDecimal.ZERO;
    }

    return discount;
}
```

### 6.4 조건 검증 예시

```java
public boolean validateCondition(CouponCondition condition, OrderContext context) {
    String key = condition.getConditionKey();
    String operator = condition.getConditionOperator();
    String value = condition.getConditionValue();

    switch (key) {
        case "MIN_ORDER_AMOUNT":
            BigDecimal minAmount = new BigDecimal(value);
            return compare(context.getOrderAmount(), minAmount, operator);

        case "CATEGORY":
            List<String> categories = Arrays.asList(value.split(","));
            return matchOperator(context.getCategory(), categories, operator);

        case "FIRST_ORDER":
            boolean isFirstOrder = context.isFirstOrder();
            return Boolean.parseBoolean(value) == isFirstOrder;

        // ... 기타 조건들
    }
    return true;
}
```

---

## 7. 배치잡

### 7.1 생일 쿠폰 발급 (매일 00:30)

```java
@Scheduled(cron = "0 30 0 * * *")
public void issueBirthdayCoupons() {
    // 1. 오늘 생일인 회원 조회 (Main DB)
    // 2. 생일 쿠폰 조회 (Coupon DB)
    // 3. 각 회원에게 쿠폰 발급
    // 4. 올해 이미 발급받은 회원은 스킵
}
```

### 7.2 만료 쿠폰 회수 (매일 01:00)

```java
@Scheduled(cron = "0 0 1 * * *")
public void expireCoupons() {
    // AVAILABLE 상태 && expiredAt < 현재시간
    // → status = EXPIRED 로 일괄 변경
}
```

---

## 8. 이벤트 연동

### 8.1 실시간 발급 이벤트

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onUserSignup(UserSignupEvent event) {
    // SIGNUP 이벤트 쿠폰 발급
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onFirstOrder(FirstOrderEvent event) {
    // FIRST_ORDER 이벤트 쿠폰 발급
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onReviewWrite(ReviewWriteEvent event) {
    // REVIEW_WRITE 이벤트 쿠폰 발급
}
```

---

## 9. 멀티 데이터소스 설정

### 9.1 application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${DB_NAME:petpro}
    username: ${DB_USERNAME:petpro}
    password: ${DB_PASSWORD}

coupon:
  datasource:
    url: jdbc:postgresql://localhost:5435/petpro_coupon
    username: ${COUPON_DB_USERNAME:coupon}
    password: ${COUPON_DB_PASSWORD}
```

### 9.2 DataSource 설정

```java
@Configuration
public class CouponDataSourceConfig {

    @Bean
    @ConfigurationProperties("coupon.datasource")
    public DataSource couponDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean couponEntityManagerFactory(
            @Qualifier("couponDataSource") DataSource dataSource) {
        // Coupon 엔티티 전용 EntityManagerFactory
    }

    @Bean
    public PlatformTransactionManager couponTransactionManager(
            @Qualifier("couponEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

---

## 10. 인덱스

```sql
-- coupons
CREATE UNIQUE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_type ON coupons(coupon_type_id);
CREATE INDEX idx_coupons_issue_type ON coupons(issue_type);
CREATE INDEX idx_coupons_auto_event ON coupons(auto_issue_event)
  WHERE auto_issue_event IS NOT NULL;
CREATE INDEX idx_coupons_active ON coupons(is_active);

-- user_coupons
CREATE INDEX idx_user_coupons_user ON user_coupons(user_id);
CREATE INDEX idx_user_coupons_coupon ON user_coupons(coupon_id);
CREATE INDEX idx_user_coupons_status ON user_coupons(status);
CREATE INDEX idx_user_coupons_expired ON user_coupons(expired_at)
  WHERE status = 'AVAILABLE';

-- coupon_conditions
CREATE INDEX idx_coupon_conditions_coupon ON coupon_conditions(coupon_id);
```

---

## 11. 프론트엔드 UI

### 11.1 쿠폰함

```
┌─────────────────────────────────────────────────────────────┐
│  쿠폰함                                   사용 가능 3장       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  쿠폰 등록    [쿠폰 코드 입력              ] [등록]            │
│                                                              │
│  ─────────────────────────────────────────────────────────  │
│                                                              │
│  [사용 가능]  [사용 완료]  [만료]                               │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  🎫 신규가입 환영 쿠폰                                │    │
│  │                                                      │    │
│  │  5,000원 할인                                        │    │
│  │                                                      │    │
│  │  ─────────────────────────────────────────────────  │    │
│  │  3만원 이상 구매 시 · 장례 서비스                      │    │
│  │  ~2025.02.28까지                           D-25     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 11.2 관리자 쿠폰 생성

```
┌─────────────────────────────────────────────────────────────┐
│  쿠폰 생성                                                   │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  기본 정보                                                    │
│  쿠폰 코드     [WELCOME2025          ]                       │
│  쿠폰명        [신규가입 환영 쿠폰      ]                       │
│  쿠폰 유형     [정액 할인 ▼]                                  │
│                                                              │
│  할인 설정                                                    │
│  할인 방식     ● 정액  ○ 정률  ○ 무료                         │
│  할인 금액     [5000                 ] 원                    │
│                                                              │
│  발급 설정                                                    │
│  발급 방식     ○ 수동  ○ 코드입력  ● 자동                     │
│  자동 발급 이벤트  [회원가입 ▼]                                │
│                                                              │
│  유효 기간                                                    │
│  유효 일수     [30                  ] 일                     │
│                                                              │
│  사용 조건 (EAV)                                       [+ 추가]│
│  │ 최소 주문금액  │ 이상(≥)  │ 30000        │ [삭제] │       │
│  │ 카테고리      │ 포함(IN) │ SERVICE      │ [삭제] │       │
│                                                              │
│                              [취소]  [저장]                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 12. 마이크로서비스 분리 시 고려사항

### 12.1 현재 구조 (모놀리식)

- 백엔드 내 `coupon` 패키지로 분리
- 별도 DB 사용
- Main DB와 ID 참조만 (FK 없음)

### 12.2 분리 시 변경사항

```
┌─────────────────┐     REST API      ┌─────────────────┐
│   Main Service  │ ───────────────── │ Coupon Service  │
│   (petpro)   │                   │ (독립 서비스)    │
└─────────────────┘                   └─────────────────┘
         │                                     │
         ▼                                     ▼
   ┌──────────┐                         ┌──────────┐
   │ Main DB  │                         │Coupon DB │
   └──────────┘                         └──────────┘
```

- REST API 또는 gRPC로 통신
- 이벤트 발행은 Kafka/RabbitMQ로 변경
- 분산 트랜잭션 처리 (Saga 패턴)
