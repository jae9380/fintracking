# Exception Rules

## Pattern

Use `CustomException` + `ErrorCode`

## ErrorCode

````java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_TOKEN(401, "AUTH_001", "Invalid token"),
    EXPIRED_TOKEN(401, "AUTH_002", "Expired token"),

    ACCOUNT_NOT_FOUND(404, "ACCOUNT_001", "Account not found"),
    TRANSACTION_NOT_FOUND(404, "TRANSACTION_001", "Transaction not found");

    private final int status;
    private final String code;
    private final String message;
}
``` id="u7k2qp"

## CustomException
```java
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
``` id="z3m8vx"

## Forbidden
- No direct RuntimeException
- No hardcoded messages
````
