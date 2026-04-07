# Test Rules

## Tools

- Unit: JUnit5 + Mockito
- Integration: Testcontainers

## Unit Test

- Mock external deps (DB, Kafka)
- Name: method_condition_expected

````java
@Test
void createAccount_whenDuplicateName_throwsCustomException() { ... }
``` id="x2s8jd"

## Integration Test
- Use Testcontainers for real deps
- @SpringBootTest + Testcontainers
- Test env in application-test.yml

```java
@Testcontainers
@SpringBootTest
class TransactionIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
}
``` id="k9v3bt"

## Forbidden
- No real external API calls
- No shared state (init with @BeforeEach)
````
