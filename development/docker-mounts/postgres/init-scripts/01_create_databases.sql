CREATE DATABASE "keycloak";
CREATE DATABASE "identity_db";
CREATE DATABASE "order_db";

CREATE ROLE identity_app WITH LOGIN PASSWORD 'identity_app' CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE identity_db TO identity_app;

CREATE ROLE order_app WITH LOGIN PASSWORD 'order_app' CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE order_db TO order_app;

-- Postgres 15+ no longer grants CREATE on the public schema to every role by
-- default, so without this Liquibase cannot bootstrap its own
-- databasechangelog/databasechangeloglock tables on first run.
\connect identity_db
GRANT ALL ON SCHEMA public TO identity_app;

\connect order_db
GRANT ALL ON SCHEMA public TO order_app;

-- audit-log-service has no relational store - it persists exclusively to Elasticsearch (index oms-audit-logs).
