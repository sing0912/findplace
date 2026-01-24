# 알림 (Notification)

## 개요

푸시 알림, SMS, 이메일 발송을 관리하는 도메인입니다.

---

## 엔티티

### Notification

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 수신자 ID (FK) | Not Null |
| type | Enum | 알림 유형 | Not Null |
| channel | Enum | 발송 채널 | Not Null |
| title | String | 제목 | Not Null |
| content | Text | 내용 | Not Null |
| data | JSON | 추가 데이터 | Nullable |
| isRead | Boolean | 읽음 여부 | Default false |
| readAt | DateTime | 읽음 시간 | Nullable |
| sentAt | DateTime | 발송 시간 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### NotificationType

| 값 | 설명 |
|----|------|
| RESERVATION | 예약 관련 |
| ORDER | 주문 관련 |
| DELIVERY | 배송 관련 |
| PAYMENT | 결제 관련 |
| MEMORIAL | 추모관 관련 |
| COLUMBARIUM | 봉안당 관련 |
| SYSTEM | 시스템 공지 |
| MARKETING | 마케팅 |

### NotificationChannel

| 값 | 설명 |
|----|------|
| PUSH | 앱 푸시 |
| SMS | 문자 |
| KAKAO | 카카오 알림톡 |
| EMAIL | 이메일 |

### SmsHistory

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| templateId | Long | 템플릿 ID (FK) | Nullable |
| phone | String | 수신 번호 | Not Null |
| content | Text | 내용 | Not Null |
| status | Enum | 상태 | Not Null |
| provider | String | 발송 업체 | Not Null |
| providerMessageId | String | 업체 메시지 ID | Nullable |
| sentAt | DateTime | 발송 시간 | Nullable |
| deliveredAt | DateTime | 수신 시간 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### EmailHistory

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| templateId | Long | 템플릿 ID (FK) | Nullable |
| toEmail | String | 수신 이메일 | Not Null |
| subject | String | 제목 | Not Null |
| content | Text | 내용 | Not Null |
| status | Enum | 상태 | Not Null |
| provider | String | 발송 업체 | Not Null |
| sentAt | DateTime | 발송 시간 | Nullable |
| openedAt | DateTime | 열람 시간 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### MessageTemplate

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| channel | Enum | 채널 | Not Null |
| code | String | 템플릿 코드 | Unique, Not Null |
| name | String | 템플릿명 | Not Null |
| subject | String | 제목 (이메일용) | Nullable |
| content | Text | 내용 | Not Null |
| variables | JSON | 변수 목록 | Nullable |
| isActive | Boolean | 활성 여부 | Default true |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

---

## API 목록

### 알림

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/notifications | 목록 조회 | 인증된 사용자 |
| POST | /api/v1/notifications | 알림 생성 | Internal |
| GET | /api/v1/notifications/{id} | 상세 조회 | 본인 |
| PUT | /api/v1/notifications/{id} | 수정 | PLATFORM_ADMIN |
| DELETE | /api/v1/notifications/{id} | 삭제 | PLATFORM_ADMIN |
| PUT | /api/v1/notifications/{id}/read | 읽음 처리 | 본인 |
| PUT | /api/v1/notifications/read-all | 전체 읽음 | 본인 |

### SMS

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/sms | 발송 내역 | ADMIN |
| POST | /api/v1/sms/send | 단일 발송 | ADMIN |
| POST | /api/v1/sms/bulk | 대량 발송 | PLATFORM_ADMIN |
| GET | /api/v1/sms/{id} | 상세 조회 | ADMIN |
| GET | /api/v1/sms/templates | 템플릿 목록 | ADMIN |

### 이메일

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/emails | 발송 내역 | ADMIN |
| POST | /api/v1/emails/send | 단일 발송 | ADMIN |
| POST | /api/v1/emails/bulk | 대량 발송 | PLATFORM_ADMIN |
| GET | /api/v1/emails/{id} | 상세 조회 | ADMIN |
| GET | /api/v1/emails/templates | 템플릿 목록 | ADMIN |

---

## 템플릿 변수

### 사용법

```
안녕하세요, {{userName}}님.
{{petName}}의 장례 예약이 확정되었습니다.
예약일시: {{reservationDateTime}}
```

### 주요 변수

| 변수 | 설명 |
|------|------|
| {{userName}} | 사용자 이름 |
| {{petName}} | 반려동물 이름 |
| {{companyName}} | 업체명 |
| {{reservationDateTime}} | 예약 일시 |
| {{orderNumber}} | 주문번호 |
| {{trackingNumber}} | 운송장 번호 |

---

## 대량 발송

### 제한

- SMS: 1회 최대 1,000건
- 이메일: 1회 최대 5,000건
- 일일 발송 한도 설정 가능

### 처리

```
1. 발송 요청 수신
2. 대기열에 추가
3. 비동기 처리 (배치)
4. 결과 저장
```

---

## 외부 서비스 연동

### SMS
- NHN Cloud SMS
- 카카오 알림톡

### 이메일
- AWS SES
- SendGrid

### 푸시
- Firebase Cloud Messaging (FCM)

---

## 비즈니스 규칙

1. 마케팅 수신 동의 사용자에게만 마케팅 알림 발송
2. 야간 시간대(21:00~08:00) 마케팅 발송 제한
3. 발송 실패 시 3회까지 재시도
4. 대량 발송은 승인 후 처리

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [push.md](./push.md) | 푸시 알림 |
| [sms.md](./sms.md) | SMS 발송 |
| [email.md](./email.md) | 이메일 발송 |
| [template.md](./template.md) | 템플릿 관리 |
| [bulk.md](./bulk.md) | 대량 발송 |
