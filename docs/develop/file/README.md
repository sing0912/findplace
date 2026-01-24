# 파일 (File)

## 개요

파일 업로드/다운로드를 관리하는 도메인입니다.

---

## 엔티티

### File

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| uploaderId | Long | 업로더 ID (FK) | Not Null |
| originalName | String | 원본 파일명 | Not Null |
| storedName | String | 저장 파일명 | Not Null |
| path | String | 저장 경로 | Not Null |
| url | String | 접근 URL | Not Null |
| size | Long | 파일 크기 (bytes) | Not Null |
| mimeType | String | MIME 타입 | Not Null |
| category | Enum | 파일 카테고리 | Not Null |
| referenceType | String | 참조 유형 | Nullable |
| referenceId | Long | 참조 ID | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### FileCategory

| 값 | 설명 |
|----|------|
| PROFILE | 프로필 이미지 |
| PRODUCT | 상품 이미지 |
| MEMORIAL | 추모관 미디어 |
| POST | 게시글 첨부 |
| DOCUMENT | 문서 |
| ETC | 기타 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/files | 목록 조회 | ADMIN |
| POST | /api/v1/files/upload | 단일 업로드 | 인증된 사용자 |
| POST | /api/v1/files/bulk-upload | 다중 업로드 | 인증된 사용자 |
| GET | /api/v1/files/{id} | 다운로드 | 권한에 따름 |
| DELETE | /api/v1/files/{id} | 삭제 | 본인 또는 ADMIN |
| GET | /api/v1/files/{id}/info | 파일 정보 | 인증된 사용자 |

---

## 저장소 구조

### MinIO (S3 호환)

```
findplace-bucket/
├── profiles/
│   └── {userId}/
│       └── {filename}
├── products/
│   └── {supplierId}/
│       └── {productId}/
│           └── {filename}
├── memorials/
│   └── {memorialId}/
│       └── {filename}
├── posts/
│   └── {postId}/
│       └── {filename}
└── documents/
    └── {category}/
        └── {filename}
```

---

## 업로드 제한

| 카테고리 | 허용 확장자 | 최대 크기 | 최대 개수 |
|----------|-------------|-----------|-----------|
| 프로필 | jpg, png, gif | 5MB | 1 |
| 상품 | jpg, png, gif, webp | 10MB | 10 |
| 추모관 이미지 | jpg, png, gif | 10MB | 50 |
| 추모관 동영상 | mp4, mov | 100MB | 10 |
| 게시글 | jpg, png, pdf, doc, docx | 10MB | 5 |
| 문서 | pdf, doc, docx, xls, xlsx | 20MB | 10 |

---

## 이미지 처리

### 리사이징

| 용도 | 크기 |
|------|------|
| 썸네일 | 200x200 |
| 미리보기 | 600x600 |
| 원본 | 원본 유지 |

### 저장 방식

```
/products/123/456/
├── original.jpg      # 원본
├── preview.jpg       # 미리보기 (600px)
└── thumbnail.jpg     # 썸네일 (200px)
```

---

## 비즈니스 규칙

1. 업로드 시 바이러스 검사 (선택)
2. 이미지는 자동 리사이징
3. 파일명은 UUID로 변경하여 저장
4. 원본 파일명은 DB에 보관
5. 삭제 시 실제 파일도 삭제 (또는 일정 기간 후)

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [upload.md](./upload.md) | 업로드 처리 |
| [download.md](./download.md) | 다운로드 처리 |
| [image.md](./image.md) | 이미지 처리 |
| [storage.md](./storage.md) | 스토리지 설정 |
