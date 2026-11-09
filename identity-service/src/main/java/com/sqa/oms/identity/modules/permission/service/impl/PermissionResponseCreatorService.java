package com.sqa.oms.identity.modules.permission.service.impl;

import com.sqa.oms.identity.mapper.IPermissionMapper;
import com.sqa.oms.identity.modules.permission.model.PermissionResponse;
import com.sqa.oms.identity.modules.permission.service.IPermissionResponseCreatorService;
import com.sqa.oms.identity.service.permission.IPermissionService;
import com.sqa.oms.identity.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the single permission response builder.
 */
@Service
public final class PermissionResponseCreatorService implements IPermissionResponseCreatorService {

    private final IPermissionService permissionService;

    private final IPermissionMapper permissionMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param permissionService core permission read access
     * @param permissionMapper  permission dto mapper
     */
    public PermissionResponseCreatorService(final IPermissionService permissionService,
                                            final IPermissionMapper permissionMapper) {
        Check.isNotNull(permissionService, "permissionService");
        Check.isNotNull(permissionMapper, "permissionMapper");

        this.permissionService = permissionService;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public PermissionResponse create(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return permissionMapper.toResponse(permissionService.getByEntityId(entityId));
    }
}
