--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE TABLE order_management.order_item (
    id int8 GENERATED ALWAYS AS IDENTITY (INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    order_id int8 NOT NULL,
    product_id int8 NULL,
    product_name varchar NOT NULL,
    quantity int4 NOT NULL,
    unit_price numeric(19,2) NOT NULL,
    created_by varchar NOT NULL,
    created_utc timestamp NOT NULL,
    updated_by varchar NULL,
    updated_utc timestamp NULL,
    CONSTRAINT order_item_pkey PRIMARY KEY (id),
    CONSTRAINT order_item_order_fk FOREIGN KEY (order_id) REFERENCES order_management.orders (id),
    CONSTRAINT order_item_product_fk FOREIGN KEY (product_id) REFERENCES order_management.product (id)
);

--rollback DROP TABLE order_management.order_item;
