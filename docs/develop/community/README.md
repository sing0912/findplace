# 커뮤니티 (Community)

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 5 (부가)

---

## 1. 개요

반려인(CUSTOMER) 대상 커뮤니티 게시판 도메인입니다. 카테고리별 게시글 작성, 좋아요, 댓글/대댓글, 신고 기능을 제공합니다.

> **참조**: 펫프렌즈 /community 카테고리 구조 (육아꿀팁, 내새꾸자랑, 펫테리어 등)를 PetPro에 맞게 재구성하였습니다.

### 1.1 관련 도메인

| 도메인 | 관계 | 설명 |
|--------|------|------|
| user | N:1 | 게시글/댓글 작성자 |
| file | 참조 | 게시글 이미지 업로드 |
| home | 참조 | 홈 화면 커뮤니티 피드 요약 |
| admin | 참조 | 관리자 게시글/신고 관리 |

---

## 2. 엔티티

### 2.1 PostCategory (게시글 카테고리)

| 값 | 코드 | 설명 |
|----|------|------|
| TIP | TIP | 팁 (돌봄/육아 꿀팁) |
| BOAST | BOAST | 자랑 (내 반려동물 자랑) |
| QA | QA | Q&A (질문/답변) |
| FREE | FREE | 자유 (자유 게시판) |

### 2.2 Post (게시글)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 작성자 ID (FK -> users) | Not Null |
| category | Enum | 게시글 카테고리 | Not Null |
| title | String | 제목 | Not Null, Max 100자 |
| content | Text | 내용 | Not Null, Max 5000자 |
| imageUrls | JSON | 이미지 URL 배열 (최대 10장) | Nullable |
| viewCount | Integer | 조회수 | Not Null, Default 0 |
| likeCount | Integer | 좋아요 수 | Not Null, Default 0 |
| commentCount | Integer | 댓글 수 | Not Null, Default 0 |
| isDeleted | Boolean | 삭제 여부 (Soft Delete) | Not Null, Default false |
| createdAt | LocalDateTime | 생성일시 | Not Null |
| updatedAt | LocalDateTime | 수정일시 | Not Null |

### 2.3 Comment (댓글)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| postId | Long | 게시글 ID (FK -> posts) | Not Null |
| userId | Long | 작성자 ID (FK -> users) | Not Null |
| parentId | Long | 상위 댓글 ID (대댓글용, FK -> comments) | Nullable |
| content | String | 댓글 내용 | Not Null, Max 500자 |
| isDeleted | Boolean | 삭제 여부 (Soft Delete) | Not Null, Default false |
| createdAt | LocalDateTime | 생성일시 | Not Null |
| updatedAt | LocalDateTime | 수정일시 | Not Null |

### 2.4 PostLike (게시글 좋아요)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| postId | Long | 게시글 ID (FK -> posts) | Not Null |
| userId | Long | 사용자 ID (FK -> users) | Not Null |
| createdAt | LocalDateTime | 생성일시 | Not Null |

> **Unique 제약**: (postId, userId) 조합은 유일해야 함

### 2.5 PostReport (게시글 신고)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| postId | Long | 게시글 ID (FK -> posts) | Not Null |
| reporterId | Long | 신고자 ID (FK -> users) | Not Null |
| reason | String | 신고 사유 | Not Null, Max 500자 |
| status | Enum | 신고 처리 상태 | Not Null, Default PENDING |
| createdAt | LocalDateTime | 생성일시 | Not Null |

### 2.6 ReportStatus (신고 처리 상태)

| 값 | 설명 |
|----|------|
| PENDING | 접수됨 (대기) |
| REVIEWED | 검토 완료 (이상 없음) |
| ACTIONED | 조치 완료 (게시글 숨김/삭제) |

---

## 3. 엔티티 관계도

```
User (1) ────────── (N) Post : 게시글 작성
User (1) ────────── (N) Comment : 댓글 작성
User (1) ────────── (N) PostLike : 좋아요
User (1) ────────── (N) PostReport : 신고

Post (1) ────────── (N) Comment : 댓글
Post (1) ────────── (N) PostLike : 좋아요
Post (1) ────────── (N) PostReport : 신고

Comment (1) ─────── (N) Comment : 대댓글 (parentId)
```

---

## 4. DDL

### 4.1 community_posts

