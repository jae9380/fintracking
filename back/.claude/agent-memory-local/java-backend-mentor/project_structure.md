---
name: fintracking project structure
description: 재무 추적 포트폴리오 프로젝트의 전체 구조와 도메인 모델 정보
type: project
---

## 프로젝트 개요
- 이름: fintracking (개인 재무 추적 앱)
- 목적: 취업 포트폴리오용 Spring Boot 3.x DDD 프로젝트
- base package: com.ft.back

## 기술 스택
- Spring Boot 3.x
- Spring Security (JWT 기반, @AuthenticationPrincipal Long userId)
- JPA/Hibernate
- 공통 응답: ApiResponse (success, created, noContent 정적 팩토리)
- 공통 예외: CustomException(ErrorCode), @RestControllerAdvice 기반 GlobalExceptionHandler

## 패키지 구조 (DDD Layered)
```
com.ft.back
├── auth/
├── account/
│   ├── domain/Account.java          (validateOwner, deposit, withdraw)
│   └── application/port/AccountRepository.java (findById, save)
├── budget/
├── transaction/
│   ├── domain/
│   │   ├── Transaction.java         (create, validateOwner, updateDescription, updateAmount, updateTransactionDate)
│   │   ├── TransactionType.java     (INCOME, EXPENSE, TRANSFER)
│   │   ├── Category.java            (create, createDefault, validateAccessible)
│   │   └── CategoryType.java        (INCOME, EXPENSE)
│   ├── application/
│   │   ├── port/CategoryRepository.java
│   │   ├── port/TransactionRepository.java
│   │   ├── dto/CreateCategoryCommand.java
│   │   ├── dto/CategoryResult.java
│   │   ├── dto/CreateTransactionCommand.java
│   │   ├── dto/TransactionResult.java
│   │   ├── CategoryService.java
│   │   └── TransactionService.java
│   ├── infrastructure/persistence/
│   │   ├── JpaCategoryRepository.java
│   │   ├── CategoryRepositoryImpl.java
│   │   ├── JpaTransactionRepository.java
│   │   └── TransactionRepositoryImpl.java
│   └── presentation/
│       ├── dto/CreateCategoryRequest.java
│       ├── dto/CategoryResponse.java
│       ├── dto/CreateTransactionRequest.java
│       ├── dto/TransactionResponse.java
│       ├── CategoryController.java  (GET/POST/DELETE /api/v1/categories)
│       └── TransactionController.java (GET/POST/DELETE /api/v1/transactions)
└── common/
    ├── exception/ErrorCode.java     (TRANSACTION_NOT_FOUND, TRANSACTION_NO_ACCESS, ACCOUNT_NOT_FOUND, ACCOUNT_OWNER_MISMATCH)
    ├── exception/CustomException.java
    └── response/ApiResponse.java
```

## ErrorCode 목록 (확인된 것)
- TRANSACTION_NOT_FOUND
- TRANSACTION_NO_ACCESS
- ACCOUNT_NOT_FOUND
- ACCOUNT_OWNER_MISMATCH

## 완료된 레이어
- transaction domain: 완료
- transaction application/infrastructure/presentation: 완료 (2026-04-11)
- account domain + AccountRepository port: 완료
