--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

CREATE TABLE identity.user_group (
    user_id int8 NOT NULL,
    group_id int8 NOT NULL,
    created_by varchar NOT NULL,
    created_utc timestamp NOT NULL,
    updated_by varchar NULL,
    updated_utc timestamp NULL,
    CONSTRAINT user_group_pkey PRIMARY KEY (user_id, group_id),
    CONSTRAINT user_group_user_fk FOREIGN KEY (user_id) REFERENCES identity."user" (id),
    CONSTRAINT user_group_group_fk FOREIGN KEY (group_id) REFERENCES identity."group" (id)
);

--rollback DROP TABLE identity.user_group;
