# Repository Analysis - JPA Best Practices

## Current State: ❌ NOT Following JPA Best Practices

### Issues Found

#### 1. **Custom Repository Pattern (Anti-Pattern for Spring Data JPA)**

**Current Implementation**:
```java
// Custom interface
public interface PortfolioRepository {
    Portfolio findById(UUID portfolioId);
    void save(Portfolio portfolio);
    // ... more custom methods
}

// Manual implementation with in-memory storage
@Repository
public class PortfolioRepo implements PortfolioRepository {
    private final Map<UUID, Portfolio> portfolioStore = new ConcurrentHashMap<>();

    @Override
    public Portfolio findById(UUID portfolioId) {
        return portfolioStore.get(portfolioId);
    }
    // ... manual implementations
}
```

**Problems**:
- ❌ Manually implementing CRUD operations
- ❌ Using in-memory storage (ConcurrentHashMap) instead of database
- ❌ Not leveraging Spring Data JPA auto-implementation
- ❌ More code to maintain and test
- ❌ No transaction management
- ❌ No database persistence

#### 2. **Missing JpaRepository Extension**

Repositories should extend `JpaRepository<Entity, ID>` which provides:
- Built-in CRUD operations (save, findById, findAll, delete, etc.)
- Pagination and sorting support
- Batch operations
- Query derivation from method names
- @Query annotation support

#### 3. **Improper Relationship Modeling**

The user-portfolio relationship is stored in a separate ConcurrentHashMap instead of using proper JPA relationships:

```java
// Current (Wrong)
private final Map<UUID, Collection<UUID>> userPortfoliosStore = new ConcurrentHashMap<>();
```

Should use:
- @ManyToMany with join table
- Or User entity with @OneToMany relationship

#### 4. **Business Logic in Repository**

`HoldingRepository` has business logic (`refreshMarketPrice`), which should be in the service layer:

```java
// Current (Wrong) - Business logic in repository
public interface HoldingRepository {
    void refreshMarketPrice(String symbol);
    void refreshMarketPrice(UUID portfolioId, String symbol);
}
```

#### 5. **Composite Key Handling**

`Holding` uses `@IdClass`, but the repository doesn't properly handle the composite key:

```java
void delete(UUID portfolioId, String symbol);  // Custom delete
```

Should use:
```java
void deleteById(Holding.HoldingId id);  // JPA standard
```

---

## Recommended Solution: Spring Data JPA Best Practices

### ✅ Proper JPA Repository Pattern

#### 1. PortfolioRepository (Refactored)

```java
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    // Spring Data JPA auto-implements: save, findById, findAll, delete, etc.

    // Custom query methods - Spring auto-implements based on method name
    List<Portfolio> findByName(String name);

    // Complex queries with @Query
    @Query("SELECT p FROM Portfolio p JOIN UserPortfolio up ON p.id = up.portfolioId WHERE up.userId = :userId")
    List<Portfolio> findByUserId(@Param("userId") UUID userId);
}
```

#### 2. HoldingRepository (Refactored)

```java
@Repository
public interface HoldingRepository extends JpaRepository<Holding, Holding.HoldingId> {
    // Query derivation from method names
    Optional<Holding> findBySymbol(String symbol);
    Optional<Holding> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
    List<Holding> findByPortfolioId(UUID portfolioId);

    // Custom delete
    void deleteByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
}
```

#### 3. TransactionRepository (Refactored)

```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // Query derivation
    List<Transaction> findByPortfolioId(UUID portfolioId);
    List<Transaction> findByPortfolioIdAndInstrumentSymbol(UUID portfolioId, String symbol);
    List<Transaction> findByPortfolioIdOrderByTransactionDateDesc(UUID portfolioId);

    // Custom query for complex filtering
    @Query("SELECT t FROM Transaction t WHERE t.portfolioId = :portfolioId AND t.type = :type")
    List<Transaction> findByPortfolioIdAndType(@Param("portfolioId") UUID portfolioId,
                                                @Param("type") String type);
}
```

---

## Required Changes

### 1. Create User-Portfolio Join Table

Since you're tracking which user owns which portfolio, you need either:

**Option A: User Entity with Relationship** (Recommended when implementing authentication)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;

    @OneToMany(mappedBy = "userId")
    private Set<Portfolio> portfolios;
}

@Entity
@Table(name = "portfolios")
public class Portfolio {
    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;  // Foreign key
}
```

**Option B: Join Table** (Current approach until User entity exists)
```java
@Entity
@Table(name = "user_portfolios")
public class UserPortfolio {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    // Unique constraint on (userId, portfolioId)
}
```

### 2. Move Business Logic Out of Repositories

Move `refreshMarketPrice` from `HoldingRepository` to `HoldingService`:

```java
// Before (Wrong)
holdingRepository.refreshMarketPrice(portfolioId, symbol);

// After (Correct)
holdingService.refreshMarketPrice(portfolioId, symbol);
```

### 3. Use Optional<T> for Single Results

```java
// Spring Data JPA convention
Optional<Portfolio> findById(UUID id);
Optional<Holding> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
```

### 4. Use List<T> Instead of Collection<T>

```java
// More specific return type
List<Transaction> findByPortfolioId(UUID portfolioId);
```

---

## Benefits of JPA Repositories

1. ✅ **No Implementation Needed**: Spring auto-implements based on method names
2. ✅ **Database Persistence**: Data survives application restarts
3. ✅ **Transaction Management**: Automatic transaction handling
4. ✅ **Less Code**: No manual CRUD implementation
5. ✅ **Pagination Support**: Built-in pagination and sorting
6. ✅ **Type Safety**: Compile-time checking
7. ✅ **Query Derivation**: Automatic query generation from method names
8. ✅ **Performance**: Optimized database queries

---

## Migration Plan

1. ✅ **Entities Ready**: Portfolio, Holding, Transaction already have JPA annotations
2. ⚠️ **Create User-Portfolio Table**: Need to handle user-portfolio relationship
3. ⚠️ **Refactor Repositories**: Convert to extend JpaRepository
4. ⚠️ **Remove Custom Implementations**: Delete PortfolioRepo, HoldingRepo, TransactionRepo classes
5. ⚠️ **Update Services**: Adjust service layer to use new repository methods
6. ⚠️ **Move Business Logic**: Move refreshMarketPrice to HoldingService
7. ⚠️ **Create Database**: Ensure PortfolioDB exists in PostgreSQL
8. ⚠️ **Test**: Verify all operations work with database

---

## Query Method Naming Conventions

Spring Data JPA derives queries from method names:

| Method Name | Generated Query |
|-------------|----------------|
| `findByName(String name)` | `WHERE name = ?` |
| `findByNameAndType(String name, String type)` | `WHERE name = ? AND type = ?` |
| `findByNameOrType(String name, String type)` | `WHERE name = ? OR type = ?` |
| `findByPortfolioIdOrderByDateDesc(UUID id)` | `WHERE portfolio_id = ? ORDER BY date DESC` |
| `findByQuantityGreaterThan(BigDecimal qty)` | `WHERE quantity > ?` |
| `countByPortfolioId(UUID id)` | `SELECT COUNT(*) WHERE portfolio_id = ?` |
| `deleteByPortfolioIdAndSymbol(UUID id, String s)` | `DELETE WHERE portfolio_id = ? AND symbol = ?` |

---

## Conclusion

**Current State**: Repositories are using in-memory storage with custom implementations (development/testing pattern)

**Required State**: Extend JpaRepository for database persistence with auto-implemented CRUD operations

**Action Needed**: Refactor all repositories to follow Spring Data JPA best practices
