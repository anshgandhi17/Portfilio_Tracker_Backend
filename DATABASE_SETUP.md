# Database Setup Instructions

## The Issue

The application is failing with:
```
FATAL: password authentication failed for user "postgres"
```

This means either:
1. The database `PortfolioDB` doesn't exist
2. The PostgreSQL password is incorrect
3. PostgreSQL authentication configuration needs adjustment

## Solution Steps

### Option 1: Using pgAdmin (Easiest)

1. **Open pgAdmin** (should be installed with PostgreSQL)

2. **Connect to PostgreSQL Server**
   - Right-click on "PostgreSQL 17" (or your version) in the left panel
   - Enter your postgres password when prompted

3. **Create the Database**
   - Right-click on "Databases"
   - Select "Create" > "Database..."
   - Database name: `PortfolioDB`
   - Owner: `postgres`
   - Click "Save"

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

### Option 2: Using Command Line

1. **Open Command Prompt as Administrator**

2. **Navigate to PostgreSQL bin directory**
   ```cmd
   cd "C:\Program Files\PostgreSQL\17\bin"
   ```

3. **Create the database**
   ```cmd
   psql -U postgres -c "CREATE DATABASE \"PortfolioDB\";"
   ```

   When prompted, enter your postgres password: `Qwerty1234!@#$`

4. **Run the application**
   ```bash
   cd "C:\Users\User\IdeaProjects\Portfilio Tracker"
   ./mvnw spring-boot:run
   ```

### Option 3: Using SQL File

1. **Open Command Prompt as Administrator**

2. **Run the setup script**
   ```cmd
   cd "C:\Program Files\PostgreSQL\17\bin"
   psql -U postgres -f "C:\Users\User\IdeaProjects\Portfilio Tracker\setup-database.sql"
   ```

3. **Run the application**

## Troubleshooting

### If password is incorrect:

The password in `application-local.properties` is: `Qwerty1234!@#$`

If this is not your PostgreSQL password:

1. **Option A**: Update `application-local.properties` with the correct password
   ```properties
   spring.datasource.password=your-actual-password
   ```

2. **Option B**: Reset postgres password:
   ```cmd
   cd "C:\Program Files\PostgreSQL\17\bin"
   psql -U postgres
   # Then in psql:
   ALTER USER postgres WITH PASSWORD 'Qwerty1234!@#$';
   ```

### If PostgreSQL is not running:

1. **Check PostgreSQL service**:
   - Press `Win + R`
   - Type `services.msc` and press Enter
   - Look for "postgresql-x64-17" (or your version)
   - If not running, right-click and select "Start"

### Verify Database Creation:

After creating the database, verify it exists:
```cmd
cd "C:\Program Files\PostgreSQL\17\bin"
psql -U postgres -c "\l"
```

You should see `PortfolioDB` in the list.

## What Happens After Database is Created?

Once the database exists and the application connects successfully:

1. **Hibernate will automatically create tables** based on your entities:
   - portfolios
   - holdings
   - transactions
   - user_portfolios

2. **DataInitializer will run** and create:
   - One default portfolio (ID: 00000000-0000-0000-0000-000000000001)
   - 5 tech stock holdings (AAPL, MSFT, GOOGL, TSLA, NVDA)
   - User-portfolio relationship

3. **Application will be ready** at http://localhost:8080

## Database Configuration Files

- `src/main/resources/application.properties` - Main config (no secrets)
- `src/main/resources/application-local.properties` - Local config with actual credentials
- `setup-database.sql` - SQL script to create database
