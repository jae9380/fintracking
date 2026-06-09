# FinTracking — 메트릭 수집 가이드

메트릭 수집은 **수집 방식**에 따라 5가지로 분류됩니다.
Spring Boot Actuator 자동 수집, AOP 어노테이션 기반 수집, 수동 헬퍼 수집으로 구성됩니다.

---

## 수집 구조 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                      fintracking-common                         │
│                                                                 │
│  @Monitored ──────► DomainMetricAspect ──► DomainMetricHelper  │
│  @MonitoredKafka ─► KafkaMetricAspect  ──► KafkaMetricHelper   │
│  (수동) ────────────────────────────────► ExternalApiMetricHelper│
│  (수동) ────────────────────────────────► BatchMetricHelper     │
│                                                                 │
│  모두 MeterRegistry → /actuator/prometheus 로 노출              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Actuator 자동 수집

의존성만으로 코드 변경 없이 자동 수집됩니다.
`spring-boot-starter-actuator` + `micrometer-registry-prometheus` 필요.

### HTTP 요청/응답

| 메트릭 | 설명 |
|---|---|
| `http_server_requests_seconds_count` | 요청 총 횟수 |
| `http_server_requests_seconds_sum` | 응답시간 합계 |
| `http_server_requests_seconds_bucket` | 히스토그램 버킷 (P50/P95/P99 계산용) |

**태그**: `method`, `uri`, `status`, `application`

### JVM 메모리

| 메트릭 | 설명 |
|---|---|
| `jvm_memory_used_bytes` | 실제 사용 중인 메모리 |
| `jvm_memory_committed_bytes` | OS에 예약된 메모리 |
| `jvm_memory_max_bytes` | 최대 허용 메모리 |

**태그**: `area` (heap / nonheap), `id` (Eden Space, Metaspace 등)

### JVM GC

| 메트릭 | 설명 |
|---|---|
| `jvm_gc_pause_seconds_count` | GC 발생 횟수 |
| `jvm_gc_pause_seconds_sum` | GC 총 소요 시간 |

**태그**: `action` (end of minor GC / end of major GC), `cause`

### JVM 스레드

| 메트릭 | 설명 |
|---|---|
| `jvm_threads_live_threads` | 현재 라이브 스레드 수 |
| `jvm_threads_daemon_threads` | 데몬 스레드 수 |
| `jvm_threads_peak_threads` | 최고 스레드 수 (기동 이후 최대치) |

### CPU

| 메트릭 | 설명 |
|---|---|
| `process_cpu_usage` | JVM 프로세스 CPU 사용률 (0~1) |
| `system_cpu_usage` | 전체 시스템 CPU 사용률 (0~1) |

### 클래스 로딩

| 메트릭 | 설명 |
|---|---|
| `jvm_classes_loaded_classes` | 현재 로드된 클래스 수 |
| `jvm_classes_unloaded_classes_total` | 언로드된 클래스 누적 수 |

### DB 커넥션 풀 (Hikari)

`spring-boot-starter-data-jpa` 사용 서비스에서 자동 수집됩니다.

| 메트릭 | 설명 |
|---|---|
| `hikaricp_connections_active` | 현재 사용 중인 커넥션 수 |
| `hikaricp_connections_idle` | 대기 중인 커넥션 수 |
| `hikaricp_connections_pending` | 커넥션 획득 대기 요청 수 |
| `hikaricp_connections_acquire_seconds` | 커넥션 획득 소요 시간 히스토그램 |

> `pending > 0` 이면 DB 커넥션 풀 고갈 — 응답시간 급증의 직접 원인

### 서비스 스크래핑 상태

| 메트릭 | 설명 |
|---|---|
| `up` | Prometheus 스크래핑 성공(1) / 실패(0) |

---

## 2. @Monitored — 서비스 레이어 AOP 수집

`fintracking-common`의 `DomainMetricAspect`가 인터셉트합니다.
메서드 성공/실패 카운터와 실행시간을 자동으로 기록합니다.

