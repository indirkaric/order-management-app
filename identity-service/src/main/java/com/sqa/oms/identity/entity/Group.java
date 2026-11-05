package com.sqa.oms.identity.entity;

import com.sqa.oms.identity.entity.common.BaseEntity;
import com.sqa.oms.identity.entity.listener.BaseEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A permission group a user can be a member of.
 */
@Entity
@EntityListeners(BaseEntityListener.class)
@Table(name = "`group`", schema = "identity")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "listOfGroupPermission")
@ToString(callSuper = true, exclude = "listOfGroupPermission")
public final class Group extends BaseEntity implements Serializable {

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

    @OneToMany(mappedBy = "groupPermissionId.group", fetch = FetchType.LAZY)
    private List<GroupPermission> listOfGroupPermission = new ArrayList<>();
}
