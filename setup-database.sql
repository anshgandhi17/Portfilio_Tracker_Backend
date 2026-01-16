-- Database Setup Script for Portfolio Tracker
-- Run this script to create the database

-- Step 1: Create the database (if it doesn't exist)
-- Note: You need to run this as the postgres superuser

CREATE DATABASE "PortfolioDB"
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'English_United States.1252'
    LC_CTYPE = 'English_United States.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Step 2: Grant privileges
GRANT ALL PRIVILEGES ON DATABASE "PortfolioDB" TO postgres;

-- You can now run the Spring Boot application
-- Hibernate will automatically create the tables based on the entities
