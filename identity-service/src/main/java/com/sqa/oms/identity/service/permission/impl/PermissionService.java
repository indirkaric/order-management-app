package com.sqa.oms.identity.service.permission.impl;

import com.sqa.oms.identity.dto.PermissionDTO;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IPermissionMapper;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.repository.projection.PermissionProjection;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.service.permission.IPermissionService;
import com.sqa.oms.identity.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the permission read access.
 */
@Service
@Transactional(readOnly = true)
public class PermissionService implements IPermissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionService.class);

    private final IPermissionRepository permissionRepository;

    private final IPermissionMapper permissionMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param permissionRepository access to the permission table
     * @param permissionMapper     permission projection mapper
     */
    public PermissionService(final IPermissionRepository permissionRepository,
                             final IPermissionMapper permissionMapper) {
        Check.isNotNull(permissionRepository, "permissionRepository");
        Check.isNotNull(permissionMapper, "permissionMapper");

        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    /**
     * Reads a single permission by its entityId.
     *
     * @param entityId entityId of the permission
     * @return the permission
     */
    @Override
    public PermissionDTO getByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        PermissionProjection projection = permissionRepository.findProjectionByEntityId(entityId)
                .orElseThrow(() -> {
                    LOGGER.error("Permission with id {} does not exist", entityId);
                    return new EntityNotFoundException(String.format("Permission %s does not exist", entityId));
                });

        return permissionMapper.toDto(projection);
    }

    /**
     * Reads a page of permissions matching the given filter.
     *
     * @param request  filter criteria
     * @param pageable paging and sorting
     * @return the matching page of permissions
     */
    @Override
    public PagedList<PermissionDTO> getPagedList(final PermissionListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        Page<PermissionProjection> page = permissionRepository.findPagedList(request, pageable);
        List<PermissionDTO> items = page.getContent().stream().map(permissionMapper::toDto).toList();

        return new PagedList<>(items, page.getTotalElements());
    }
}
