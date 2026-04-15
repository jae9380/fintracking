---
name: fintracking project context
description: fintracking 프로젝트의 기술 스택, 구조, 도메인 모델 및 코딩 패턴 요약
type: project
---

## 기본 정보

- 위치: `/Users/jae/Desktop/git/fintracking/back`
- 메인 패키지: `com.ft.back`
- 브랜치 전략: main -> develop -> feature/{name}
- 현재 작업 브랜치: `feature/monolith`

## 기술 스택

- Spring Boot 3.x, Java 17+
- Spring Security (JWT 기반, @AuthenticationPrincipal Long userId)
- JPA/Hibernate, Spring Data JPA
- Lombok (record DTO 선호, @Setter 금지)
- 빌드: Gradle

## 아키텍처

DDD 레이어: domain -> application -> infrastructure -> presentation

모듈별 디자인 패턴:
- auth: Template Method
- account: Factory + Strategy (AES256 암호화)
- transaction: DDD + Event
- budget: Chain of Responsibility (50/80/100% 핸들러)
- notification: Observer (NotificationSender 인터페이스, FCM/Email 구현체)

## 공통 패턴 (반드시 준수)

- 응답: `ApiResponse<T>` record (success/created/noContent/error 팩토리 메서드)
- 예외: `CustomException(ErrorCode)` — 직접 RuntimeException 금지
- Entity: `BaseEntity` 상속 (createdAt, updatedAt 자동 관리)
- Repository 구조: `Port 인터페이스` (application) <- `Impl` <- `JpaRepository` (infrastructure)
- DTO: application 레이어는 record Result, presentation 레이어는 record Response/Request
- 도메인 객체: `@NoArgsConstructor(access = PROTECTED)` + `static 팩토리 메서드`
- 소유자 검증: 도메인 엔티티의 `validateOwner(userId)` 메서드

## 구현 완료 모듈

- auth, account, transaction, budget, notification (2026-04-14)

## notification 모듈 특이사항

- `notification_logs` 테이블에 `is_read` 컬럼 추가 (ERD에는 없었으나 읽음 처리 API 존재)
- `notification_settings`는 별도 엔티티 없이 서비스 레이어에서 인메모리 처리 (포트폴리오 수준)
- Observer: `NotificationSender` List를 생성자 주입 -> Map<NotificationChannel, NotificationSender>로 변환
- BudgetService.checkAlerts()에서 newAlerts 발생 시 NotificationService.send() 직접 호출 (모놀리스이므로 직접 호출 허용)
- Slack은 현재 미구현 (NOTIFICATION_INVALID_WEBHOOK ErrorCode만 추가)
