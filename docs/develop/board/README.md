# 게시판 (Board)

## 개요

공지사항, FAQ, 문의 등 게시판을 관리하는 도메인입니다.

---

## 엔티티

### Board

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| code | String | 게시판 코드 | Unique, Not Null |
| name | String | 게시판 이름 | Not Null |
| description | String | 설명 | Nullable |
| boardType | Enum | 게시판 유형 | Not Null |
| isCommentAllowed | Boolean | 댓글 허용 | Default true |
| isAnonymousAllowed | Boolean | 익명 허용 | Default false |
| isActive | Boolean | 활성 여부 | Default true |
| sortOrder | Integer | 정렬 순서 | Default 0 |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### BoardType

| 값 | 설명 |
|----|------|
| NOTICE | 공지사항 |
| FAQ | 자주 묻는 질문 |
| QNA | 1:1 문의 |
| FREE | 자유게시판 |
| REVIEW | 이용후기 |

### Post

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| boardId | Long | 게시판 ID (FK) | Not Null |
| userId | Long | 작성자 ID (FK) | Not Null |
| title | String | 제목 | Not Null |
| content | Text | 내용 | Not Null |
| viewCount | Integer | 조회수 | Default 0 |
| isPinned | Boolean | 고정 여부 | Default false |
| isSecret | Boolean | 비밀글 여부 | Default false |
| status | Enum | 상태 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### PostStatus

| 값 | 설명 |
|----|------|
| DRAFT | 임시저장 |
| PUBLISHED | 게시됨 |
| HIDDEN | 숨김 |
| DELETED | 삭제됨 |

### PostAttachment

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| postId | Long | 게시글 ID (FK) | Not Null |
| fileName | String | 파일명 | Not Null |
| fileUrl | String | 파일 URL | Not Null |
| fileSize | Long | 파일 크기 | Not Null |
| mimeType | String | MIME 타입 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |

### Comment

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| postId | Long | 게시글 ID (FK) | Not Null |
| parentId | Long | 상위 댓글 ID (FK) | Nullable |
| userId | Long | 작성자 ID (FK) | Not Null |
| content | Text | 내용 | Not Null |
| isDeleted | Boolean | 삭제 여부 | Default false |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

---

## API 목록

### 게시판

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/boards | 목록 조회 | Public |
| POST | /api/v1/boards | 생성 | PLATFORM_ADMIN |
| GET | /api/v1/boards/{id} | 상세 조회 | Public |
| PUT | /api/v1/boards/{id} | 수정 | PLATFORM_ADMIN |
| DELETE | /api/v1/boards/{id} | 삭제 | PLATFORM_ADMIN |

### 게시글

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/posts | 목록 조회 | 게시판별 상이 |
| POST | /api/v1/posts | 작성 | 게시판별 상이 |
| GET | /api/v1/posts/{id} | 상세 조회 | 게시판별 상이 |
| PUT | /api/v1/posts/{id} | 수정 | 본인 또는 ADMIN |
| DELETE | /api/v1/posts/{id} | 삭제 | 본인 또는 ADMIN |
| GET | /api/v1/posts/{id}/comments | 댓글 목록 | 게시판별 상이 |
| POST | /api/v1/posts/{id}/comments | 댓글 작성 | 게시판별 상이 |

---

## 게시판별 권한

| 게시판 | 목록 | 조회 | 작성 | 수정/삭제 |
|--------|------|------|------|-----------|
| 공지사항 | Public | Public | ADMIN | ADMIN |
| FAQ | Public | Public | ADMIN | ADMIN |
| 1:1 문의 | 본인 | 본인 | 인증 | 본인/ADMIN |
| 자유게시판 | Public | Public | 인증 | 본인/ADMIN |
| 이용후기 | Public | Public | 인증 | 본인/ADMIN |

---

## 비즈니스 규칙

1. 비밀글은 작성자와 관리자만 조회 가능
2. 1:1 문의는 비밀글 기본 설정
3. 댓글 삭제 시 "삭제된 댓글입니다" 표시
4. 고정글은 목록 상단에 표시
5. 첨부파일 크기 제한: 10MB/파일, 총 50MB

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [crud.md](./crud.md) | 게시글 CRUD |
| [comment.md](./comment.md) | 댓글 관리 |
| [attachment.md](./attachment.md) | 첨부파일 |
| [search.md](./search.md) | 검색 기능 |
