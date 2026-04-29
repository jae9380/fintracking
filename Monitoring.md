# FinTracking — 모니터링 가이드

Prometheus + Grafana 기반 모니터링 구성입니다.
Prometheus가 Eureka SD로 서비스 인스턴스를 자동 발견하고,
Grafana 대시보드에서 전체 및 서비스별 메트릭을 시각화합니다.

---

## 전체 구성도

```
마이크로서비스 (호스트, 랜덤 포트)
  │  /actuator/prometheus
  │
  ▼
Prometheus (Docker:9090)
  │  Eureka SD → host.docker.internal:8761
  │  15초마다 인스턴스 자동 발견 및 수집
  │
  ▼
Grafana (Docker:3000)
  │  Prometheus 데이터소스 자동 프로비저닝
  │
  ├── Overview Dashboard    : 전체 서비스 현황
  └── Service Detail Dashboard : 서비스 선택 후 심층 분석
```

---

## 실행 방법

```bash
# Docker Compose 기동 (fintracking 디렉토리 기준)
cd fintracking
docker compose up -d

# 각 UI 접속
Prometheus  : http://localhost:9090
Grafana     : http://localhost:3000  (admin / admin)
```

> 마이크로서비스들이 먼저 기동되어 Eureka에 등록된 상태여야 Prometheus가 타겟을 발견합니다.

---

## Prometheus 설정

### 스크래핑 타겟

| job | 방식 | 대상 |
|---|---|---|
| `spring-microservices` | Eureka SD 자동 발견 | auth, account, transaction, budget, notification, batch |
| `infra-services` | static 고정 | discovery(8761), config(8888), gateway(8000) |
| `prometheus` | static 고정 | Prometheus 자기 자신(9090) |

### Eureka SD 릴레이블링 포인트

서비스가 호스트에서 실행되므로 Prometheus(Docker)에서 접근하려면
`__address__`를 `host.docker.internal:port`로 교체합니다.

```yaml
relabel_configs:
  - source_labels: [__meta_eureka_app_instance_port_number]
    replacement: 'host.docker.internal:$1'
    target_label: __address__
  - target_label: __metrics_path__
    replacement: /actuator/prometheus
  - source_labels: [__meta_eureka_app_name]
    target_label: application
```

### Prometheus 타겟 확인

```
http://localhost:9090/targets
```

모든 타겟이 `UP` 상태인지 확인합니다. `DOWN`이면 해당 서비스의
`/actuator/prometheus` 엔드포인트 접근 가능 여부를 확인합니다.

---

## Grafana 대시보드

### 1. Overview Dashboard — 전체 서비스 현황

**접근**: Grafana → Dashboards → FinTracking — 전체 서비스 현황

전체 서비스를 한 화면에서 비교하는 대시보드입니다.
특정 서비스에서 이상 징후를 발견하면 Service Detail Dashboard로 이동합니다.

---

#### 핵심 지표 요약 (Stat 패널)

화면 최상단에 위치하며 서비스 전체의 건강 상태를 즉시 파악할 수 있습니다.

| 패널 | 메트릭 | 해석 기준 |
|---|---|---|
| 전체 RPS | `http_server_requests_seconds_count` | 급격한 증가 → 트래픽 스파이크 / 0 근접 → 서비스 장애 |
| 에러율 (5xx) | `http_server_requests_seconds_count{status=~"5.."}` | 1% 이상 → 즉시 원인 파악 / 10% 이상 → 서비스 포화 |
| P95 응답시간 | `http_server_requests_seconds_bucket` 히스토그램 | 500ms 이하 양호 / 1s 이상 경고 / 3s 이상 위험 |
| 평균 Heap 사용률 | `jvm_memory_used_bytes / jvm_memory_max_bytes` | 80% 초과 지속 → GC 압박, OOM 전조 |
| 전체 활성 스레드 | `jvm_threads_live_threads` | 서비스당 200+ 지속 → 스레드 풀 고갈 임박 |
| UP 서비스 수 | `up{job="spring-microservices"}` | 예상 수보다 적으면 장애 서비스 존재 |

---

#### HTTP 트래픽

| 패널 | 확인 내용 |
|---|---|
| 서비스별 RPS (timeseries) | 트래픽이 특정 서비스에 편중되는지 비교. 부하가 고르게 분산되는지 확인 |
| HTTP 상태 코드 분포 (timeseries) | 2xx/4xx/5xx 비율 추이. 5xx 급증 시점과 서비스를 특정하여 Service Detail로 이동 |

---

#### 응답 시간

| 패널 | 확인 내용 |
|---|---|
| P50 / P95 / P99 전체 (timeseries) | P50은 괜찮지만 P99가 급등 → 간헐적 긴 꼬리 요청 존재. P95와 P99 격차가 크면 스파이크 의심 |

