package com.sqa.oms.identity.entity;

import com.sqa.oms.identity.entity.common.BaseEntity;
import com.sqa.oms.identity.entity.listener.BaseEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * A permission, seeded by Liquibase and mirrored by a Keycloak client scope of the same name.
 */
@Entity
@EntityListeners(BaseEntityListener.class)
@Table(name = "permission", schema = "identity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Permission extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "entity_id", nullable = false, updatable = false)
    private String entityId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private Short status;
}
