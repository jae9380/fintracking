# Env & Config Security

## Principles

- Inject sensitive values via env only
- Add `.env` to `.gitignore`

## Config Structure

- application.yml: common (no secrets)
- application-local.yml: local (gitignore)
- application-dev.yml: dev
- application-test.yml: test

## Required Env

- ENCRYPTION_AES_SECRET_KEY: AES key
- JWT_SECRET_KEY: JWT key
- DB_PASSWORD: DB password
- KAFKA_BOOTSTRAP_SERVERS: Kafka broker
- FCM_SERVER_KEY: FCM key

## Forbidden

- No secrets in application.yml
- Do not commit local config or `.env`
- No default values for sensitive data