---

#### JVM 리소스

| 패널 | 확인 내용 |
|---|---|
| 서비스별 Heap 사용량 (timeseries) | 지속 증가하면 메모리 릭 의심. GC 후에도 반환 안 되면 위험 |
| 서비스별 CPU 사용률 (timeseries) | 0.8 이상 지속 → CPU 포화. P99 응답시간 악화와 함께 발생 |
| GC 발생 빈도 (timeseries) | 빈도 높을수록 Stop-The-World 증가 → P99 상승. Heap 사용량과 함께 분석 |
| 라이브 스레드 수 (timeseries) | 급등하면 스레드 생성 누수. Deadlock 시 지속 증가 |

---

#### 도메인 & Kafka 메트릭

| 패널 | 확인 내용 |
|---|---|
| 도메인 요청 처리율 (timeseries) | HTTP 에러 없이 도메인 fail만 증가 → try-catch로 삼켜진 비즈니스 예외 |
| Kafka 이벤트 처리율 (timeseries) | consume rate < publish rate → Consumer 처리 지연. consume fail → 이벤트 유실 |

---

#### 서비스 비교 (Bar Chart)

| 패널 | 확인 내용 |
|---|---|
| 서비스별 총 요청 수 비교 | 부하 테스트 결과 어떤 서비스가 가장 많은 요청을 받는지 한눈에 순위 파악 |
| 서비스별 P95 응답시간 비교 | 특정 서비스만 P95가 높으면 해당 서비스가 병목. Service Detail로 이동하여 원인 분석 |

---

### 2. Service Detail Dashboard — 서비스 심층 분석

**접근**: Grafana → Dashboards → FinTracking — 서비스 상세

상단 드롭다운에서 분석할 서비스를 선택합니다.
Overview에서 이상 징후를 발견한 서비스를 선택하여 원인을 특정합니다.

---

#### 핵심 지표 (Stat 8개)

이 8개 패널만 봐도 해당 서비스의 포화 여부를 즉시 판단할 수 있습니다.

| 패널 | 이상 기준 | 조치 방향 |
|---|---|---|
| 서비스 상태 | DOWN | 서비스 기동 확인, /actuator/prometheus 접근 가능 여부 확인 |
| 현재 RPS | 예상보다 낮음 | Gateway 라우팅, 서비스 자체 문제 확인 |
| 에러율 (5xx) | 1% 이상 | Circuit Breaker 상태, Hikari Pending과 함께 확인 |
| P95 응답시간 | 500ms 이상 | URI별 P95, 도메인 API별 P95로 병목 지점 특정 |
| Heap 사용률 | 80% 이상 | GC 후에도 높으면 메모리 릭 의심 |
| CPU 사용률 | 80% 이상 | CPU 바운드 로직 최적화 또는 수평 확장 |
| 활성 DB 커넥션 | 풀 최대치 근접 | max-pool-size 증설 또는 쿼리 최적화 |
| 라이브 스레드 | 200 이상 | 스레드 풀 한계, Deadlock 여부 확인 |

---

#### HTTP 트래픽 상세

| 패널 | 확인 내용 |
|---|---|
| RPS + 에러율 (이중축, timeseries) | 어느 RPS 지점에서 에러가 시작되는지 → 이 값이 서비스의 최대 안전 처리량 |
| HTTP 상태 코드 분포 (timeseries) | 4xx 증가 → 인증/파라미터 문제. 5xx 증가 → 서버 내부 오류. 어느 시점에서 5xx 시작하는지 확인 |
| P50 / P95 / P99 (timeseries) | P50과 P99 격차가 3배 이상 → 간헐적 긴 꼬리 요청 존재 |
| URI별 P95 (timeseries) | 특정 URI만 느리면 해당 API의 도메인 P95와 교차 분석 |

---

#### JVM 상세

| 패널 | 확인 내용 |
|---|---|
| Heap (Used / Committed / Max) | Used가 GC 후에도 줄지 않으면 메모리 릭. Max에 근접 시 OOM 임박 |
| Non-Heap (Metaspace) | 지속 증가 → 동적 클래스 생성 또는 ClassLoader 누수 |
| GC 시간 + 빈도 | Minor GC는 자주·짧게, Major GC는 드물게·길게가 정상. Major GC 빈도 증가 → Heap 증설 신호 |
| 스레드 (Live / Daemon / Peak) | Live가 Peak에 근접 → 스레드 풀 한계 임박. Live 지속 증가 → 스레드 누수 |
| CPU (Process / System) | Process CPU만 높음 → JVM 코드 문제. 둘 다 높음 → 물리 자원 부족 |
| 클래스 로딩 | Loaded가 지속 증가 → ClassLoader 누수 |

