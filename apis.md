# FinTracking API

## Auth Service

| Method |         Endpoint          |  Desc   | Request               | Response                        |
| :----: | :-----------------------: | :-----: | :-------------------- | :------------------------------ |
|  POST  |    /api/v1/auth/signup    | signup  | email, password, name | 201 → userId, email             |
|  POST  |    /api/v1/auth/login     |  login  | email, password       | 200 → accessToken, refreshToken |
|  POST  | /api/v1/auth/oauth2/login | oauth2  | provider, code        | 200 → accessToken, refreshToken |
|  POST  |   /api/v1/auth/reissue    | reissue | Header: refreshToken  | 200 → accessToken               |
|  POST  |    /api/v1/auth/logout    | logout  | Header: accessToken   | 204                             |

---

## Account Service

| Method |       Endpoint        |  Desc  | Request             | Response                                  |
| :----: | :-------------------: | :----: | :------------------ | :---------------------------------------- |
|  POST  |   /api/v1/accounts    | create | type, name, balance | 201 → accountId, type, name, maskedNumber |
|  GET   |   /api/v1/accounts    |  list  | -                   | 200 → account list                        |
|  GET   | /api/v1/accounts/{id} | detail | -                   | 200 → account detail                      |
| DELETE | /api/v1/accounts/{id} | delete | -                   | 204                                       |

---

## Transaction Service

| Method |         Endpoint          |  Desc  | Request                           | Response                  |
| :----: | :-----------------------: | :----: | :-------------------------------- | :------------------------ |
|  POST  |   /api/v1/transactions    | create | accountId, type, amount, category | 201 → transactionId, info |
|  GET   |   /api/v1/transactions    |  list  | query params                      | 200 → content, total      |
|  GET   | /api/v1/transactions/{id} | detail | -                                 | 200 → transaction detail  |
| DELETE | /api/v1/transactions/{id} | delete | -                                 | 204                       |

---

## Notification Service

| Method |            Endpoint             |   Desc   | Request           | Response             |
| :----: | :-----------------------------: | :------: | :---------------- | :------------------- |
|  GET   |      /api/v1/notifications      |   list   | read, page, size  | 200 → content, total |
| PATCH  | /api/v1/notifications/{id}/read | read one | -                 | 200 → id, read       |
| PATCH  | /api/v1/notifications/read-all  | read all | -                 | 204                  |
|  POST  | /api/v1/notifications/settings  | settings | fcm, email, slack | 200 → settings       |

---

## Budget Service

| Method |       Endpoint       |  Desc  | Request                       | Response          |
| :----: | :------------------: | :----: | :---------------------------- | :---------------- |
|  POST  |   /api/v1/budgets    | create | category, amount, year, month | 201 → budget info |
|  GET   |   /api/v1/budgets    |  list  | year, month                   | 200 → budget list |
|  PUT   | /api/v1/budgets/{id} | update | amount                        | 200 → budget info |
| DELETE | /api/v1/budgets/{id} | delete | -                             | 204               |

## Error Codes (Meaning-based)

|   Service    |             Code             | Status |         Message          |
| :----------: | :--------------------------: | :----: | :----------------------: |
|     Auth     |      AUTH_INVALID_TOKEN      |  401   |      invalid token       |
|     Auth     |      AUTH_EXPIRED_TOKEN      |  401   |      expired token       |
|     Auth     |      AUTH_EMAIL_EXISTS       |  409   |       email exists       |
|     Auth     |     AUTH_USER_NOT_FOUND      |  404   |      user not found      |
|   Account    |      ACCOUNT_NOT_FOUND       |  404   |    account not found     |
|   Account    |      ACCOUNT_NO_ACCESS       |  403   |    no account access     |
|   Account    |     ACCOUNT_INVALID_TYPE     |  400   |   invalid account type   |
| Transaction  |    TRANSACTION_NOT_FOUND     |  404   |  transaction not found   |
| Transaction  |    TRANSACTION_NO_ACCESS     |  403   |  no transaction access   |
| Transaction  |   TRANSACTION_INVALID_TYPE   |  400   | invalid transaction type |
| Notification |    NOTIFICATION_NOT_FOUND    |  404   |  notification not found  |
| Notification |    NOTIFICATION_NO_ACCESS    |  403   |  no notification access  |
| Notification | NOTIFICATION_INVALID_WEBHOOK |  400   |  invalid slack webhook   |
|    Budget    |       BUDGET_NOT_FOUND       |  404   |     budget not found     |
|    Budget    |       BUDGET_DUPLICATE       |  409   |     duplicate budget     |
|    Budget    |       BUDGET_NO_ACCESS       |  403   |     no budget access     |
