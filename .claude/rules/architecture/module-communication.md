# Module Communication

## Principles

- No direct module dependency
- Use Kafka + Zookeeper only
- No sync calls (RestTemplate, Feign)

## Flow

- transaction -> publish event
- budget -> handle aggregation
- notification -> send alerts

## Common Module

- Shared code in fintracker-common only
- No business logic

Allowed:

- exception, response, util, enum

Forbidden:

- service logic, JPA entity, external calls
