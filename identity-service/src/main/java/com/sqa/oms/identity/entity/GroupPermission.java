package com.sqa.oms.identity.entity;

import com.sqa.oms.identity.entity.common.AuditEntity;
import com.sqa.oms.identity.entity.id.GroupPermissionId;
import com.sqa.oms.identity.entity.listener.BaseEntityListener;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * Join entity between a permission group and a permission.
 */
@Entity
@EntityListeners(BaseEntityListener.class)
@Table(name = "group_permission", schema = "identity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class GroupPermission extends AuditEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private GroupPermissionId groupPermissionId;
}
