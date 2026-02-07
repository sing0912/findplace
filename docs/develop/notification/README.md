# 알림 (Notification)

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 개요

푸시 알림, SMS, 이메일 발송을 관리하는 도메인입니다.
PetPro 플랫폼의 예약, 돌봄, 채팅, 결제, 리뷰, 시스템, 마케팅 관련 알림을 통합 처리합니다.

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
| data | JSON | 추가 데이터 (딥링크 등) | Nullable |
| isRead | Boolean | 읽음 여부 | Default false |
| readAt | DateTime | 읽음 시간 | Nullable |
| sentAt | DateTime | 발송 시간 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### NotificationType

| 값 | 설명 |
|----|------|
| BOOKING | 예약 관련 (예약 확정/취소/변경 등) |
| CARE | 돌봄 관련 (돌봄 시작/종료, 일지 등록 등) |
| CHAT | 채팅 관련 (새 메시지 등) |
| PAYMENT | 결제 관련 (결제 완료/환불 등) |
| REVIEW | 리뷰 관련 (리뷰 요청/등록 알림) |
| SYSTEM | 시스템 공지 |
| MARKETING | 마케팅 (이벤트/쿠폰 등) |

### NotificationChannel

| 값 | 설명 |
|----|------|
| PUSH | 앱 푸시 (FCM) |
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
{{petName}}의 돌봄 예약이 확정되었습니다.
시터: {{sitterName}}
예약일시: {{bookingDateTime}}
```

### 주요 변수

| 변수 | 설명 |
|------|------|
| {{userName}} | 사용자 이름 |
| {{petName}} | 반려동물 이름 |
| {{sitterName}} | 시터 이름 |
| {{bookingDateTime}} | 예약 일시 |
| {{bookingNumber}} | 예약 번호 |
| {{bookingStatus}} | 예약 상태 |
| {{paymentAmount}} | 결제 금액 |
| {{careStartTime}} | 돌봄 시작 시간 |
| {{careEndTime}} | 돌봄 종료 시간 |

---

## FCM 딥링크

PetPro 앱 내 화면으로 직접 이동하기 위한 딥링크 스키마입니다.
알림의 `data` 필드에 딥링크 URL을 포함하여 전송합니다.

### 스키마

```
petpro://{path}
```

### 딥링크 목록

| NotificationType | 딥링크 | 설명 |
|------------------|--------|------|
| BOOKING | `petpro://reservation/{id}` | 예약 상세 화면 |
| CARE | `petpro://care/{id}` | 돌봄 일지 상세 화면 |
| CHAT | `petpro://chat/{chatRoomId}` | 채팅방 화면 |
| PAYMENT | `petpro://payment/{id}` | 결제 상세 화면 |
| REVIEW | `petpro://review/{bookingId}` | 리뷰 작성/상세 화면 |
| SYSTEM | `petpro://notice/{id}` | 공지사항 상세 화면 |
| MARKETING | `petpro://event/{id}` | 이벤트 상세 화면 |

### FCM 페이로드 예시

```json
{
  "notification": {
    "title": "예약이 확정되었습니다",
    "body": "2월 10일 14:00 돌봄 예약이 확정되었습니다."
  },
  "data": {
    "type": "BOOKING",
    "deepLink": "petpro://reservation/12345",
    "notificationId": "67890"
  }
}
```

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
5. 돌봄 시작/종료 알림은 반려인(CUSTOMER)에게 자동 발송
6. 예약 상태 변경 시 관련 당사자(반려인/시터) 모두에게 알림 발송

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [push.md](./push.md) | 푸시 알림 |
| [sms.md](./sms.md) | SMS 발송 |
| [email.md](./email.md) | 이메일 발송 |
| [template.md](./template.md) | 템플릿 관리 |
| [bulk.md](./bulk.md) | 대량 발송 |
