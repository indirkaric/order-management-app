--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE TABLE order_management.orders (
    id int8 GENERATED ALWAYS AS IDENTITY (INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    entity_id varchar NOT NULL,
    order_number varchar NOT NULL,
    customer_id varchar NOT NULL,
    status varchar NOT NULL,
    total_amount numeric(19,2) NOT NULL,
    created_by varchar NOT NULL,
    created_utc timestamp NOT NULL,
    updated_by varchar NULL,
    updated_utc timestamp NULL,
    CONSTRAINT orders_pkey PRIMARY KEY (id),
    CONSTRAINT orders_order_number_key UNIQUE (order_number)
);

CREATE INDEX orders_customer_id_idx ON order_management.orders (customer_id);

CREATE UNIQUE INDEX orders_entity_id_idx ON order_management.orders (entity_id);

--rollback DROP TABLE order_management.orders;