```sql
CREATE TABLE community_posts (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    category        VARCHAR(20)     NOT NULL,
    title           VARCHAR(100)    NOT NULL,
    content         TEXT            NOT NULL,
    image_urls      JSONB,
    view_count      INTEGER         NOT NULL DEFAULT 0,
    like_count      INTEGER         NOT NULL DEFAULT 0,
    comment_count   INTEGER         NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 community_comments

```sql
CREATE TABLE community_comments (
    id              BIGSERIAL       PRIMARY KEY,
    post_id         BIGINT          NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    parent_id       BIGINT          REFERENCES community_comments(id) ON DELETE CASCADE,
    content         VARCHAR(500)    NOT NULL,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 4.3 community_post_likes

```sql
CREATE TABLE community_post_likes (
    id              BIGSERIAL       PRIMARY KEY,
    post_id         BIGINT          NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (post_id, user_id)
);
```

### 4.4 community_post_reports

```sql
CREATE TABLE community_post_reports (
    id              BIGSERIAL       PRIMARY KEY,
    post_id         BIGINT          NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    reporter_id     BIGINT          NOT NULL REFERENCES users(id),
    reason          VARCHAR(500)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. 인덱스

```sql
-- community_posts
CREATE INDEX idx_community_posts_user_id ON community_posts(user_id);
CREATE INDEX idx_community_posts_category ON community_posts(category);
CREATE INDEX idx_community_posts_created_at ON community_posts(created_at DESC);
CREATE INDEX idx_community_posts_category_created ON community_posts(category, created_at DESC);
CREATE INDEX idx_community_posts_like_count ON community_posts(like_count DESC);
CREATE INDEX idx_community_posts_not_deleted ON community_posts(created_at DESC) WHERE is_deleted = FALSE;

-- community_comments
CREATE INDEX idx_community_comments_post_id ON community_comments(post_id);
CREATE INDEX idx_community_comments_user_id ON community_comments(user_id);
CREATE INDEX idx_community_comments_parent_id ON community_comments(parent_id);

-- community_post_likes
CREATE INDEX idx_community_post_likes_post_id ON community_post_likes(post_id);
CREATE INDEX idx_community_post_likes_user_id ON community_post_likes(user_id);

-- community_post_reports
CREATE INDEX idx_community_post_reports_post_id ON community_post_reports(post_id);
CREATE INDEX idx_community_post_reports_status ON community_post_reports(status);
```

---

## 6. 비즈니스 규칙

### 6.1 게시글

1. 로그인한 사용자(CUSTOMER, PARTNER)만 게시글 작성 가능
2. 본인의 게시글만 수정/삭제 가능
3. 삭제는 Soft Delete (isDeleted = true)
4. 이미지는 최대 10장까지 첨부 가능 (imageUrls JSON 배열)
5. 카테고리는 필수 선택 (TIP, BOAST, QA, FREE)
6. 제목 최대 100자, 내용 최대 5000자

### 6.2 좋아요

1. 좋아요는 토글 방식: 이미 좋아요한 상태에서 다시 요청하면 좋아요 취소
2. 본인 게시글에도 좋아요 가능
3. 좋아요/취소 시 post의 likeCount를 동기화 (증가/감소)

### 6.3 댓글/대댓글

1. 댓글은 2단계까지만 허용 (댓글 + 대댓글)
2. 대댓글에 다시 대댓글은 불가 (parentId가 있는 댓글의 id를 parentId로 사용 불가)
3. 댓글 삭제 시 Soft Delete → "삭제된 댓글입니다" 표시
4. 댓글 작성/삭제 시 post의 commentCount를 동기화
5. 댓글 최대 500자

### 6.4 신고

1. 본인 게시글은 신고 불가
2. 같은 사용자가 같은 게시글을 중복 신고 불가
3. 동일 게시글에 대해 누적 신고 3회 도달 시 자동으로 게시글 숨김 처리 (isDeleted = true)
4. 관리자가 신고 건을 검토하여 REVIEWED(이상 없음) 또는 ACTIONED(조치 완료) 처리

### 6.5 조회수

1. 게시글 상세 조회 시 viewCount 1 증가
2. 같은 사용자의 연속 조회는 카운트하지 않음 (Redis 기반 30분 내 중복 방지)

---

## 7. 에러 코드

| 코드 | HTTP | 메시지 |
|------|------|--------|
| POST_NOT_FOUND | 404 | 게시글을 찾을 수 없습니다. |
| COMMENT_NOT_FOUND | 404 | 댓글을 찾을 수 없습니다. |
| UNAUTHORIZED_POST_ACCESS | 403 | 해당 게시글에 대한 권한이 없습니다. |
| UNAUTHORIZED_COMMENT_ACCESS | 403 | 해당 댓글에 대한 권한이 없습니다. |
| INVALID_POST_TITLE | 400 | 제목을 입력해주세요. (1~100자) |
| INVALID_POST_CONTENT | 400 | 내용을 입력해주세요. (1~5000자) |
| INVALID_POST_CATEGORY | 400 | 유효하지 않은 카테고리입니다. |
| INVALID_COMMENT_CONTENT | 400 | 댓글 내용을 입력해주세요. (1~500자) |
| INVALID_REPLY_DEPTH | 400 | 대댓글에는 답글을 작성할 수 없습니다. |
| CANNOT_REPORT_OWN_POST | 400 | 본인의 게시글은 신고할 수 없습니다. |
| ALREADY_REPORTED | 409 | 이미 신고한 게시글입니다. |
| POST_IMAGE_LIMIT_EXCEEDED | 400 | 이미지는 최대 10장까지 첨부할 수 있습니다. |

---

## 8. 패키지 구조

```
backend/src/main/java/com/petpro/domain/community/
├── entity/
│   ├── Post.java
│   ├── Comment.java
│   ├── PostLike.java
│   ├── PostReport.java
│   ├── PostCategory.java          # Enum
│   └── ReportStatus.java          # Enum
├── repository/
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   ├── PostLikeRepository.java
│   └── PostReportRepository.java
├── service/
│   ├── PostService.java
│   ├── CommentService.java
│   ├── PostLikeService.java
│   └── PostReportService.java
├── controller/
│   ├── CommunityController.java    # 사용자 API
│   └── AdminCommunityController.java # 관리자 API
└── dto/
    ├── PostRequest.java
    ├── PostResponse.java
    ├── PostListResponse.java
    ├── CommentRequest.java
    ├── CommentResponse.java
    └── ReportRequest.java
```

---

## 9. 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | 커뮤니티 API 상세 스펙 |
| [frontend.md](./frontend.md) | 커뮤니티 프론트엔드 UI 지침 |
