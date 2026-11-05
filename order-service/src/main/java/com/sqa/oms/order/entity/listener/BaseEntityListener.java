package com.sqa.oms.order.entity.listener;

import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.entity.common.BaseEntity;
import com.sqa.oms.order.util.StringHelper;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;

public final class BaseEntityListener {

    /**
     * Fills the creation audit columns before the entity is inserted.
     *
     * @param entity entity about to be persisted
     */
    @PrePersist
    public void onPrePersist(final BaseEntity entity) {
        entity.setCreatedBy(resolveActor());
        entity.setCreatedUtc(Instant.now());
    }

    /**
     * Fills the modification audit columns before the entity is updated.
     *
     * @param entity entity about to be updated
     */
    @PreUpdate
    public void onPreUpdate(final BaseEntity entity) {
        entity.setUpdatedBy(resolveActor());
        entity.setUpdatedUtc(Instant.now());
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Constants.SYSTEM_ACTOR;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String userId = jwtAuthentication.getToken().getClaimAsString(Constants.USER_ID_CLAIM);

            if (!StringHelper.isEmpty(userId)) {
                return userId;
            }
        }

        String name = authentication.getName();

        return StringHelper.isEmpty(name) ? Constants.SYSTEM_ACTOR : name;
    }
}
