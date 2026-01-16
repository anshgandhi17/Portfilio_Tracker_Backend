# Security Configuration Guide

## Sensitive Information Management

This project uses environment variables and local configuration files to manage sensitive information like API keys and database credentials.

### Setup Instructions

#### Option 1: Using application-local.properties (Recommended for development)

1. Copy the example file:
   ```bash
   cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
   ```

2. Edit `application-local.properties` with your actual credentials:
   ```properties
   finnhub.api.key=your-actual-api-key
   spring.datasource.username=your-db-username
   spring.datasource.password=your-db-password
   ```

3. Spring Boot will automatically load `application-local.properties` and override values from `application.properties`

#### Option 2: Using Environment Variables (Recommended for production)

Set the following environment variables:

```bash
export FINNHUB_API_KEY=your-actual-api-key
export DB_URL=jdbc:postgresql://localhost:5432/PortfolioDB
export DB_USERNAME=your-db-username
export DB_PASSWORD=your-db-password
```

Or use a `.env` file with a tool like `dotenv`:

1. Copy the example file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your actual credentials

### Files Excluded from Git

The following files containing sensitive information are excluded via `.gitignore`:

- `**/application-local.properties` - Local Spring Boot configuration
- `.env` - Environment variables file

### Important Security Notes

1. **Never commit sensitive credentials to git**
2. **Rotate credentials** if they were accidentally committed
3. **Use different credentials** for development, staging, and production environments
4. The `application.properties` file in git now only contains placeholders and default values

### Getting API Keys

- **Finnhub API Key**: Sign up at https://finnhub.io/ to get a free API key

### Credential Rotation

If credentials were previously committed to git history:

1. **Rotate all exposed credentials immediately**:
   - Get a new Finnhub API key
   - Change your database password

2. **Remove from git history** (optional, advanced):
   ```bash
   # Using git-filter-repo (recommended)
   git filter-repo --path src/main/resources/application.properties --invert-paths

   # Or use BFG Repo-Cleaner
   bfg --delete-files application.properties
   ```

3. **Force push** (only if you haven't shared the repository):
   ```bash
   git push --force
   ```

### Production Deployment

For production environments:

1. Use a secrets management service (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
2. Set environment variables through your hosting platform
3. Never store credentials in configuration files
4. Enable encryption at rest for sensitive data
5. Use IAM roles or managed identities when possible
