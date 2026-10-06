-- init-db.sql
-- Runs automatically when the PostgreSQL container first starts.
-- Creates all databases needed by 360 Church Flow microservices.

CREATE DATABASE church360_members;
-- CREATE DATABASE church360_auth;
-- CREATE DATABASE church360_attendance;
-- CREATE DATABASE church360_pastoral;
-- CREATE DATABASE church360_finance;

-- Grant full access to the application user
GRANT ALL PRIVILEGES ON DATABASE church360_members    TO churchflow360;
-- GRANT ALL PRIVILEGES ON DATABASE church360_auth       TO churchflow360;
-- GRANT ALL PRIVILEGES ON DATABASE church360_attendance TO churchflow360;
-- GRANT ALL PRIVILEGES ON DATABASE church360_pastoral   TO churchflow360;
-- GRANT ALL PRIVILEGES ON DATABASE church360_finance    TO churchflow360;