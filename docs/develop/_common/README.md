# 공통 지침 (_common)

## 개요

모든 도메인에 적용되는 공통 규칙과 컨벤션을 정의합니다.
구현 전 반드시 이 지침들을 숙지해야 합니다.

---

## 서브 지침 목록

| 파일 | 설명 |
|------|------|
| [api-convention.md](./api-convention.md) | API 설계 컨벤션 |
| [db-convention.md](./db-convention.md) | 데이터베이스 컨벤션 |
| [error-handling.md](./error-handling.md) | 에러 처리 규칙 |
| [security.md](./security.md) | 보안 규칙 |
| [tdd.md](./tdd.md) | TDD 지침 (테스트 커버리지 100%) |

**테스트 시나리오 미들웨어:**
- [test-scenario](../test-scenario/README.md) | 시나리오 기반 자동 테스트

---

## 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Backend | Java | 21+ |
| Backend | Spring Boot | 3.2+ |
| Frontend | React | 18+ |
| Frontend | TypeScript | 5+ |
| Database | PostgreSQL | 16+ |
| Cache | Redis | 7+ |
| Container | Docker | 24+ |
| Container | Docker Compose | 2.20+ |
| File Storage | MinIO | Latest |
| Reverse Proxy | Nginx | Latest |

---

## 프로젝트 구조

### Backend (Spring Boot)

```
src/main/java/com/petpro/
├── PetProApplication.java
├── common/                      # 공통 모듈
│   ├── config/                  # 설정
│   ├── exception/               # 예외
│   ├── response/                # 응답 포맷
│   └── util/                    # 유틸리티
├── domain/                      # 도메인별 패키지
│   ├── auth/
│   ├── user/
│   ├── company/
│   ├── supplier/
│   └── ...
└── infra/                       # 인프라 (외부 연동)
    ├── persistence/             # JPA Repository
    ├── external/                # 외부 API
    └── messaging/               # 메시지 큐
```

### 도메인 패키지 구조

```
domain/{domain}/
├── controller/                  # API Controller
├── service/                     # 비즈니스 로직
├── repository/                  # Repository Interface
├── entity/                      # JPA Entity
├── dto/                         # DTO (Request/Response)
└── exception/                   # 도메인 예외
```

### Frontend (React)

```
src/
├── App.tsx
├── components/                  # 공통 컴포넌트
├── pages/                       # 페이지
├── hooks/                       # Custom Hooks
├── services/                    # API 호출
├── stores/                      # 상태 관리
├── types/                       # TypeScript 타입
└── utils/                       # 유틸리티
```

---

## 코딩 컨벤션

### Java

- 클래스명: PascalCase
- 메소드/변수명: camelCase
- 상수: UPPER_SNAKE_CASE
- 패키지: lowercase

### TypeScript

- 컴포넌트: PascalCase
- 함수/변수: camelCase
- 상수: UPPER_SNAKE_CASE
- 타입/인터페이스: PascalCase

---

## Git 컨벤션

### 브랜치 명명

```
main                            # 프로덕션
develop                         # 개발
feature/{domain}-{feature}      # 기능 개발
bugfix/{issue-number}-{desc}    # 버그 수정
hotfix/{issue-number}-{desc}    # 긴급 수정
```

### 커밋 메시지

```
{type}({scope}): {subject}

{body}

{footer}
```

**Type:**
- feat: 새 기능
- fix: 버그 수정
- docs: 문서
- style: 포맷팅
- refactor: 리팩토링
- test: 테스트
- chore: 기타

**예시:**
```
feat(supplier): 공급사 상품 등록 API 구현

- POST /api/v1/suppliers/{id}/products 엔드포인트 추가
- 상품 검증 로직 구현
- 재고 초기화 연동

Closes #123
```

---

## 환경 설정

### 환경 변수

```
# Database
DB_MASTER_HOST=localhost
DB_MASTER_PORT=5432
DB_SLAVE1_HOST=localhost
DB_SLAVE1_PORT=5433
DB_SLAVE2_HOST=localhost
DB_SLAVE2_PORT=5434
DB_NAME=petpro
DB_USERNAME=petpro
DB_PASSWORD=your-secure-password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=your-minio-secret-key

# JWT
JWT_SECRET=your-jwt-secret-key-at-least-256-bits
JWT_EXPIRATION=3600
```

---

## 다음 단계

각 서브 지침을 순서대로 읽어주세요:
1. [API 컨벤션](./api-convention.md)
2. [DB 컨벤션](./db-convention.md)
3. [에러 처리](./error-handling.md)
4. [보안](./security.md)
