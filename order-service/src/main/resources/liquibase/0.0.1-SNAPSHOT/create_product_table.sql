--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE TABLE order_management.product (
    id int8 GENERATED ALWAYS AS IDENTITY (INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    entity_id varchar NOT NULL,
    name varchar NOT NULL,
    sku varchar NOT NULL,
    description varchar NULL,
    unit_price numeric(19,2) NOT NULL,
    created_by varchar NOT NULL,
    created_utc timestamp NOT NULL,
    updated_by varchar NULL,
    updated_utc timestamp NULL,
    CONSTRAINT product_pkey PRIMARY KEY (id),
    CONSTRAINT product_sku_key UNIQUE (sku)
);

CREATE UNIQUE INDEX product_entity_id_idx ON order_management.product (entity_id);

--rollback DROP TABLE order_management.product;
