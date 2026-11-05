--liquibase formatted sql

--changeset oms:0.0.1-SNAPSHOT

-- One row per Keycloak realm role / client scope name (see development/docker-mounts/keycloak/oms-realm.json).
-- The permission entity_id doubles as the Keycloak scope name, so the two stay in sync by naming convention.
INSERT INTO identity.permission (entity_id, name, description, status, created_by, created_utc)
VALUES
    ('ORDER_READ', 'ORDER_READ', 'Pregled narudzbi', 1, 'oms', current_timestamp),
    ('ORDER_WRITE', 'ORDER_WRITE', 'Kreiranje i izmjena narudzbi', 1, 'oms', current_timestamp),
    ('ORDER_DELETE', 'ORDER_DELETE', 'Brisanje narudzbi', 1, 'oms', current_timestamp),
    ('USER_READ', 'USER_READ', 'Pregled korisnika', 1, 'oms', current_timestamp),
    ('USER_MANAGE', 'USER_MANAGE', 'Kreiranje, izmjena, brisanje korisnika i dodjela grupa', 1, 'oms', current_timestamp),
    ('AUDIT_READ', 'AUDIT_READ', 'Pregled audit logova', 1, 'oms', current_timestamp),
    ('PRODUCT_READ', 'PRODUCT_READ', 'Pregled proizvoda i njihovih dokumenata', 1, 'oms', current_timestamp),
    ('PRODUCT_WRITE', 'PRODUCT_WRITE', 'Kreiranje proizvoda i upload dokumentacije', 1, 'oms', current_timestamp);

--rollback DELETE FROM identity.permission WHERE created_by = 'oms';
