--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE SCHEMA IF NOT EXISTS order_management;

--rollback DROP SCHEMA order_management;
