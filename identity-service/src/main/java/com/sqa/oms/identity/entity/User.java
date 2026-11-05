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
 * A user of the order management system.
 */
@Entity
@EntityListeners(BaseEntityListener.class)
@Table(name = "`user`", schema = "identity")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "listOfUserGroup")
@ToString(callSuper = true, exclude = "listOfUserGroup")
public final class User extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "entity_id", nullable = false, updatable = false)
    private String entityId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "status", nullable = false)
    private Short status;

    @OneToMany(mappedBy = "userGroupId.user", fetch = FetchType.LAZY)
    private List<UserGroup> listOfUserGroup = new ArrayList<>();
}
