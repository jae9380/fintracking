# DDD Layers

## Dependency

domain <- application <- infrastructure <- presentation  
No reverse dependency

## Roles

- Domain: entity, VO, domain service, event
- Application: use case, transaction, event publish
- Infrastructure: JPA, external API, Kafka
- Presentation: controller, DTO

## Forbidden

- No direct repo call in service (use application)
- No @Setter in domain (use constructor/factory)
- No domain logic outside domain