---

#### DB 커넥션 풀 (Hikari)

| 패널 | 확인 내용 |
|---|---|
| 커넥션 상태 (Active / Idle / Pending) | **Pending > 0 이면 즉시 DB 커넥션 병목 확정** → 응답시간 급증의 직접 원인 |
| 커넥션 획득 시간 P95 | 수십ms 이하가 정상. 수백ms 이상 → 풀 고갈 또는 DB 자체 응답 지연 |

---

#### Circuit Breaker

| 패널 | 확인 내용 |
|---|---|
| CB 상태 (timeseries) | CLOSED=정상, OPEN=차단 중, HALF_OPEN=복구 시도. OPEN 발생 시 연결된 외부 서비스 점검 |
| CB 호출 성공 / 실패 / 차단 | `not_permitted` 급증 → 사용자가 fallback 응답을 받는 중. 원인 서비스(DB, Kafka, 외부 API) 점검 |

---

#### 커스텀 도메인 메트릭

| 패널 | 확인 내용 |
|---|---|
| API별 성공 / 실패 (timeseries) | HTTP 에러 없이 도메인 fail만 증가 → 내부에서 try-catch로 처리된 비즈니스 예외. fail 급증하는 api 특정 |
| API별 P95 (timeseries) | HTTP P95와 도메인 P95의 차이 → Presentation 레이어 오버헤드 측정. 특정 api만 느리면 해당 메서드의 DB 쿼리·로직 점검 |

---

#### Kafka & 외부 API 메트릭

| 패널 | 확인 내용 |
|---|---|
| Kafka 이벤트 처리 (timeseries) | consume fail 발생 시 `error_type` 태그로 어떤 예외인지 즉시 파악. 재시도 또는 DLQ 확인 |
| 외부 API 호출 성공 / 실패 (timeseries) | system, operation별 실패율 확인. 외부 서비스 장애가 Circuit Breaker로 전파되기 전 선행 감지 가능 |
| 외부 API 응답시간 P95 (timeseries) | 특정 외부 시스템이 느리면 Circuit Breaker 타임아웃 설정 조정 또는 fallback 전략 보완 |

---

## 부하 테스트 시 진단 흐름

부하 테스트 중 문제가 발생하면 다음 순서로 원인을 특정합니다.

```
1. Overview → 전체 RPS / 에러율 / P95 추이 확인
             어느 서비스에서 이상 발생하는지 Bar Chart로 비교
      │
      ▼
2. Service Detail → 해당 서비스 선택
             핵심 지표 8개로 포화 유형 즉시 판단
      │
      ├─ Hikari Pending > 0
      │        → DB 커넥션 병목 확정
      │        → URI별 P95에서 어떤 API가 느린지 확인
      │        → 도메인 API별 P95에서 쿼리가 문제인지 로직이 문제인지 분리
      │
      ├─ CPU > 0.8 + GC 빈도 증가
      │        → CPU+GC 동시 포화
      │        → Heap 사용량이 함께 증가하면 메모리 압박
      │        → Heap 정상이면 CPU 바운드 로직 최적화
      │
      ├─ Circuit Breaker OPEN
      │        → downstream 장애 (DB, Kafka, 외부 API)
      │        → 외부 API P95 확인 → 어떤 외부 시스템이 느린지 특정
      │        → Kafka consume fail → error_type으로 예외 원인 파악
      │
      └─ 도메인 fail 급증 (HTTP 에러는 없음)
               → try-catch로 삼켜진 비즈니스 예외
               → fail 증가하는 api 태그값으로 메서드 특정
               → 해당 메서드 로그 및 코드 점검
```

---

## 설정 파일 위치

```
fintracking/
├── docker-compose.yml                              ← Prometheus, Grafana 컨테이너
└── docker/
    ├── prometheus/
    │   └── prometheus.yml                          ← 스크래핑 타겟 및 Eureka SD 설정
    └── grafana/
        └── provisioning/
            ├── datasources/
            │   └── prometheus.yml                  ← Prometheus 데이터소스 자동 등록
            └── dashboards/
                ├── dashboard.yml                   ← 대시보드 프로비저닝 설정
                ├── overview.json                   ← 전체 서비스 현황 대시보드
                └── service-detail.json             ← 서비스 상세 대시보드
```

---

## config-repo/application.yml 설정

모든 마이크로서비스에 공통 적용됩니다.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
  metrics:
    distribution:
      percentiles-histogram:
        "[http.server.requests]": true      # P50/P95/P99 히스토그램 활성화
      percentiles:
        "[http.server.requests]": 0.5, 0.95, 0.99
```
