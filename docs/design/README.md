# FindPlace 시스템 설계 문서

## 개요

FindPlace는 반려동물 장례 서비스 플랫폼입니다. 이 문서는 시스템의 전체 설계를 도메인별로 정리한 기술 문서입니다.

---

## 목차

1. [시스템 아키텍처](#시스템-아키텍처)
2. [도메인 설계 문서](#도메인-설계-문서)
3. [인프라 구성](#인프라-구성)
4. [기술 스택](#기술-스택)

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           FindPlace System                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────────────────┐   │
│  │  Frontend   │     │   Backend   │     │      External APIs      │   │
│  │  (React)    │────▶│(Spring Boot)│────▶│  • Google Maps API      │   │
│  │  Port:3000  │     │  Port:8080  │     │  • 공공데이터포털 API    │   │
│  └─────────────┘     └─────────────┘     └─────────────────────────┘   │
│                             │                                            │
│         ┌───────────────────┼───────────────────┐                       │
│         ▼                   ▼                   ▼                       │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐               │
│  │  Main DB    │     │  Coupon DB  │     │   Redis     │               │
│  │ (PostgreSQL)│     │ (PostgreSQL)│     │   Cache     │               │
│  │  Port:5432  │     │  Port:5435  │     │  Port:6379  │               │
│  │  Master/    │     │             │     │             │               │
│  │  Slave x2   │     │             │     │             │               │
│  └─────────────┘     └─────────────┘     └─────────────┘               │
│                                                                          │
│  ┌─────────────┐     ┌─────────────┐                                    │
│  │   MinIO     │     │   Nginx     │                                    │
│  │  (Storage)  │     │  (Proxy)    │                                    │
│  │ Port:9000/1 │     │  Port:80    │                                    │
│  └─────────────┘     └─────────────┘                                    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 도메인 설계 문서

| 문서 | 설명 |
|------|------|
| [01-user-domain.md](./01-user-domain.md) | 회원 도메인 설계 |
| [02-pet-domain.md](./02-pet-domain.md) | 반려동물 도메인 설계 |
| [03-coupon-domain.md](./03-coupon-domain.md) | 쿠폰 도메인 설계 (별도 DB) |
| [04-funeral-home-domain.md](./04-funeral-home-domain.md) | 장례식장 도메인 설계 |
| [05-region-domain.md](./05-region-domain.md) | 지역 코드 도메인 설계 |
| [06-location-service.md](./06-location-service.md) | 위치 서비스 패키지 설계 |
| [07-admin-user-management.md](./07-admin-user-management.md) | 관리자 회원 관리 설계 |
| [08-customer-mypage.md](./08-customer-mypage.md) | 고객 마이페이지 설계 |
| [09-batch-jobs.md](./09-batch-jobs.md) | 배치잡 설계 |
| [10-database-schema.md](./10-database-schema.md) | 데이터베이스 스키마 |

---

## 인프라 구성

### 데이터베이스 구성

| DB | 용도 | 포트 | 비고 |
|----|------|------|------|
| Main DB (Master) | 메인 데이터 쓰기 | 5432 | PostgreSQL 16 |
| Main DB (Slave1) | 메인 데이터 읽기 | 5433 | Round Robin |
| Main DB (Slave2) | 메인 데이터 읽기 | 5434 | Round Robin |
| Coupon DB | 쿠폰 전용 | 5435 | 마이크로서비스 분리 대비 |

### 외부 API 연동

| API | 용도 | 일일 한도 |
|-----|------|----------|
| Google Maps API | 지도, Geocoding, Places | 과금 기반 |
| 공공데이터포털 (행안부) | 동물장묘업 데이터 | 10,000회 |

---

## 기술 스택

### Backend
- Java 21
- Spring Boot 3.2.5
- Spring Data JPA
- Spring Security + JWT
- Flyway (DB Migration)
- Gradle 8.7

### Frontend
- React 19
- TypeScript
- Material-UI (MUI)
- React Query (TanStack Query)
- Zustand (상태 관리)
- React Hook Form + Zod

### Infrastructure
- PostgreSQL 16 (Master-Slave)
- Redis 7
- MinIO (S3 호환 스토리지)
- Nginx (Reverse Proxy)
- Docker & Docker Compose

---

## 버전 이력

| 버전 | 날짜 | 내용 |
|------|------|------|
| 1.0.0 | 2025-01-25 | 최초 설계 문서 작성 |

---

## 참고 자료

- [프로젝트 README](/README.md)
- [운영 환경 설정 가이드](/docs/troubleshooting/production-setup-guide.md)