### 어노테이션 사용법

```java
@Monitored(domain = "transaction", layer = "service", api = "create")
public TransactionResult create(CreateTransactionCommand command) {
    // ...
}
```

### 수집 메트릭

| 메트릭 | 태그 | 설명 |
|---|---|---|
| `ft_domain_requests_total` | domain, layer, api, result(success/fail) | API별 성공/실패 카운트 |
| `ft_domain_request_duration_seconds` | domain, layer, api | API별 실행시간 히스토그램 (P50/P95/P99) |

### 적용 현황

| 서비스 | 적용 메서드 (api 태그값) |
|---|---|
| **fintracking-auth** | signup, login, oauth2_login, reissue, logout |
| **fintracking-account** | create, find_all, find_by_id, delete |
| **fintracking-transaction** | create, find_all, find_by_id, update, delete |
| **fintracking-budget** | create, find_all, find_by_id, update_amount, delete |
| **fintracking-notification** | find_all, mark_as_read, mark_all_as_read, send, update_settings |

---

## 3. @MonitoredKafka + Publisher 자동 인터셉트 — Kafka 수집

### Consumer — @MonitoredKafka 어노테이션

```java
@MonitoredKafka(topic = KafkaTopic.TRANSACTION_CREATED, action = "consume")
public void handle(TransactionCreatedEvent event) {
    // ...
}
```

### Producer — AbstractEventPublisher 자동 인터셉트

어노테이션 없이 `publish()` 호출 시 `KafkaMetricAspect`가 자동으로 감지합니다.

```java
// 이 클래스를 상속하면 publish() 자동 수집됨
public class TransactionEventPublisher extends AbstractEventPublisher<TransactionCreatedEvent> {
    @Override
    public String topic() { return KafkaTopic.TRANSACTION_CREATED; }
}
```

### 수집 메트릭

| 메트릭 | 태그 | 설명 |
|---|---|---|
| `ft_kafka_events_total` | topic, action(publish/consume), result(success/fail), error_type? | 이벤트 처리 성공/실패 카운트 |
| `ft_kafka_event_duration_seconds` | topic, action | 이벤트 처리 시간 히스토그램 |

### 적용 현황

| 방향 | 서비스 | 토픽 | action |
|---|---|---|---|
| **produce** | fintracking-auth | user.registered | publish |
| **produce** | fintracking-transaction | transaction.created | publish |
| **produce** | fintracking-budget | budget.alert | publish |
| **produce** | fintracking-batch | batch.report | publish |
| **consume** | fintracking-account | transaction.created | consume |
| **consume** | fintracking-budget | transaction.created | consume |
| **consume** | fintracking-notification | budget.alert | consume |
| **consume** | fintracking-batch | user.registered, transaction.created | consume |

---

## 4. ExternalApiMetricHelper — 외부 API 수집

외부 시스템 호출 지점에 직접 주입하여 사용합니다.

### 사용법

```java
@RequiredArgsConstructor
public class KakaoOAuth2Client {

    private final ExternalApiMetricHelper metricHelper;

    public String getAccessToken(String code) {
        Timer.Sample sample = metricHelper.startSample();
        try {
            // 외부 API 호출
            metricHelper.success("kakao", "get_access_token").increment();
            return token;
        } catch (HttpClientErrorException e) {
            metricHelper.fail("kakao", "get_access_token", "HttpClientErrorException").increment();
            throw new CustomException(AUTH_OAUTH2_FAILED);
        } finally {
            sample.stop(metricHelper.timer("kakao", "get_access_token"));
        }
    }
}
```

### 수집 메트릭

| 메트릭 | 태그 | 설명 |
|---|---|---|
| `ft_external_api_requests_total` | system, operation, result(success/fail), error_type? | 외부 API 호출 성공/실패 카운트 |
| `ft_external_api_duration_seconds` | system, operation | 외부 API 응답시간 히스토그램 |

### 적용 현황

