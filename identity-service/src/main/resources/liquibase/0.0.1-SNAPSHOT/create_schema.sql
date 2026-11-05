--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE SCHEMA IF NOT EXISTS identity;

--rollback DROP SCHEMA identity;
