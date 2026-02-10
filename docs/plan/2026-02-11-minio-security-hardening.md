# MinIO 보안 강화 — 수동 조치 항목

## 배경

2026-02-11 MinIO 파일 서빙 보안 감사에서 발견된 취약점 중, 코드 수정으로 해결된 항목 외에 **프로덕션 서버에서 수동 조치가 필요한 항목**을 정리합니다.

## 코드로 이미 수정 완료된 항목

| # | 취약점 | 수정 내용 | 파일 |
|---|--------|----------|------|
| C-2 | 보안 헤더 누락 (add_header 상속 버그) | location 블록에 보안 헤더 명시적 추가 | `ssl.conf` |
| H-1 | 파일 다운로드 Rate Limit 없음 | `limit_req zone=api_limit burst=20` 추가 | `ssl.conf` |
| H-2 | Path Traversal 가능성 | `..` 포함 요청 차단 + rewrite 보안 | `ssl.conf` |
| H-3 | 임의 파일 삭제 (objectKey 미검증) | `startsWith("pets/")` + `!contains("..")` 검증 | `PetImageService.java` |
| L-3 | MinIO `cap_drop` 누락 | `cap_drop: ALL` 추가 | `docker-compose.prod.yml` |
| — | `minio-init` 특수문자 처리 | 환경변수 따옴표 감싸기 + `|| true` 처리 | `docker-compose.yml` |

---

## 수동 조치 필요 항목

### 1. [CRITICAL] MinIO 자격 증명 변경

**현상:** 프로덕션 MinIO가 `minioadmin` / `minioadmin123!`로 운영 중
**위험:** MinIO 포트 노출 시 전체 파일 시스템 탈취 가능

**조치 방법:**

```bash
# 1. 강력한 비밀번호 생성
NEW_ACCESS_KEY="petpro-minio-$(openssl rand -hex 8)"
NEW_SECRET_KEY="$(openssl rand -base64 32)"

# 2. 프로덕션 서버의 .env 파일 수정
MINIO_ACCESS_KEY=$NEW_ACCESS_KEY
MINIO_SECRET_KEY=$NEW_SECRET_KEY

# 3. 서비스 재시작 (MinIO + Backend + minio-init)
docker-compose down
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 4. minio-init 로그 확인 (정책 재적용 확인)
docker logs petpro-minio-init
# "Access permission ... set to download" 메시지 확인

# 5. 이미지 접근 테스트
curl -I https://dev.findplace.co.kr/files/petpro/pets/1/profile_*.png
# 200 OK 확인
```

**주의사항:**
- Backend의 `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY` 환경변수도 동일하게 변경
- `application.yml`의 기본값 `minioadmin` 제거 (기본값 없이 환경변수 필수화)

---

### 2. [MEDIUM] MinIO 이미지 버전 업그레이드

**현상:** `minio/minio:RELEASE.2022-10-24T18-35-07Z` (3년 이상 경과)
**위험:** 알려진 CVE 취약점 존재 가능

**조치 방법:**

```bash
# 1. 현재 이미지 취약점 스캔
trivy image minio/minio:RELEASE.2022-10-24T18-35-07Z

# 2. 최신 안정 버전 확인
# https://github.com/minio/minio/releases

# 3. docker-compose.yml 이미지 태그 업데이트
# minio/minio:RELEASE.2022-10-24T18-35-07Z → 최신 버전
# minio/mc:RELEASE.2022-10-29T10-09-23Z → 호환 버전

# 4. 로컬에서 테스트 후 프로덕션 적용
docker-compose pull minio minio-init
docker-compose up -d minio
docker-compose run --rm minio-init
```

**주의사항:**
- 업그레이드 전 MinIO 데이터 백업 필수
- `mc anonymous` 명령어 호환성 확인 (최신 mc에서는 `mc anonymous` → `mc policy` 변경 가능)

---

### 3. [MEDIUM] Docker 네트워크 분리

**현상:** 모든 서비스가 `petpro-network` 단일 네트워크 공유
**위험:** Grafana/Prometheus 등 모니터링 컨테이너 침해 시 MinIO/DB 직접 접근 가능

**조치 방법:**

`docker-compose.yml`에 네트워크 분리 적용:

```yaml
networks:
  app-network:        # nginx, backend, frontend
  data-network:       # postgres, redis, minio, backend
  monitoring-network: # prometheus, grafana, loki, promtail, tempo

services:
  nginx:
    networks: [app-network]
  backend:
    networks: [app-network, data-network]
  frontend:
    networks: [app-network]
  postgres-master:
    networks: [data-network]
  redis:
    networks: [data-network]
  minio:
    networks: [data-network, app-network]  # nginx에서 프록시 접근 필요
  grafana:
    networks: [monitoring-network]
  prometheus:
    networks: [monitoring-network]
```

**주의사항:**
- `minio`는 `app-network`에도 포함해야 nginx에서 프록시 가능
- Backend의 모니터링 메트릭 노출을 위한 별도 설정 필요
- 변경 후 모든 서비스 간 통신 테스트 필수

---

### 4. [LOW] `application.yml` MinIO 기본값 제거

**현상:** `access-key` 기본값이 `minioadmin`으로 설정됨
**위험:** 환경변수 미설정 시 약한 자격 증명으로 연결

**조치 방법:**

```yaml
# 변경 전
app:
  minio:
    access-key: ${MINIO_ACCESS_KEY:minioadmin}

# 변경 후 (기본값 제거 → 미설정 시 앱 시작 실패)
app:
  minio:
    access-key: ${MINIO_ACCESS_KEY}
```

---

### 5. [LOW] 미사용 S3Presigner Bean 제거

**현상:** `MinioConfig.java`에 `S3Presigner` Bean이 생성되지만 사용되지 않음
**위험:** 불필요한 자격 증명 메모리 보유

**조치:** 현재 Presigned URL을 사용하지 않으면 제거, 향후 `sitters/chat/care` 경로에 인증된 파일 접근 시 활용 예정이면 유지

---

## 조치 우선순위

| 순서 | 항목 | 심각도 | 예상 소요 |
|------|------|--------|-----------|
| 1 | MinIO 자격 증명 변경 | CRITICAL | 30분 |
| 2 | MinIO 이미지 업그레이드 | MEDIUM | 1시간 (테스트 포함) |
| 3 | Docker 네트워크 분리 | MEDIUM | 2시간 (테스트 포함) |
| 4 | application.yml 기본값 제거 | LOW | 5분 |
| 5 | S3Presigner Bean 정리 | LOW | 5분 |

---

## 완료 기준

- [ ] MinIO 자격 증명이 강력한 랜덤 비밀번호로 변경됨
- [ ] MinIO 이미지가 최신 안정 버전으로 업데이트됨
- [ ] Docker 네트워크가 서비스 그룹별로 분리됨
- [ ] `application.yml`에서 `minioadmin` 기본값이 제거됨
- [ ] 모든 조치 후 이미지 접근 테스트 통과 (200 OK)
- [ ] `docs/develop/_common/security.md` 체크리스트와 일치 확인
