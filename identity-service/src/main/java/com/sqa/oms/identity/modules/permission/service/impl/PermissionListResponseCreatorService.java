package com.sqa.oms.identity.modules.permission.service.impl;

import com.sqa.oms.identity.dto.PermissionDTO;
import com.sqa.oms.identity.mapper.IPermissionMapper;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.modules.permission.model.PermissionListResponse;
import com.sqa.oms.identity.modules.permission.model.PermissionResponse;
import com.sqa.oms.identity.modules.permission.service.IPermissionListResponseCreatorService;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.service.permission.IPermissionService;
import com.sqa.oms.identity.util.Check;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the permission list response builder.
 */
@Service
public class PermissionListResponseCreatorService implements IPermissionListResponseCreatorService {

    private final IPermissionService permissionService;

    private final IPermissionMapper permissionMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param permissionService core permission read access
     * @param permissionMapper  permission dto mapper
     */
    public PermissionListResponseCreatorService(final IPermissionService permissionService,
                                                final IPermissionMapper permissionMapper) {
        Check.isNotNull(permissionService, "permissionService");
        Check.isNotNull(permissionMapper, "permissionMapper");

        this.permissionService = permissionService;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public PermissionListResponse create(final PermissionListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        PagedList<PermissionDTO> pagedList = permissionService.getPagedList(request, pageable);
        List<PermissionResponse> data = permissionMapper.toResponseList(pagedList.getItems());

        return new PermissionListResponse(data, pagedList.getCount());
    }
}
