# 돌봄 (Care)

## 개요

돌봄 일지 작성 및 GPS 산책 트래킹을 담당하는 도메인입니다.
펫시터(PARTNER)가 진행 중인 예약에 대해 돌봄 일지를 작성하고, 산책 시 GPS 경로를 기록합니다.
반려인(CUSTOMER)은 실시간으로 돌봄 일지 타임라인과 산책 GPS를 확인할 수 있습니다.

---

## 엔티티

### CareJournal (돌봄 일지)

예약 1건당 1개의 돌봄 일지가 생성됩니다.

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingId | Long | 예약 ID (FK) | Unique, Not Null |
| partnerId | Long | 시터 ID (FK) | Not Null |
| summary | Text | 돌봄 완료 요약 코멘트 | Nullable |
| startedAt | DateTime | 돌봄 시작 시각 | Not Null |
| completedAt | DateTime | 돌봄 완료 시각 | Nullable |
| status | Enum | 일지 상태 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

#### JournalStatus (일지 상태)

| 값 | 설명 |
|----|------|
| IN_PROGRESS | 돌봄 진행 중 |
| COMPLETED | 돌봄 완료 |

### JournalEntry (일지 항목)

돌봄 일지에 속하는 개별 기록 항목입니다. 하나의 일지에 N개의 항목이 포함됩니다.

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| journalId | Long | 돌봄 일지 ID (FK) | Not Null |
| entryType | Enum | 항목 유형 | Not Null |
| content | Text | 기록 내용 | Not Null |
| recordedAt | DateTime | 기록 시각 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |

#### EntryType (항목 유형)

| 값 | 설명 | 사용 예시 |
|----|------|-----------|
| MEAL | 식사 기록 | "오전 10시 사료 1컵 급여 완료" |
| BOWEL | 배변 기록 | "정상 배변, 산책 중 2회" |
| PLAY | 놀이/활동 | "공놀이 30분, 터그놀이 15분" |
| SNACK | 간식 | "보호자 요청 간식 1개 급여" |
| CONDITION | 컨디션 체크 | "활력 좋음, 식욕 정상" |
| FREE | 자유 기록 | "낮잠 1시간 숙면" |

### JournalMedia (일지 미디어)

일지 항목에 첨부되는 사진/동영상입니다.

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| entryId | Long | 일지 항목 ID (FK) | Not Null |
| mediaType | Enum | 미디어 유형 | Not Null |
| mediaUrl | String | 원본 미디어 URL | Not Null |
| thumbnailUrl | String | 썸네일 URL | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

#### MediaType (미디어 유형)

| 값 | 설명 |
|----|------|
| IMAGE | 이미지 (jpg, jpeg, png, webp) |
| VIDEO | 동영상 (mp4, mov) |

### WalkTracking (산책 트래킹)

예약 1건에 대해 여러 번의 산책이 가능합니다 (1:N).

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingId | Long | 예약 ID (FK) | Not Null |
| partnerId | Long | 시터 ID (FK) | Not Null |
| startedAt | DateTime | 산책 시작 시각 | Not Null |
| endedAt | DateTime | 산책 종료 시각 | Nullable |
| distanceMeters | Integer | 총 이동 거리 (미터) | Default 0 |
| durationMinutes | Integer | 산책 시간 (분) | Default 0 |
| status | Enum | 트래킹 상태 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |

#### WalkStatus (트래킹 상태)

| 값 | 설명 |
|----|------|
| TRACKING | 산책 진행 중 (GPS 수집 중) |
| COMPLETED | 산책 완료 |

### GpsPoint (GPS 포인트)

산책 중 수집되는 개별 위치 데이터입니다.

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| walkTrackingId | Long | 산책 트래킹 ID (FK) | Not Null |
| latitude | Decimal(10,7) | 위도 | Not Null |
| longitude | Decimal(10,7) | 경도 | Not Null |
| altitude | Decimal(7,2) | 고도 (미터) | Nullable |
| speed | Decimal(5,2) | 속도 (m/s) | Nullable |
| recordedAt | DateTime | 기록 시각 | Not Null |

---

## 엔티티 관계

```
Booking (1) ────── (0..1) CareJournal : 예약당 1개 일지
CareJournal (1) ── (N) JournalEntry : 일지당 N개 항목
JournalEntry (1) ─ (N) JournalMedia : 항목당 N개 미디어

Booking (1) ────── (N) WalkTracking : 예약당 N번 산책
WalkTracking (1) ─ (N) GpsPoint : 산책당 N개 GPS 포인트
```

---

## 비즈니스 규칙

### 돌봄 일지

1. **진행 중(IN_PROGRESS) 예약에만** 돌봄 일지 작성 가능
2. 예약당 돌봄 일지는 **1개만** 생성 가능
3. 일지 시작 시 예약 상태가 IN_PROGRESS인지 검증
4. 돌봄 완료 시 **요약 코멘트(summary)** 작성 필수
5. 완료된 일지에는 추가 항목 등록 불가

### 일지 항목

1. 항목 추가 시 `recordedAt`은 현재 시각 또는 과거 시각 (미래 불가)
2. 항목 삭제는 불가 (기록 보존 원칙)
3. 미디어는 항목당 최대 **10개**

### 미디어

1. 이미지: 최대 **10MB**, jpg/jpeg/png/webp
2. 동영상: 최대 **100MB**, mp4/mov
3. 저장 경로: `petpro/care/{bookingId}/entry_{entryId}/{uuid}.{ext}`
4. 썸네일: 이미지는 서버에서 자동 생성, 동영상은 첫 프레임 추출

