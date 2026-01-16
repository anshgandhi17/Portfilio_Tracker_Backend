@echo off
echo Testing PostgreSQL connection...
echo.
echo Attempting to connect to PortfolioDB with current credentials...
echo.

set PGPASSWORD=Qwerty1234!@#$
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d PortfolioDB -c "SELECT version();"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================
    echo SUCCESS! Connection works.
    echo ======================================
    echo.
    echo The database connection is working.
    echo The issue might be with Spring Boot configuration.
) else (
    echo.
    echo ======================================
    echo FAILED! Password is incorrect.
    echo ======================================
    echo.
    echo Please update the password in:
    echo src\main\resources\application-local.properties
    echo.
    echo To find your correct password:
    echo 1. Check how you logged into pgAdmin
    echo 2. Or reset it using pgAdmin: Right-click PostgreSQL server ^> Properties ^> Definition ^> Password
)

pause
