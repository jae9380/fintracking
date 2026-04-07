# Architecture Rules

Define core architecture principles for FinTracker

## Core

- DDD layers: domain -> application -> infrastructure -> presentation
- No direct module dependency, use fintracker-common only
- Use Kafka + Zookeeper for service communication

## References

- DDD layers, roles: `@rules/architecture/ddd-layer.md`
- Module patterns: `@rules/architecture/module-patterns.md`
- Communication, Kafka flow: `@rules/architecture/module-communication.md`