| 서비스 | system | operation | 설명 |
|---|---|---|---|
| **fintracking-auth** | kakao | get_access_token | 카카오 액세스 토큰 교환 |
| **fintracking-auth** | kakao | get_user_info | 카카오 유저 정보 조회 |
| **fintracking-notification** | smtp | send_email | Thymeleaf 렌더링 후 이메일 발송 |
| **fintracking-notification** | fcm | send_push | FCM 푸시 알림 발송 |

---

## 5. BatchMetricHelper — 배치 잡 수집

`MetricBatchDecorator`에 주입되어 배치 실행 결과를 기록합니다.

### 사용법

```java
public class MetricBatchDecorator extends AbstractBatchDecorator {

    private final BatchMetricHelper metricHelper;

    @Override
    public void execute(YearMonth yearMonth) {
        long startMs = System.currentTimeMillis();
        String result = "success";
        try {
            delegate.execute(yearMonth);
            metricHelper.success(JOB_NAME);
        } catch (Exception e) {
            result = "fail";
            metricHelper.fail(JOB_NAME, e.getClass().getSimpleName());
            throw e;
        } finally {
            metricHelper.recordDuration(JOB_NAME, result, System.currentTimeMillis() - startMs);
        }
    }
}
```

### 수집 메트릭

| 메트릭 | 태그 | 설명 |
|---|---|---|
| `ft_batch_job_total` | job, result(success/fail), reason? | 배치 성공/실패 카운트 |
| `ft_batch_job_duration_seconds` | job, result | 배치 실행 소요시간 |

### 적용 현황

| 서비스 | job | 설명 |
|---|---|---|
| **fintracking-batch** | monthlyStatisticsJob | 월간 통계 집계 배치 |

---

## 전체 수집 메트릭 목록 요약

| 접두사 | 수집 방식 | 주요 태그 |
|---|---|---|
| `http_server_requests_*` | Actuator 자동 | application, uri, method, status |
| `jvm_memory_*` | Actuator 자동 | application, area, id |
| `jvm_gc_pause_*` | Actuator 자동 | application, action, cause |
| `jvm_threads_*` | Actuator 자동 | application |
| `process_cpu_usage` | Actuator 자동 | application |
| `system_cpu_usage` | Actuator 자동 | application |
| `jvm_classes_*` | Actuator 자동 | application |
| `hikaricp_connections_*` | Actuator 자동 (JPA) | application, pool |
| `resilience4j_circuitbreaker_*` | Actuator 자동 (R4j) | application, name, state |
| `ft_domain_requests_total` | @Monitored AOP | application, domain, layer, api, result |
| `ft_domain_request_duration_seconds` | @Monitored AOP | application, domain, layer, api |
| `ft_kafka_events_total` | @MonitoredKafka AOP | application, topic, action, result, error_type |
| `ft_kafka_event_duration_seconds` | @MonitoredKafka AOP | application, topic, action |
| `ft_external_api_requests_total` | ExternalApiMetricHelper | application, system, operation, result, error_type |
| `ft_external_api_duration_seconds` | ExternalApiMetricHelper | application, system, operation |
| `ft_batch_job_total` | BatchMetricHelper | application, job, result, reason |
| `ft_batch_job_duration_seconds` | BatchMetricHelper | application, job, result |

---

## CommonAutoConfiguration 빈 등록 조건

메트릭 관련 빈은 `MeterRegistry` Bean이 존재할 때만 등록됩니다.
`spring-boot-starter-actuator`가 `MeterRegistry`를 자동 등록하므로 별도 설정 불필요합니다.

```java
@Bean
@ConditionalOnBean(MeterRegistry.class)
public DomainMetricHelper domainMetricHelper(MeterRegistry registry) { ... }

@Bean
@ConditionalOnBean({ MeterRegistry.class, DomainMetricHelper.class })
public DomainMetricAspect domainMetricAspect(...) { ... }
```

> AOP Aspect 활성화를 위해 각 서비스에 `spring-boot-starter-aop` 의존성이 필요합니다.
