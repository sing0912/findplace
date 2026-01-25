# FindPlace 모니터링 가이드

## 1. 개요

FindPlace는 Grafana 기반의 풀스택 관제 시스템을 사용합니다.

| 컴포넌트 | 역할 | 포트 |
|---------|-----|-----|
| **Prometheus** | 메트릭 수집 및 저장 | 9090 |
| **Loki** | 로그 집계 및 저장 | 3100 |
| **Tempo** | 분산 트레이싱 | 3200, 4317, 4318 |
| **Promtail** | 로그 수집 에이전트 | - |
| **Grafana** | 시각화 대시보드 | 3001 |

## 2. 시작하기

### 모니터링 스택 시작

```bash
./scripts/monitoring.sh start
```

### 접속 정보

| 서비스 | URL | 인증 |
|--------|-----|------|
| Grafana | http://localhost:3001 | admin / admin123! |
| Prometheus | http://localhost:9090 | - |

## 3. 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                       Grafana (3001)                        │
│                     시각화 / 대시보드                         │
└───────────┬───────────────┬──────────────────┬──────────────┘
            │               │                  │
            ▼               ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  Prometheus   │  │     Loki      │  │    Tempo      │
│   (9090)      │  │    (3100)     │  │   (3200)      │
│   메트릭      │  │    로그       │  │   트레이싱    │
└───────┬───────┘  └───────┬───────┘  └───────┬───────┘
        │                  │                  │
        │                  ▼                  │
        │          ┌───────────────┐          │
        │          │   Promtail    │          │
        │          │  로그 수집    │          │
        │          └───────┬───────┘          │
        │                  │                  │
        ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend (8080)                  │
│  /api/actuator/prometheus    logback-loki    OTLP exporter │
└─────────────────────────────────────────────────────────────┘
```

## 4. 메트릭 (Prometheus)

### 수집되는 메트릭

백엔드 애플리케이션에서 자동 수집:

| 메트릭 카테고리 | 설명 |
|----------------|------|
| `http_server_requests_*` | HTTP 요청 (응답시간, 횟수, 상태코드) |
| `jvm_memory_*` | JVM 메모리 사용량 |
| `jvm_gc_*` | 가비지 컬렉션 |
| `jvm_threads_*` | JVM 스레드 |
| `hikaricp_*` | 데이터베이스 커넥션 풀 |
| `spring_data_repository_*` | JPA 레포지토리 |

### Prometheus 쿼리 예시

```promql
# 평균 응답 시간
rate(http_server_requests_seconds_sum{application="findplace-backend"}[5m])
/ rate(http_server_requests_seconds_count{application="findplace-backend"}[5m])

# 요청 처리량 (RPS)
sum(rate(http_server_requests_seconds_count{application="findplace-backend"}[5m]))

# JVM Heap 사용률
sum(jvm_memory_used_bytes{application="findplace-backend",area="heap"})
/ sum(jvm_memory_max_bytes{application="findplace-backend",area="heap"}) * 100

# 에러율
sum(rate(http_server_requests_seconds_count{application="findplace-backend",status=~"5.."}[5m]))
/ sum(rate(http_server_requests_seconds_count{application="findplace-backend"}[5m])) * 100
```

## 5. 로그 (Loki)

### 로그 수집 방식

1. **로컬 개발 (local profile)**: 파일로만 저장
2. **개발/운영 (dev/prod profile)**: Loki로 직접 전송 + 파일 저장

### LogQL 쿼리 예시

```logql
# 특정 서비스 로그
{job="findplace-backend"}

# 에러 로그만
{job="findplace-backend"} |= "ERROR"

# TraceID로 검색
{job="findplace-backend"} |= "traceId=abc123"

# JSON 파싱 후 필터
{job="findplace-backend"} | json | level="ERROR"
```

### 로그 파일 위치

- `logs/findplace-backend.log`: 현재 로그
- `logs/findplace-backend.YYYY-MM-DD.*.log`: 롤링된 로그

## 6. 트레이싱 (Tempo)

### 트레이싱 활성화

`management.tracing.sampling.probability`로 샘플링 비율 조정:

| 환경 | 비율 | 설명 |
|-----|-----|------|
| local | 1.0 | 모든 요청 추적 |
| dev | 0.5 | 50% 샘플링 |
| prod | 0.1 | 10% 샘플링 |

### TraceID 확인

모든 로그에 traceId 포함:
```
2024-01-15 10:30:45.123 [http-nio-8080-exec-1] [abc123def456] INFO ...
```

## 7. Grafana 대시보드

### 사전 구성된 대시보드

| 대시보드 | 설명 |
|---------|------|
| FindPlace Overview | 전체 시스템 개요 |

### 대시보드 구성

#### Overview 섹션
- API 평균 응답 시간
- 요청 처리량 (RPS)
- JVM Heap 사용률
- 애플리케이션 상태

#### HTTP Requests 섹션
- 엔드포인트별 요청 비율
- 엔드포인트별 응답 시간

#### Logs 섹션
- 실시간 애플리케이션 로그

## 8. 스크립트 사용법

### monitoring.sh

```bash
# 모니터링 스택 시작
./scripts/monitoring.sh start

# 모니터링 스택 중지
./scripts/monitoring.sh stop

# 상태 확인
./scripts/monitoring.sh status

# 로그 확인
./scripts/monitoring.sh logs
./scripts/monitoring.sh logs grafana
```

## 9. 설정 파일 위치

| 파일 | 설명 |
|-----|------|
| `docker/prometheus/prometheus.yml` | Prometheus 스크래핑 설정 |
| `docker/loki/loki-config.yml` | Loki 저장소 설정 |
| `docker/promtail/promtail-config.yml` | Promtail 수집 설정 |
| `docker/tempo/tempo-config.yml` | Tempo 트레이싱 설정 |
| `docker/grafana/provisioning/` | Grafana 자동 프로비저닝 |
| `docker/grafana/dashboards/` | 대시보드 JSON |

## 10. 트러블슈팅

### Prometheus가 메트릭을 수집하지 않음

1. 백엔드 actuator 엔드포인트 확인:
   ```bash
   curl http://localhost:8080/api/actuator/prometheus
   ```

2. Prometheus targets 상태 확인:
   - http://localhost:9090/targets

### Loki에 로그가 없음

1. 프로파일 확인 (local이면 Loki로 안 보냄)
2. Promtail 상태 확인:
   ```bash
   docker compose logs promtail
   ```

### Grafana 대시보드가 비어있음

1. 데이터소스 연결 확인:
   - Grafana > Configuration > Data Sources
2. 쿼리 직접 테스트:
   - Grafana > Explore

### 서비스가 시작되지 않음

```bash
# 전체 로그 확인
docker compose logs -f prometheus loki tempo grafana

# 개별 서비스 재시작
docker compose restart prometheus
```

## 11. 보안 주의사항

**운영 환경에서 반드시 변경:**

1. Grafana 관리자 비밀번호 (`GF_SECURITY_ADMIN_PASSWORD`)
2. Prometheus 접근 제한 (내부 네트워크만)
3. 민감 정보 로그 마스킹
4. TLS 활성화
