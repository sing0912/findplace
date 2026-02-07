# 문의 게시판 (Inquiry)

**최종 수정일:** 2026-02-05
**상태:** 확정

---

## 1. 개요

사용자가 관리자에게 문의를 등록하고 답변을 받는 게시판입니다.

---

## 2. API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/inquiries | 내 문의 목록 | ✅ |
| 2 | POST | /api/v1/inquiries | 문의 작성 | ✅ |
| 3 | GET | /api/v1/inquiries/{id} | 문의 상세 | ✅ |
| 4 | PUT | /api/v1/inquiries/{id} | 문의 수정 | ✅ |
| 5 | DELETE | /api/v1/inquiries/{id} | 문의 삭제 | ✅ |

---

## 3. 문의 목록

```
GET /api/v1/inquiries?page=0&size=10

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "content": [
    {
      "id": 1,
      "title": "상품 문의",
      "status": "WAITING",
      "createdAt": "2026-02-05T10:00:00Z"
    },
    {
      "id": 2,
      "title": "배송 문의",
      "status": "ANSWERED",
      "createdAt": "2026-02-04T10:00:00Z"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0
}
```

---

## 4. 문의 작성

```
POST /api/v1/inquiries

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "title": "상품 문의",
  "content": "상품 배송이 언제 되나요?"
}

Response 201:
{
  "id": 1,
  "title": "상품 문의",
  "content": "상품 배송이 언제 되나요?",
  "status": "WAITING",
  "createdAt": "2026-02-05T10:00:00Z"
}

Error 400:
{ "code": "INVALID_TITLE", "message": "제목을 입력해주세요." }
{ "code": "INVALID_CONTENT", "message": "내용을 입력해주세요." }
```

---

## 5. 문의 상세

```
GET /api/v1/inquiries/{id}

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "id": 1,
  "title": "상품 문의",
  "content": "상품 배송이 언제 되나요?",
  "status": "ANSWERED",
  "createdAt": "2026-02-05T10:00:00Z",
  "answer": {
    "content": "안녕하세요. 배송은 2-3일 소요됩니다.",
    "createdAt": "2026-02-05T11:00:00Z"
  }
}

Response 200 (답변 전):
{
  "id": 1,
  "title": "상품 문의",
  "content": "상품 배송이 언제 되나요?",
  "status": "WAITING",
  "createdAt": "2026-02-05T10:00:00Z",
  "answer": null
}

Error 404:
{ "code": "INQUIRY_NOT_FOUND", "message": "문의를 찾을 수 없습니다." }

Error 403:
{ "code": "ACCESS_DENIED", "message": "접근 권한이 없습니다." }
```

---

## 6. 문의 수정

```
PUT /api/v1/inquiries/{id}

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "title": "상품 문의 (수정)",
  "content": "수정된 내용"
}

Response 200:
{
  "id": 1,
  "title": "상품 문의 (수정)",
  "content": "수정된 내용",
  "status": "WAITING",
  "createdAt": "2026-02-05T10:00:00Z"
}

Error 400:
{ "code": "ALREADY_ANSWERED", "message": "답변이 완료된 문의는 수정할 수 없습니다." }
```

---

## 7. 문의 삭제

```
DELETE /api/v1/inquiries/{id}

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{ "success": true }

Error 400:
{ "code": "ALREADY_ANSWERED", "message": "답변이 완료된 문의는 삭제할 수 없습니다." }
```

---

## 8. Entity

### 8.1 InquiryStatus (Enum)
```java
public enum InquiryStatus {
    WAITING,    // 답변 대기
    ANSWERED    // 답변 완료
}
```

### 8.2 Inquiry
```java
@Entity
public class Inquiry {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @OneToOne(mappedBy = "inquiry", cascade = CascadeType.ALL)
    private InquiryAnswer answer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 8.3 InquiryAnswer
```java
@Entity
public class InquiryAnswer {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    private User admin;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;
}
```

---

## 9. 비즈니스 규칙

1. 문의는 로그인한 사용자만 작성 가능
2. 본인의 문의만 조회/수정/삭제 가능
3. 답변이 완료된 문의는 수정/삭제 불가
4. 문의 목록은 최신순 정렬

---

## 10. 파일 구조

```
backend/src/main/java/com/petpro/domain/inquiry/
├── entity/
│   ├── Inquiry.java
│   ├── InquiryAnswer.java
│   └── InquiryStatus.java
├── repository/
│   ├── InquiryRepository.java
│   └── InquiryAnswerRepository.java
├── service/
│   └── InquiryService.java
├── controller/
│   └── InquiryController.java
└── dto/
    ├── InquiryRequest.java
    ├── InquiryResponse.java
    └── InquiryListResponse.java
```
