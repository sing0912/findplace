# 추모관 (Memorial)

## 개요

사이버 추모관을 관리하는 도메인입니다.
온라인에서 반려동물을 추모하고 방명록을 작성할 수 있습니다.

---

## 엔티티

### Memorial

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 보호자 ID (FK) | Not Null |
| petId | Long | 반려동물 ID (FK) | Not Null |
| title | String | 추모관 제목 | Not Null |
| description | Text | 추모 메시지 | Nullable |
| isPublic | Boolean | 공개 여부 | Default true |
| viewCount | Integer | 조회수 | Default 0 |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### MemorialMedia

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| memorialId | Long | 추모관 ID (FK) | Not Null |
| mediaType | Enum | 미디어 유형 | Not Null |
| url | String | URL | Not Null |
| thumbnailUrl | String | 썸네일 URL | Nullable |
| sortOrder | Integer | 정렬 순서 | Default 0 |
| createdAt | DateTime | 생성일시 | Not Null |

### MediaType

| 값 | 설명 |
|----|------|
| IMAGE | 이미지 |
| VIDEO | 동영상 |

### Guestbook

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| memorialId | Long | 추모관 ID (FK) | Not Null |
| userId | Long | 작성자 ID (FK) | Nullable (비회원) |
| authorName | String | 작성자명 | Not Null |
| content | Text | 내용 | Not Null |
| isAnonymous | Boolean | 익명 여부 | Default false |
| createdAt | DateTime | 생성일시 | Not Null |

### MemorialAnniversary

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| memorialId | Long | 추모관 ID (FK) | Not Null |
| type | Enum | 기일 유형 | Not Null |
| date | Date | 날짜 | Not Null |
| notifyBefore | Integer | 알림 일수 (전) | Default 1 |
| isNotified | Boolean | 알림 발송 여부 | Default false |

### AnniversaryType

| 값 | 설명 |
|----|------|
| DEATH | 기일 |
| BIRTH | 생일 |
| BUDDHIST_49 | 49재 |
| FIRST_YEAR | 1주기 |
| CUSTOM | 사용자 정의 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/memorials | 목록 조회 | Public (공개만) |
| POST | /api/v1/memorials | 생성 | 인증된 사용자 |
| GET | /api/v1/memorials/{id} | 상세 조회 | Public (공개) / 본인 |
| PUT | /api/v1/memorials/{id} | 수정 | 본인 |
| DELETE | /api/v1/memorials/{id} | 삭제 | 본인 |
| GET | /api/v1/memorials/{id}/guestbook | 방명록 목록 | Public (공개) / 본인 |
| POST | /api/v1/memorials/{id}/guestbook | 방명록 작성 | Public |
| POST | /api/v1/memorials/{id}/media | 미디어 업로드 | 본인 |
| DELETE | /api/v1/memorials/{id}/media/{mediaId} | 미디어 삭제 | 본인 |
| GET | /api/v1/memorials/{id}/anniversaries | 기일 목록 | 본인 |
| POST | /api/v1/memorials/{id}/anniversaries | 기일 등록 | 본인 |

---

## 기일 알림

### 자동 계산

- 49재: 사망일 + 49일
- 1주기: 사망일 + 1년
- 매년 기일

### 알림 발송

```
기일 N일 전 (설정값) → 푸시 알림 + 카카오/문자
기일 당일 → 푸시 알림
```

---

## 비즈니스 규칙

1. 공개 추모관은 누구나 조회 가능
2. 비공개 추모관은 본인만 조회 가능
3. 방명록은 누구나 작성 가능 (비회원 포함)
4. 미디어 업로드 용량 제한: 이미지 10MB, 동영상 100MB
5. 추모관당 미디어 최대 50개

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [crud.md](./crud.md) | 추모관 CRUD |
| [guestbook.md](./guestbook.md) | 방명록 관리 |
| [media.md](./media.md) | 미디어 관리 |
| [anniversary.md](./anniversary.md) | 기일 알림 |
