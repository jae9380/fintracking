# Encryption Rules

## Targets

Encrypt sensitive data before storage

- Account No: AES-256 (fintracker-account)
- Card No: AES-256 (fintracker-account)
- Personal Info: AES-256 (fintracker-auth)

## Implementation

- Use `EncryptionStrategy`
- Support AES256 / RSA switch
- Key from env only (`@Value` or `@ConfigurationProperties`)

```java
@Value("${encryption.aes.secret-key}")
private String secretKey;

// Forbidden
private String secretKey = "hardcoded";
```
