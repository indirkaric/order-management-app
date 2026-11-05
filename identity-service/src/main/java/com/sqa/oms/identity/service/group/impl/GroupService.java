package com.sqa.oms.identity.service.group.impl;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.projection.GroupProjection;
import com.sqa.oms.identity.service.group.IGroupService;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the group read access.
 */
@Service
@Transactional(readOnly = true)
public class GroupService implements IGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

    private final IGroupRepository groupRepository;

    private final IGroupMapper groupMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param groupRepository access to the group table
     * @param groupMapper     group projection mapper
     */
    public GroupService(final IGroupRepository groupRepository, final IGroupMapper groupMapper) {
        Check.isNotNull(groupRepository, "groupRepository");
        Check.isNotNull(groupMapper, "groupMapper");

        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    public GroupDTO getByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        GroupProjection projection = groupRepository.findProjectionByEntityId(entityId)
                .orElseThrow(() -> {
                    LOGGER.error("Group with id {} does not exist", entityId);
                    return new EntityNotFoundException(String.format("Group %s does not exist", entityId));
                });

        return groupMapper.toDto(projection);
    }

    @Override
    public PagedList<GroupDTO> getPagedList(final GroupListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        Page<GroupProjection> page = groupRepository.findPagedList(request, pageable);
        List<GroupDTO> items = page.getContent().stream().map(groupMapper::toDto).toList();

        return new PagedList<>(items, page.getTotalElements());
    }
}
