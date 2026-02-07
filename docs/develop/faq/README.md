# FAQ 관리

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 개요

FAQ(자주 묻는 질문)를 카테고리별로 관리하는 도메인입니다.
사용자에게 공개되는 FAQ와 관리자 CRUD 기능을 제공합니다.

---

## 엔티티

### FaqCategory

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| name | String | 카테고리명 | Not Null, Unique |
| sortOrder | Integer | 정렬 순서 | Not Null, Default 0 |
| isActive | Boolean | 활성 여부 | Not Null, Default true |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### Faq

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| categoryId | Long | 카테고리 ID (FK → faq_categories) | Not Null |
| question | String | 질문 | Not Null |
| answer | Text | 답변 | Not Null |
| sortOrder | Integer | 정렬 순서 | Not Null, Default 0 |
| isPublished | Boolean | 공개 여부 | Not Null, Default false |
| viewCount | Integer | 조회수 | Not Null, Default 0 |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

---

## DDL

### faq_categories 테이블

```sql
CREATE TABLE faq_categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### faqs 테이블

```sql
CREATE TABLE faqs (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT       NOT NULL REFERENCES faq_categories(id),
    question     VARCHAR(500) NOT NULL,
    answer       TEXT         NOT NULL,
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    is_published BOOLEAN      NOT NULL DEFAULT FALSE,
    view_count   INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### 인덱스

```sql
-- faq_categories
CREATE INDEX idx_faq_categories_sort_order ON faq_categories(sort_order);
CREATE INDEX idx_faq_categories_is_active ON faq_categories(is_active);

-- faqs
CREATE INDEX idx_faqs_category_id ON faqs(category_id);
CREATE INDEX idx_faqs_is_published ON faqs(is_published);
CREATE INDEX idx_faqs_category_published ON faqs(category_id, is_published);
CREATE INDEX idx_faqs_sort_order ON faqs(sort_order);
CREATE INDEX idx_faqs_view_count ON faqs(view_count DESC);
```

---

## 패키지 구조

```
com.petpro.domain.faq/
├── controller/
│   ├── FaqController.java          # 사용자 공개 API
│   └── AdminFaqController.java     # 관리자 API
├── dto/
│   ├── FaqCategoryRequest.java
│   ├── FaqCategoryResponse.java
│   ├── FaqRequest.java
│   └── FaqResponse.java
├── entity/
│   ├── FaqCategory.java
│   └── Faq.java
├── repository/
│   ├── FaqCategoryRepository.java
│   └── FaqRepository.java
└── service/
    ├── FaqCategoryService.java
    └── FaqService.java
```

---

## 비즈니스 규칙

1. **카테고리별 FAQ 관리**: 모든 FAQ는 반드시 하나의 카테고리에 속함
2. **공개/비공개 전환**: isPublished가 true인 FAQ만 사용자에게 노출
3. **조회수 집계**: 사용자가 FAQ 상세를 조회할 때마다 viewCount 1 증가
4. **정렬 순서**: sortOrder 오름차순으로 정렬, 같은 값이면 createdAt 내림차순
5. **카테고리 삭제 제한**: 하위 FAQ가 존재하는 카테고리는 삭제 불가
6. **비활성 카테고리**: isActive가 false인 카테고리는 사용자에게 노출되지 않음
7. **카테고리명 중복 불가**: 동일한 이름의 카테고리 생성 불가

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | API 명세 |
