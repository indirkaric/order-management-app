--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE TABLE identity.group_permission (
    group_id int8 NOT NULL,
    permission_id int8 NOT NULL,
    created_by varchar NOT NULL,
    created_utc timestamp NOT NULL,
    updated_by varchar NULL,
    updated_utc timestamp NULL,
    CONSTRAINT group_permission_pkey PRIMARY KEY (group_id, permission_id),
    CONSTRAINT group_permission_group_fk FOREIGN KEY (group_id) REFERENCES identity."group" (id),
    CONSTRAINT group_permission_permission_fk FOREIGN KEY (permission_id) REFERENCES identity.permission (id)
);

--rollback DROP TABLE identity.group_permission;
