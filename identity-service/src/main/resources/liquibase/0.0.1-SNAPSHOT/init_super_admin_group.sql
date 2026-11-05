--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

INSERT INTO identity."group" (entity_id, name, description, status, created_by, created_utc)
VALUES ('SUPER-ADMIN', 'Super Administrator', 'Group for super administrators.', 1, 'oms', current_timestamp);

-- Every permission that exists at this point is granted to the super admin group; later changesets
-- that add a new permission must insert a matching row here as well.
INSERT INTO identity.group_permission (group_id, permission_id, created_by, created_utc)
SELECT g.id, p.id, 'oms', current_timestamp
FROM identity."group" g
CROSS JOIN identity.permission p
WHERE g.entity_id = 'SUPER-ADMIN';

--rollback DELETE FROM identity.group_permission WHERE created_by = 'oms';
--rollback DELETE FROM identity."group" WHERE entity_id = 'SUPER-ADMIN';
