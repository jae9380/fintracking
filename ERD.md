```
Auth Service        → users, refresh_tokens
Account Service     → accounts
Transaction Service → transactions, categories
Budget Service      → budgets, budget_alerts
Notification Service→ notification_logs
Batch Service       → monthly_statistics
```

## Auth

```
users
- id (PK)
- email
- password          ← BCrypt 해시
- created_at
- updated_at

refresh_tokens
- id (PK)
- user_id           ← users 참조 (서비스 내 FK)
- token
- expires_at
- created_at
```

## Account

```
accounts
- id (PK)
- user_id           ← 타 서비스 참조 (실제 FK 없음, 논리적 참조)
- account_name      ← 사용자가 붙인 별칭 (예: 카카오뱅크 통장)
- account_number    ← AES-256 암호화 저장
- account_type      ← CHECKING / SAVINGS / CARD
- balance
- created_at
- updated_at
```

## Transaction

```
transactions
- id (PK)
- user_id           ← 논리적 참조
- account_id        ← 논리적 참조
- category_id (FK)  ← categories 참조 (서비스 내 FK)
- type              ← INCOME / EXPENSE / TRANSFER
- amount
- description
- transaction_date
- created_at
- updated_at

categories
- id (PK)
- user_id           ← 논리적 참조 (사용자 정의 카테고리)
- name              ← 식비, 교통, 급여 등
- type              ← INCOME / EXPENSE
- is_default        ← 기본 제공 카테고리 여부
```

## Budget

```
budgets
- id (PK)
- user_id           ← 논리적 참조
- category_id       ← 논리적 참조 (Transaction 서비스 소유)
- year_month        ← 2026-04 형식
- amount            ← 설정 예산
- created_at
- updated_at

budget_alerts
- id (PK)
- budget_id (FK)    ← budgets 참조
- alert_type        ← WARNING_50 / WARNING_80 / EXCEEDED_100
- alerted_at
```

## Notification

```
notification_logs
- id (PK)
- user_id           ← 논리적 참조
- type              ← BUDGET_WARNING / BUDGET_EXCEEDED / MONTHLY_REPORT
- channel           ← FCM / EMAIL
- title
- message
- sent_at
- is_success
```

## Batch

```
monthly_statistics
- id (PK)
- user_id           ← 논리적 참조
- year_month        ← 2026-04
- total_income
- total_expense
- category_id       ← 논리적 참조
- category_expense
- created_at
```
