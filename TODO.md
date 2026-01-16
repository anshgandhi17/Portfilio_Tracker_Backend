# TODO List

## High Priority

### 1. User Authentication & Authorization
**Status**: Not Started
**Priority**: High
**Description**: Implement proper user authentication and authorization system

#### Tasks:
- [ ] Add Spring Security dependency to `pom.xml`
- [ ] Create `User` entity with JPA annotations
  - Fields: id, username, email, password (hashed), createdAt, updatedAt
  - Add proper indexes
- [ ] Create `UserRepository` interface extending JpaRepository
- [ ] Implement JWT-based authentication
  - JWT token generation
  - Token validation
  - Token refresh mechanism
- [ ] Create authentication endpoints
  - `POST /api/auth/register` - User registration
  - `POST /api/auth/login` - User login
  - `POST /api/auth/refresh` - Refresh JWT token
  - `POST /api/auth/logout` - User logout
- [ ] Add security configuration
  - Configure password encoder (BCrypt)
  - Define secured and public endpoints
  - Add CORS configuration for production
- [ ] Update PortfolioController
  - Remove `DEFAULT_USER_ID` constant
  - Get user ID from security context (authenticated user)
  - Add `/api/portfolios/my-portfolios` endpoint
  - Secure portfolio operations to ensure users can only access their own portfolios
- [ ] Add authorization checks
  - Users can only view/modify their own portfolios
  - Users can only view/modify holdings in their portfolios
  - Users can only view/modify transactions in their portfolios
- [ ] Update DataInitializer
  - Create default admin user
  - Associate sample portfolios with the admin user
- [ ] Add user profile management
  - `GET /api/users/profile` - Get current user profile
  - `PUT /api/users/profile` - Update user profile
  - `PUT /api/users/password` - Change password

#### Code Locations:
- `PortfolioController.java:22-24` - Remove DEFAULT_USER_ID constant
- `PortfolioController.java:33-35` - Replace with security context user
- `PortfolioController.java:45-51` - Implement /my-portfolios endpoint

#### References:
- Spring Security Documentation: https://spring.io/projects/spring-security
- JWT Implementation Guide: https://jwt.io/

---

## Medium Priority

### 2. Database Migration from In-Memory to PostgreSQL
**Status**: Partially Complete (JPA entities added)
**Priority**: Medium
**Description**: Fully migrate from in-memory storage to PostgreSQL

#### Tasks:
- [x] Add JPA annotations to entity classes (Portfolio, Holding, Transaction)
- [x] Add `spring-boot-starter-data-jpa` dependency
- [x] Configure PostgreSQL connection in `application.properties`
- [ ] Create database `PortfolioDB` in PostgreSQL
- [ ] Replace in-memory repositories with JPA repositories
  - [ ] `PortfolioRepo` -> JPA Repository
  - [ ] `HoldingRepo` -> JPA Repository
  - [ ] `TransactionRepo` -> JPA Repository
- [ ] Test all CRUD operations with database
- [ ] Add database migration tool (Flyway or Liquibase)
- [ ] Create initial migration scripts
- [ ] Add database indexes for performance
  - Index on `portfolios.id`
  - Composite index on `holdings(portfolio_id, symbol)`
  - Index on `transactions.portfolio_id`
  - Index on `transactions.transaction_date`

---

### 3. Enhanced API Documentation
**Status**: Not Started
**Priority**: Medium
**Description**: Add comprehensive API documentation

#### Tasks:
- [ ] Add Springdoc OpenAPI dependency
- [ ] Add Swagger annotations to controllers
- [ ] Configure Swagger UI
- [ ] Add API examples and descriptions
- [ ] Document error responses
- [ ] Add authentication documentation

---

## Low Priority

### 4. Additional Features
**Status**: Not Started
**Priority**: Low

#### Tasks:
- [ ] Add portfolio sharing functionality
- [ ] Implement watchlist feature
- [ ] Add price alerts
- [ ] Add dividend tracking
- [ ] Add export functionality (CSV, PDF)
- [ ] Add portfolio performance charts/analytics
- [ ] Support multiple currencies with conversion
- [ ] Add tax reporting features
- [ ] Implement portfolio rebalancing suggestions

---

### 5. Testing
**Status**: Not Started
**Priority**: Medium

#### Tasks:
- [ ] Add unit tests for services
- [ ] Add integration tests for controllers
- [ ] Add repository tests
- [ ] Add security tests
- [ ] Add end-to-end tests
- [ ] Set up test coverage reporting
- [ ] Achieve minimum 80% code coverage

---

### 6. DevOps & Deployment
**Status**: Not Started
**Priority**: Low

#### Tasks:
- [ ] Create Dockerfile
- [ ] Create docker-compose.yml for local development
- [ ] Set up CI/CD pipeline (GitHub Actions)
- [ ] Configure production application.properties
- [ ] Add health check endpoints
- [ ] Add monitoring and logging (Prometheus, Grafana)
- [ ] Deploy to cloud platform (AWS, Azure, or GCP)

---

## Technical Debt

- [ ] Fix Java version inconsistency (currently downgraded to 17 from 21)
- [ ] Remove hardcoded API key from `application.properties` (use environment variables)
- [ ] Add proper exception handling with custom exceptions
- [ ] Add request/response DTOs to separate API contract from entities
- [ ] Implement API versioning
- [ ] Add rate limiting
- [ ] Add request validation error messages improvement

---

## Notes

### Default User Information
- **User ID**: `00000000-0000-0000-0000-000000000001`
- **Portfolio ID**: `00000000-0000-0000-0000-000000000001`
- This is temporary until authentication is implemented

### Database Information
- **Database Name**: PortfolioDB
- **Username**: postgres
- **Port**: 5432
- **Tables**: portfolios, holdings, transactions

### API Keys
- **Finnhub API**: Configured in application.properties (TODO: Move to environment variables)
