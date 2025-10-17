# Portfolio Tracker

A comprehensive investment portfolio management system built with Spring Boot that enables users to track their stock holdings, execute buy/sell transactions, and monitor portfolio performance with real-time market data.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Data Models](#data-models)
- [API Endpoints](#api-endpoints)
- [Business Logic](#business-logic)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Development Notes](#development-notes)

## Overview

Portfolio Tracker is a RESTful API service that allows users to:
- Create and manage multiple investment portfolios
- Track stock holdings with real-time market prices
- Execute buy and sell transactions
- Calculate realized and unrealized profits
- View comprehensive portfolio summaries

The application integrates with the Finnhub API to fetch live stock market data and automatically calculates portfolio metrics including cost basis, current value, and profit/loss.

## Features

### Core Functionality
- **Multi-Portfolio Management**: Create and manage multiple portfolios with different base currencies
- **Transaction Processing**: Execute BUY and SELL transactions with automatic holding updates
- **Real-Time Pricing**: Integration with Finnhub API for live stock market data
- **Profit Calculations**:
  - **Realized Profit**: Calculated when selling stocks (sale price - cost basis)
  - **Unrealized Profit**: Current market value vs. cost basis for active holdings
  - **Total Profit**: Sum of realized and unrealized profits
- **Weighted Average Cost**: Automatically calculates average purchase price when buying more shares
- **Automatic Cleanup**: Holdings with zero quantity are automatically removed after sell transactions

### Advanced Features
- **Portfolio Summary**: Comprehensive view of portfolio performance metrics
- **Transaction History**: Complete audit trail of all buy/sell transactions
- **Symbol-Based Queries**: Retrieve transactions and holdings by stock symbol
- **Market Price Refresh**: Automatic price updates when querying holdings

## Technology Stack

### Backend
- **Java 21**: Modern Java features and performance improvements
- **Spring Boot 3.5.6**: Enterprise-grade application framework
- **Spring Web**: RESTful API development
- **Spring Validation**: Request validation using Jakarta Bean Validation

### Data & Storage
- **In-Memory Storage**: ConcurrentHashMap for thread-safe data management
- **PostgreSQL**: Database support (configured but currently using in-memory)

### External APIs
- **Finnhub API**: Real-time stock market data

### Development Tools
- **Lombok**: Reduces boilerplate code with annotations
- **SLF4J**: Structured logging throughout the application
- **Maven**: Dependency management and build tool

## Project Structure

```
src/main/java/com/ansh/portfilio_tracker/
├── Classes/                          # Data Models (DTOs and Entities)
│   ├── Portfolio.java               # Portfolio entity
│   ├── PortfolioSummary.java        # Portfolio summary DTO
│   ├── Holding.java                 # Stock holding entity
│   ├── Transaction.java             # Transaction entity
│   ├── CreatePortfolioRequest.java  # Portfolio creation DTO
│   ├── CreateHoldingRequest.java    # Holding creation DTO
│   └── FinnhubQuoteResponse.java    # Finnhub API response DTO
│
├── Controller/                       # REST API Controllers
│   ├── PortfolioController.java     # Portfolio endpoints
│   ├── HoldingController.java       # Holdings endpoints
│   └── TransactionController.java   # Transaction endpoints
│
├── Service/                          # Business Logic Layer
│   ├── PortfolioService.java        # Portfolio management
│   ├── HoldingService.java          # Holdings management
│   ├── TransactionService.java      # Transaction interface
│   ├── TransactionServiceImpl.java  # Transaction implementation
│   └── FinnhubClient.java           # External API integration
│
├── Repo/                             # Data Access Layer
│   ├── PortfolioRepository.java     # Portfolio repository interface
│   ├── PortfolioRepo.java           # Portfolio repository implementation
│   ├── HoldingRepository.java       # Holding repository interface
│   ├── HoldingRepo.java             # Holding repository implementation
│   ├── TransactionRepository.java   # Transaction repository interface
│   └── TransactionRepo.java         # Transaction repository implementation
│
├── Config/                           # Configuration Classes
│   └── DataInitializer.java         # Startup data initialization
│
└── PortfilioTrackerApplication.java # Main application entry point

src/main/resources/
└── application.properties            # Application configuration
```

## Architecture

### Layered Architecture

The application follows a clean layered architecture pattern:

1. **Controller Layer**: Handles HTTP requests, validates input, and returns responses
2. **Service Layer**: Contains business logic, calculations, and orchestration
3. **Repository Layer**: Manages data persistence and retrieval
4. **External Services**: Integration with third-party APIs (Finnhub)

### Key Design Decisions

- **In-Memory Storage**: Currently uses `ConcurrentHashMap` for thread-safe in-memory data storage
  - Fast access and simple deployment
  - Data is lost on application restart
  - Suitable for development and testing

- **Repository Pattern**: Abstracts data access logic
  - Interface-based design for easy replacement
  - Ready to swap in-memory storage with database persistence

- **Service Layer Separation**: Clear separation between transaction processing and data access
  - `TransactionServiceImpl` handles complex buy/sell logic
  - Repositories handle simple CRUD operations

## Data Models

### Portfolio
Represents an investment portfolio.

```java
Portfolio {
    UUID id;
    String name;
    String baseCurrency;
    BigDecimal realizedProfitInBaseCurrency;
}
```

### Holding
Represents a stock position in a portfolio.

```java
Holding {
    UUID portfolioId;
    String symbol;              // Stock ticker (e.g., "AAPL")
    String name;                // Company name
    BigDecimal quantity;        // Number of shares
    BigDecimal avgPriceInBaseCurrency;  // Weighted average cost
    BigDecimal marketPrice;     // Current market price (from Finnhub)
    String instrumentCurrency;
    BigDecimal valueInBaseCurrency;     // quantity × marketPrice
    BigDecimal unrealizedProfitInBaseCurrency;  // value - cost
}
```

### Transaction
Represents a buy or sell transaction.

```java
Transaction {
    UUID id;
    UUID portfolioId;
    String instrumentSymbol;
    String type;                // "BUY" or "SELL"
    BigDecimal quantity;
    BigDecimal pricePerUnit;
    String txnCurrency;
    LocalDateTime transactionDate;
    BigDecimal realizedProfit;  // Only for SELL transactions
}
```

### PortfolioSummary
Aggregated portfolio performance metrics.

```java
PortfolioSummary {
    String portfolioId;
    String baseCurrency;
    BigDecimal totalCostInBase;         // Total invested
    BigDecimal totalValueInBase;        // Current market value
    BigDecimal unrealizedProfitInBase;  // Profit on active holdings
    BigDecimal realizedProfitInBase;    // Profit from sold holdings
    BigDecimal totalProfitInBase;       // unrealized + realized
}
```

## API Endpoints

### Portfolio Endpoints

#### Create Portfolio
```http
POST /api/portfolios
Content-Type: application/json

{
  "name": "Tech Portfolio",
  "baseCurrency": "USD"
}
```

#### Get Portfolio by ID
```http
GET /api/portfolios/{portfolioId}
```

#### Get User Portfolios
```http
GET /api/portfolios/user/{userId}
```

#### Get Portfolio Summary
```http
GET /api/portfolios/{portfolioId}/summary

Response:
{
  "portfolioId": "uuid",
  "baseCurrency": "USD",
  "totalCostInBase": 50000.00,
  "totalValueInBase": 58500.00,
  "unrealizedProfitInBase": 8500.00,
  "realizedProfitInBase": 5000.00,
  "totalProfitInBase": 13500.00
}
```

#### Get Portfolio Holdings
```http
GET /api/portfolios/{portfolioId}/holdings
```

#### Get Portfolio Transactions
```http
GET /api/portfolios/{portfolioId}/transactions
```

### Transaction Endpoints

#### Execute Transaction
```http
POST /api/transactions/portfolio/{portfolioId}
Content-Type: application/json

BUY Transaction:
{
  "instrumentSymbol": "AAPL",
  "type": "BUY",
  "quantity": 10,
  "pricePerUnit": 175.50
}

SELL Transaction:
{
  "instrumentSymbol": "AAPL",
  "type": "SELL",
  "quantity": 5,
  "pricePerUnit": 200.00
}
```

#### Get Transaction by ID
```http
GET /api/transactions/{transactionId}
```

#### Get Portfolio Transactions
```http
GET /api/transactions/portfolio/{portfolioId}
```

#### Get Transactions by Symbol
```http
GET /api/transactions/portfolio/{portfolioId}/symbol/{symbol}
```

### Holdings Endpoints

#### Get Holding by Symbol
```http
GET /api/holdings/portfolio/{portfolioId}/symbol/{symbol}
```

#### Refresh Market Price
```http
POST /api/holdings/portfolio/{portfolioId}/symbol/{symbol}/refresh
```

## Business Logic

### Buy Transaction Flow

1. **Receive Transaction**: Validate buy request
2. **Find or Create Holding**: Check if holding exists for symbol
3. **Calculate Weighted Average**:
   ```
   currentValue = existingQuantity × avgPrice
   newValue = newQuantity × newPrice
   totalQuantity = existingQuantity + newQuantity
   newAvgPrice = (currentValue + newValue) / totalQuantity
   ```
4. **Update Holding**: Save updated quantity and average price
5. **Refresh Market Price**: Fetch current price from Finnhub
6. **Save Transaction**: Record transaction in history

### Sell Transaction Flow

1. **Receive Transaction**: Validate sell request
2. **Verify Holding**: Ensure sufficient shares available
3. **Calculate Realized Profit**:
   ```
   costBasis = avgPrice × quantitySold
   saleProceeds = salePrice × quantitySold
   realizedProfit = saleProceeds - costBasis
   ```
4. **Update Portfolio**: Add realized profit to portfolio total
5. **Update Holding**: Reduce quantity
6. **Delete if Zero**: Remove holding if quantity reaches 0
7. **Refresh Market Price**: Update prices for remaining holdings
8. **Save Transaction**: Record transaction with realized profit

### Portfolio Summary Calculation

```
totalCost = Σ(holding.avgPrice × holding.quantity)
totalValue = Σ(holding.marketPrice × holding.quantity)
unrealizedProfit = totalValue - totalCost
realizedProfit = portfolio.realizedProfit (from all sell transactions)
totalProfit = unrealizedProfit + realizedProfit
```

### Market Price Refresh

- Fetches real-time price from Finnhub API
- Calculates current value: `marketPrice × quantity`
- Calculates unrealized profit: `currentValue - costBasis`
- Automatically triggered when:
  - Executing buy/sell transactions
  - Querying portfolio holdings
  - Manual refresh endpoint called

## Setup & Installation

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Finnhub API key (free tier available at https://finnhub.io)
- (Optional) PostgreSQL database

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd "Portfilio Tracker"
   ```

2. **Configure Application Properties**

   Edit `src/main/resources/application.properties`:
   ```properties
   # Application Name
   spring.application.name=Portfilio Tracker

   # Finnhub API Configuration (REQUIRED)
   finnhub.api.key=YOUR_API_KEY_HERE
   finnhub.api.base-url=https://finnhub.io/api/v1

   # Database Configuration (Optional - using in-memory by default)
   spring.datasource.url=jdbc:postgresql://localhost:5432/portfoliomanager
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Build the Project**
   ```bash
   ./mvnw clean install
   ```

4. **Run the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

   The application will start on `http://localhost:8080`

### Initial Data

The application automatically initializes with:
- A default portfolio (ID: `00000000-0000-0000-0000-000000000001`)
- Sample holdings: AAPL, MSFT, GOOGL, TSLA, NVDA

You can modify this in `DataInitializer.java`.

## Configuration

### CORS Configuration

The application enables CORS for `http://localhost:5173` (common frontend dev server port).

To modify CORS settings, update the `@CrossOrigin` annotation in controllers:



### Logging

The application uses SLF4J for logging. Key events logged:
- Transaction execution (buy/sell)
- Market price updates
- Holding creation/deletion
- Portfolio profit updates
- API errors

Adjust logging levels in `application.properties`:
```properties
logging.level.com.ansh.portfilio_tracker=DEBUG
```

## Usage Examples

### Complete Workflow Example

#### 1. Create a Portfolio
```bash
curl -X POST http://localhost:8080/api/portfolios \
  -H "Content-Type: application/json" \
  -d '{"name":"My Portfolio","baseCurrency":"USD"}'
```

#### 2. Buy Some Stocks
```bash
# Buy 50 shares of Apple
curl -X POST http://localhost:8080/api/transactions/portfolio/{portfolioId} \
  -H "Content-Type: application/json" \
  -d '{
    "instrumentSymbol": "AAPL",
    "type": "BUY",
    "quantity": 50,
    "pricePerUnit": 175.50
  }'

# Buy 30 shares of Microsoft
curl -X POST http://localhost:8080/api/transactions/portfolio/{portfolioId} \
  -H "Content-Type: application/json" \
  -d '{
    "instrumentSymbol": "MSFT",
    "type": "BUY",
    "quantity": 30,
    "pricePerUnit": 380.00
  }'
```

#### 3. Check Portfolio Summary
```bash
curl http://localhost:8080/api/portfolios/{portfolioId}/summary
```

#### 4. Sell Some Stocks
```bash
curl -X POST http://localhost:8080/api/transactions/portfolio/{portfolioId} \
  -H "Content-Type: application/json" \
  -d '{
    "instrumentSymbol": "AAPL",
    "type": "SELL",
    "quantity": 10,
    "pricePerUnit": 200.00
  }'
```

#### 5. View Holdings
```bash
curl http://localhost:8080/api/portfolios/{portfolioId}/holdings
```

#### 6. View Transaction History
```bash
curl http://localhost:8080/api/portfolios/{portfolioId}/transactions
```

## Development Notes

### Current State

- **Data Persistence**: Currently using in-memory storage (`ConcurrentHashMap`)
  - All data is lost when the application restarts
  - Suitable for development and demo purposes
  - Can be replaced with database persistence by implementing repository interfaces

- **PostgreSQL Support**: Dependencies are included but not currently active
  - To enable: Implement JPA repositories
  - Database schema can be auto-created with `spring.jpa.hibernate.ddl-auto=update`

### Future Enhancements

- **Database Persistence**: Migrate from in-memory to PostgreSQL
- **User Authentication**: Add Spring Security with JWT
- **Multi-Currency Support**: Currency conversion for international stocks
- **Performance Metrics**: Additional portfolio analytics (Sharpe ratio, beta, etc.)
- **Dividend Tracking**: Record and track dividend income
- **Tax Reporting**: Generate tax reports for capital gains
- **Portfolio Rebalancing**: Suggestions for portfolio optimization
- **WebSocket Updates**: Real-time price updates
- **Batch Processing**: Bulk transaction imports

### Known Limitations

1. **In-Memory Storage**: Data does not persist between restarts
2. **Single Currency**: Currently assumes all prices are in USD
3. **No Authentication**: No user authentication or authorization
4. **API Rate Limits**: Finnhub free tier has rate limits (60 calls/minute)
5. **No Transaction Cancellation**: Transactions cannot be reversed or cancelled
6. **Basic Validation**: Limited business rule validation

### Testing

Run unit tests:
```bash
./mvnw test
```

### Code Quality

- Uses Lombok to reduce boilerplate
- Follows standard Spring Boot project structure
- Comprehensive logging for debugging
- Input validation using Jakarta Bean Validation

## Contributing

When contributing to this project:
1. Follow the existing code structure and naming conventions
2. Add appropriate logging for important operations
3. Include validation for all user inputs
4. Update this README if adding new features
5. Test thoroughly before submitting changes

## License

This project is part of a portfolio/learning project.

## Contact

For questions or support, please open an issue in the repository.

---

**Built with Spring Boot 3.5.6 and Java 21**
