# Module Patterns

## fintracker-auth — Template Method

- Abstract flow in base class
- Email/OAuth2 in subclasses
- Add new auth via subclass only

## fintracker-account — Factory + Strategy

- AccountFactory creates account types
- EncryptionStrategy (AES256/RSA) switch at runtime

## fintracker-transaction — DDD + Event

- Core logic in domain
- Publish event after create
- Handle extra logic (budget/stat) via listeners

## fintracker-budget — Chain of Responsibility

- Handlers by threshold (50/80/100)
- Pass to next if not matched
- Add handler without changing core

## fintracker-notification — Observer

- Receive Kafka event
- Broadcast to all channels
- Add/remove channels via interface

## fintracker-batch — Decorator

- Base executor + decorators
- Add logging/retry/notify independently
