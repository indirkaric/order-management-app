package com.sqa.oms.identity.entity.id;

import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.User;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Composite id of the UserGroup entity.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class UserGroupId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
}
