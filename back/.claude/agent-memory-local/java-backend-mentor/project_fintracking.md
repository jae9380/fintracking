---
name: fintracking project overview
description: FinTracker 프로젝트 도메인, 기술스택, 주요 아키텍처 결정 사항
type: project
---

## 프로젝트 개요
- 가계부/자산 추적 백엔드 포트폴리오 프로젝트
- 모놀리식 구조 (feature/monolith 브랜치)
- 향후 MSA 전환 예정 (Kafka 기반)

## 기술 스택
- Java 21, Spring Boot 3.5.13
- JPA/Hibernate (H2 in-memory, create-drop)
- Spring Security + JWT (jjwt 0.12.3)
- Gradle 빌드

## 패키지 구조
- `com.ft.back.{domain}.domain` — Entity, VO, Domain Service
- `com.ft.back.{domain}.application` — Use Case, Handler, Port Interface
- `com.ft.back.{domain}.infrastructure` — JPA, OAuth2, Config
- `com.ft.back.{domain}.presentation` — Controller, DTO

## 아키텍처 패턴
- auth: Template Method (AbstractAuthHandler → EmailAuthHandler, KakaoAuthHandler)
- account: Factory + Strategy (암호화)
- transaction: DDD + Event
- budget: Chain of Responsibility
- notification: Observer (Kafka)

## 주요 공통 컴포넌트
- `CustomException` + `ErrorCode` (HttpStatus + 한글 메시지)
- `ApiResponse<T>` (statusCode, message, resultType, data)
- `BaseEntity` (감사 필드)

## 구현 완료 모듈
- Auth: 이메일 로그인, JWT 발급, Refresh Token Rotation, 카카오 OAuth2
- Account, Transaction, Budget: 기본 CRUD
- Notification: 기본 구조

## 설정 파일
- application.yml: 공개 설정
- application-secret.yml: 시크릿 (gitignore)
- 환경변수: key.jwt, key.aes, KAKAO_CLIENT_ID, KAKAO_REDIRECT_URI
