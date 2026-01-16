# Dependency Injection Summary

This document outlines the dependency injection pattern used throughout the application.

## Dependency Injection Pattern

The application uses **Constructor Injection** via Lombok's `@RequiredArgsConstructor` annotation, which is the recommended approach in modern Spring applications.

### Why Constructor Injection?

1. **Immutability**: Dependencies are final and cannot be changed
2. **Testability**: Easy to mock dependencies in unit tests
3. **Safety**: Prevents NullPointerException - dependencies are guaranteed to be present
4. **Clear Dependencies**: All dependencies are visible in the constructor

## Classes Using Dependency Injection

### ✅ Controllers

| Class | Annotation | Dependencies Injected |
|-------|-----------|----------------------|
| `PortfolioController` | `@RequiredArgsConstructor` | PortfolioService, TransactionService |
| `TransactionController` | `@RequiredArgsConstructor` | TransactionService, TransactionServiceImpl |
| `HoldingController` | `@RequiredArgsConstructor` | HoldingService |

### ✅ Services

| Class | Annotation | Dependencies Injected |
|-------|-----------|----------------------|
| `PortfolioService` | `@RequiredArgsConstructor` | PortfolioRepo, HoldingRepository |
| `TransactionServiceImpl` | `@RequiredArgsConstructor` | TransactionRepository, HoldingRepository, PortfolioRepo |
| `HoldingService` | `@RequiredArgsConstructor` | HoldingRepository |
| `FinnhubClient` | `@RequiredArgsConstructor` | RestTemplate |

**Note**: `FinnhubClient` also uses `@Value` for configuration properties:
- `finnhub.api.key`
- `finnhub.api.base-url`

### ✅ Repositories

| Class | Annotation | Dependencies Injected |
|-------|-----------|----------------------|
| `PortfolioRepo` | `@Repository` | None (uses in-memory ConcurrentHashMap) |
| `HoldingRepo` | `@RequiredArgsConstructor` | FinnhubClient |
| `TransactionRepo` | `@Repository` | None (uses in-memory ConcurrentHashMap) |

**Note**: PortfolioRepo and TransactionRepo don't need `@RequiredArgsConstructor` because they have no dependencies.

### ✅ Configuration Classes

| Class | Annotation | Dependencies Injected |
|-------|-----------|----------------------|
| `DataInitializer` | `@RequiredArgsConstructor` | PortfolioRepo, HoldingRepository |
| `RestTemplateConfig` | `@Configuration` | None (provides beans) |

## Bean Configuration

### RestTemplate Bean

A `RestTemplate` bean is configured in `RestTemplateConfig.java` and injected into `FinnhubClient`:

```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

This allows `FinnhubClient` to use dependency injection instead of creating its own instance.

## Anti-Patterns Avoided

### ❌ Field Injection (Not Recommended)
```java
// DON'T DO THIS
@Service
public class MyService {
    @Autowired
    private MyRepository repository;
}
```

### ✅ Constructor Injection (Recommended)
```java
// DO THIS INSTEAD
@Service
@RequiredArgsConstructor
public class MyService {
    private final MyRepository repository;
}
```

## Migration to JPA Repositories

When migrating from in-memory repositories to JPA repositories, the dependency injection will remain the same:

### Current (In-Memory)
```java
@Repository
public class PortfolioRepo implements PortfolioRepository {
    private final Map<UUID, Portfolio> store = new ConcurrentHashMap<>();
    // ...
}
```

### Future (JPA)
```java
@Repository
public interface PortfolioRepo extends JpaRepository<Portfolio, UUID> {
    // Custom query methods if needed
}
```

The services consuming these repositories won't need any changes because they inject the interface, not the implementation.

## Testing with Dependency Injection

Constructor injection makes testing easy:

```java
@Test
void testPortfolioService() {
    // Mock dependencies
    PortfolioRepo mockRepo = mock(PortfolioRepo.class);
    HoldingRepository mockHoldingRepo = mock(HoldingRepository.class);

    // Create service with mocked dependencies
    PortfolioService service = new PortfolioService(mockRepo, mockHoldingRepo);

    // Test the service
    // ...
}
```

## Summary

✅ **All classes are using proper dependency injection**
✅ **Constructor injection via @RequiredArgsConstructor is used consistently**
✅ **RestTemplate is now properly configured as a Spring bean**
✅ **No @Autowired annotations needed (Lombok handles it)**
✅ **Code is testable and follows Spring best practices**

## Fixed Issues

1. **FinnhubClient**: Previously created its own `RestTemplate` instance. Now properly injects it.
2. **RestTemplateConfig**: Created to provide `RestTemplate` as a Spring bean.

## Next Steps

When implementing user authentication (see TODO.md):
- User details will be injected via Spring Security's `@AuthenticationPrincipal`
- Security context will be available in controllers
- No changes needed to existing dependency injection pattern
