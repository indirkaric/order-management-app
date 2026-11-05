package com.sqa.oms.identity.entity.listener;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.common.AuditEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

/**
 * Fills the audit columns of an entity right before it is written.
 */
public final class BaseEntityListener {

    /**
     * Sets the creation audit columns.
     *
     * @param entity entity about to be inserted
     */
    @PrePersist
    public void onPrePersist(final AuditEntity entity) {
        entity.setCreatedBy(resolveActor());
        entity.setCreatedUtc(new Date());
    }

    /**
     * Sets the modification audit columns.
     *
     * @param entity entity about to be updated
     */
    @PreUpdate
    public void onPreUpdate(final AuditEntity entity) {
        entity.setUpdatedBy(resolveActor());
        entity.setUpdatedUtc(new Date());
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return Constants.SYSTEM_ACTOR;
        }

        return authentication.getName();
    }
}
