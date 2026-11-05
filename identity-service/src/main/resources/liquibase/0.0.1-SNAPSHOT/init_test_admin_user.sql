--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

-- entity_id 'OMSADM01' must stay in sync with the Keycloak realm's seeded "admin" user
-- attributes.user_id in development/docker-mounts/keycloak/oms-realm.json.
INSERT INTO identity."user" (entity_id, first_name, last_name, email, status, created_by, created_utc)
VALUES ('OMSADM01', 'Admin', 'Admin', 'admin@oms.local', 1, 'oms', current_timestamp);

INSERT INTO identity.user_group (user_id, group_id, created_by, created_utc)
SELECT u.id, g.id, 'oms', current_timestamp
FROM identity."user" u, identity."group" g
WHERE u.entity_id = 'OMSADM01' AND g.entity_id = 'SUPER-ADMIN';

--rollback DELETE FROM identity.user_group WHERE created_by = 'oms';
--rollback DELETE FROM identity."user" WHERE entity_id = 'OMSADM01';
