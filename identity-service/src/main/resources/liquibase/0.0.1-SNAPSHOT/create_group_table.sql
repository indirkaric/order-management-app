--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE TABLE identity."group" (
    id int8 GENERATED ALWAYS AS IDENTITY (INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
    entity_id varchar NOT NULL,
    name varchar NOT NULL,
    description varchar NULL,
    status int2 NOT NULL,
    created_by varchar NOT NULL,
    created_utc timestamp NOT NULL,
    updated_by varchar NULL,
    updated_utc timestamp NULL,
    CONSTRAINT group_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX group_entity_id_idx ON identity."group" (entity_id);

--rollback DROP TABLE identity."group";
