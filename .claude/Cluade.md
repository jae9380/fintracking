# FinTracker

MSA personal finance platform  
Java 21, Spring Boot 3.x, Gradle multi-module

## Modules

- fintracker-common: exception, response, util
- fintracker-auth: JWT, OAuth2
- fintracker-account: account, AES-256
- fintracker-transaction: transaction core
- fintracker-budget: budget, limit check
- fintracker-notification: FCM, email, Slack
- fintracker-batch: batch jobs

## Rules

- Architecture: `@rules/architecture/README.md`
- Coding: `@rules/coding/README.md`
- Git: `@rules/git/README.md`
- Security: `@rules/security/README.md`
