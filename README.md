# Portfolio Tracker

A Spring Boot REST API for managing investment portfolios with real-time stock tracking, transaction processing, and profit/loss calculations.

## Features

- Multi-portfolio management with buy/sell transactions
- Real-time stock prices via Finnhub API
- Automatic profit calculations (realized & unrealized)
- WebSocket support for live price updates
- PostgreSQL persistence with JPA

## Quick Start

### Prerequisites

- **Java 21** or higher
- **PostgreSQL** database
- **Finnhub API key** - Get free key at [finnhub.io](https://finnhub.io)
- **Maven** (or use included wrapper `./mvnw`)

### 1. Clone the Repository

```bash
git clone https://github.com/anshgandhi17/Portfilio_Tracker_Backend.git
cd Portfilio_Tracker_Backend
```

### 2. Set Up Database

Create a PostgreSQL database:

```bash
# Using psql
createdb PortfolioDB

# Or using SQL
psql -U postgres -c "CREATE DATABASE PortfolioDB;"
```

For detailed database setup, see [DATABASE_SETUP.md](DATABASE_SETUP.md).

### 3. Configure Credentials

**Option A: Using application-local.properties (Recommended for local development)**

```bash
# Copy the example template
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Edit `src/main/resources/application-local.properties` with your credentials:

```properties
finnhub.api.key=your-actual-finnhub-api-key
spring.datasource.url=jdbc:postgresql://localhost:5432/PortfolioDB
spring.datasource.username=postgres
spring.datasource.password=your-db-password
```

**Option B: Using environment variables**

```bash
export FINNHUB_API_KEY=your-actual-api-key
export DB_URL=jdbc:postgresql://localhost:5432/PortfolioDB
export DB_USERNAME=postgres
export DB_PASSWORD=your-db-password
```

See [SECURITY.md](SECURITY.md) for more configuration options.

### 4. Run the Application

```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or if Maven is installed globally
mvn spring-boot:run
```

The API will start at `http://localhost:8080`

### 5. Test the API

**Create a portfolio:**
```bash
curl -X POST http://localhost:8080/api/portfolios \
  -H "Content-Type: application/json" \
  -d '{"name":"My Portfolio","baseCurrency":"USD"}'
```

**Buy stocks:**
```bash
curl -X POST http://localhost:8080/api/transactions/portfolio/{portfolioId} \
  -H "Content-Type: application/json" \
  -d '{
    "instrumentSymbol": "AAPL",
    "type": "BUY",
    "quantity": 10,
    "pricePerUnit": 175.50
  }'
```

**View portfolio summary:**
```bash
curl http://localhost:8080/api/portfolios/{portfolioId}/summary
```

## Project Structure

```
src/main/java/com/ansh/portfilio_tracker/
├── Classes/         # Entities and DTOs
├── Controller/      # REST API endpoints
├── Service/         # Business logic
├── Repo/            # JPA repositories
└── Config/          # Configuration classes
```

## Key API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/portfolios` | Create portfolio |
| `GET` | `/api/portfolios/{id}` | Get portfolio details |
| `GET` | `/api/portfolios/{id}/summary` | Get portfolio performance |
| `GET` | `/api/portfolios/{id}/holdings` | Get all holdings |
| `POST` | `/api/transactions/portfolio/{id}` | Execute BUY/SELL transaction |
| `GET` | `/api/transactions/portfolio/{id}` | Get transaction history |

## Technology Stack

- **Java 21** - Modern Java features
- **Spring Boot 3.5.6** - Application framework
- **PostgreSQL** - Database
- **JPA/Hibernate** - ORM
- **Finnhub API** - Real-time stock data
- **WebSocket** - Live price updates
- **Lombok** - Reduce boilerplate

## Documentation

- [DATABASE_SETUP.md](DATABASE_SETUP.md) - Database setup and schema
- [SECURITY.md](SECURITY.md) - Security configuration guide
- [WEBSOCKET_REALTIME_API.md](WEBSOCKET_REALTIME_API.md) - WebSocket API documentation

## Development

**Run tests:**
```bash
./mvnw test
```

**Build JAR:**
```bash
./mvnw clean package
```

**Run with custom profile:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Important Security Notes

- Never commit `application-local.properties` or `.env` files
- Both are excluded via `.gitignore`
- Rotate API keys if accidentally exposed
- See [SECURITY.md](SECURITY.md) for details

## License

This project is part of a portfolio/learning project.

## Contact

For issues or questions, please open an issue on GitHub.

---

**Built with Spring Boot and Java 21**
