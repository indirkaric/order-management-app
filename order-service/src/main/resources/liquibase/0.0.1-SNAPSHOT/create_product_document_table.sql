--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE TABLE order_management.product_document (
    id int8 GENERATED ALWAYS AS IDENTITY (INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    entity_id varchar NOT NULL,
    product_id int8 NOT NULL,
    file_name varchar NOT NULL,
    content_type varchar NULL,
    size_bytes int8 NULL,
    s3_key varchar NOT NULL,
    s3_url varchar NULL,
    uploaded_at timestamp NOT NULL,
    CONSTRAINT product_document_pkey PRIMARY KEY (id),
    CONSTRAINT product_document_product_fk FOREIGN KEY (product_id) REFERENCES order_management.product (id)
);

CREATE UNIQUE INDEX product_document_entity_id_idx ON order_management.product_document (entity_id);

--rollback DROP TABLE order_management.product_document;