### GPS 산책 트래킹

1. **진행 중 예약에만** 산책 시작 가능
2. GPS 포인트 수집 간격: **5초**
3. GPS 포인트는 **배치 전송** (10~20개씩 묶어서 전송)
4. 산책 종료 시 `distanceMeters`, `durationMinutes` 자동 계산
5. 동시에 진행 가능한 산책: 예약당 **1개**
6. 반려인은 **SSE 또는 WebSocket**으로 실시간 GPS 수신

---

## DDL

### care_journals 테이블

```sql
CREATE TABLE care_journals (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id),
    partner_id BIGINT NOT NULL REFERENCES partners(id),
    summary TEXT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### journal_entries 테이블

```sql
CREATE TABLE journal_entries (
    id BIGSERIAL PRIMARY KEY,
    journal_id BIGINT NOT NULL REFERENCES care_journals(id),
    entry_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### journal_media 테이블

```sql
CREATE TABLE journal_media (
    id BIGSERIAL PRIMARY KEY,
    entry_id BIGINT NOT NULL REFERENCES journal_entries(id),
    media_type VARCHAR(10) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### walk_trackings 테이블

```sql
CREATE TABLE walk_trackings (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    partner_id BIGINT NOT NULL REFERENCES partners(id),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    distance_meters INTEGER DEFAULT 0,
    duration_minutes INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'TRACKING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### gps_points 테이블

```sql
CREATE TABLE gps_points (
    id BIGSERIAL PRIMARY KEY,
    walk_tracking_id BIGINT NOT NULL REFERENCES walk_trackings(id),
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    altitude DECIMAL(7, 2),
    speed DECIMAL(5, 2),
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### 인덱스

```sql
-- care_journals
CREATE UNIQUE INDEX idx_care_journals_booking_id ON care_journals(booking_id);
CREATE INDEX idx_care_journals_partner_id ON care_journals(partner_id);
CREATE INDEX idx_care_journals_status ON care_journals(status);

-- journal_entries
CREATE INDEX idx_journal_entries_journal_id ON journal_entries(journal_id);
CREATE INDEX idx_journal_entries_entry_type ON journal_entries(entry_type);
CREATE INDEX idx_journal_entries_recorded_at ON journal_entries(recorded_at);

-- journal_media
CREATE INDEX idx_journal_media_entry_id ON journal_media(entry_id);

-- walk_trackings
CREATE INDEX idx_walk_trackings_booking_id ON walk_trackings(booking_id);
CREATE INDEX idx_walk_trackings_partner_id ON walk_trackings(partner_id);
CREATE INDEX idx_walk_trackings_status ON walk_trackings(status);

-- gps_points
CREATE INDEX idx_gps_points_walk_tracking_id ON gps_points(walk_tracking_id);
CREATE INDEX idx_gps_points_recorded_at ON gps_points(recorded_at);
```

---

## 패키지 구조

```
domain/care/
├── entity/
│   ├── CareJournal.java
│   ├── JournalEntry.java
│   ├── JournalMedia.java
│   ├── WalkTracking.java
│   ├── GpsPoint.java
│   ├── JournalStatus.java
│   ├── EntryType.java
│   ├── MediaType.java
│   └── WalkStatus.java
├── repository/
│   ├── CareJournalRepository.java
│   ├── JournalEntryRepository.java
│   ├── JournalMediaRepository.java
│   ├── WalkTrackingRepository.java
│   └── GpsPointRepository.java
├── service/
│   ├── CareJournalService.java
│   ├── WalkTrackingService.java
│   └── GpsLiveStreamService.java
├── controller/
│   ├── PartnerCareController.java
│   └── CustomerCareController.java
└── dto/
    ├── CareJournalRequest.java
    ├── CareJournalResponse.java
    ├── JournalEntryRequest.java
    ├── JournalEntryResponse.java
    ├── WalkTrackingRequest.java
    ├── WalkTrackingResponse.java
    └── GpsPointRequest.java
```

---

## 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| CARE_JOURNAL_NOT_FOUND | 404 | 돌봄 일지 없음 |
| CARE_JOURNAL_ALREADY_EXISTS | 409 | 해당 예약에 이미 일지 존재 |
| CARE_JOURNAL_ALREADY_COMPLETED | 400 | 이미 완료된 일지 |
| CARE_BOOKING_NOT_IN_PROGRESS | 400 | 진행 중이 아닌 예약 |
| CARE_ENTRY_NOT_FOUND | 404 | 일지 항목 없음 |
| CARE_MEDIA_LIMIT_EXCEEDED | 400 | 미디어 개수 초과 |
| CARE_INVALID_FILE_TYPE | 400 | 지원하지 않는 파일 형식 |
| CARE_FILE_TOO_LARGE | 400 | 파일 크기 초과 |
| WALK_NOT_FOUND | 404 | 산책 트래킹 없음 |
| WALK_ALREADY_IN_PROGRESS | 409 | 이미 진행 중인 산책 존재 |
| WALK_ALREADY_COMPLETED | 400 | 이미 완료된 산책 |

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | 돌봄 일지/산책 API 상세 스펙 |
| [frontend.md](./frontend.md) | 돌봄 프론트엔드 UI 지침 |

---

## 관련 도메인

- **Booking**: 예약 정보 (진행 중 예약에만 돌봄 일지 작성 가능)
- **Sitter**: 시터 정보 (partnerId 참조)
- **Chat**: 돌봄 중 시터-반려인 소통
- **Review**: 돌봄 완료 후 후기 작성
- **Notification**: 돌봄 일지 업데이트 시 반려인에게 푸시 알림
