package com.sqa.oms.identity.service.group.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.GroupPermission;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.entity.id.GroupPermissionId;
import com.sqa.oms.identity.event.DomainChangeEvent;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;
import com.sqa.oms.identity.repository.IGroupPermissionRepository;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.group.IModifyGroupService;
import com.sqa.oms.identity.service.validator.IModifyGroupServiceValidator;
import com.sqa.oms.identity.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of the group update flow.
 */
@Service
@Transactional
public class ModifyGroupService implements IModifyGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyGroupService.class);

    private final IGroupRepository groupRepository;

    private final IPermissionRepository permissionRepository;

    private final IGroupPermissionRepository groupPermissionRepository;

    private final IGroupMapper groupMapper;

    private final IModifyGroupServiceValidator validator;

    private final ICurrentCallerProvider currentCallerProvider;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ObjectMapper objectMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param groupRepository           access to the group table
     * @param permissionRepository      access to the permission table
     * @param groupPermissionRepository access to the group_permission table
     * @param groupMapper               group entity mapper
     * @param validator                 validator of the update request
     * @param currentCallerProvider     resolver of the caller identity
     * @param applicationEventPublisher publisher of the local domain events
     * @param objectMapper              serializer of the event payloads
     */
    public ModifyGroupService(final IGroupRepository groupRepository,
                              final IPermissionRepository permissionRepository,
                              final IGroupPermissionRepository groupPermissionRepository,
                              final IGroupMapper groupMapper,
                              final IModifyGroupServiceValidator validator,
                              final ICurrentCallerProvider currentCallerProvider,
                              final ApplicationEventPublisher applicationEventPublisher,
                              final ObjectMapper objectMapper) {

        Check.isNotNull(groupRepository, "groupRepository");
        Check.isNotNull(permissionRepository, "permissionRepository");
        Check.isNotNull(groupPermissionRepository, "groupPermissionRepository");
        Check.isNotNull(groupMapper, "groupMapper");
        Check.isNotNull(validator, "validator");
        Check.isNotNull(currentCallerProvider, "currentCallerProvider");
        Check.isNotNull(applicationEventPublisher, "applicationEventPublisher");
        Check.isNotNull(objectMapper, "objectMapper");

        this.groupRepository = groupRepository;
        this.permissionRepository = permissionRepository;
        this.groupPermissionRepository = groupPermissionRepository;
        this.groupMapper = groupMapper;
        this.validator = validator;
        this.currentCallerProvider = currentCallerProvider;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Updates an existing group and its permission assignments.
     *
     * @param entityId entityId of the group to update
     * @param request  the new group content
     */
    @Override
    public void update(final String entityId, final UpdateGroupRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        Group group = findGroupOrThrow(entityId);

        validator.validate(group, request);

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setStatus(request.getStatus());

        Group saved = groupRepository.save(group);
        replacePermissions(saved, request.getPermissionIds());
        raiseEvent(saved, request.getPermissionIds());
    }

    private Group findGroupOrThrow(final String entityId) {
        return groupRepository.findByEntityId(entityId)
                .orElseThrow(() -> {
                    LOGGER.error("Group with id {} does not exist", entityId);
                    return new EntityNotFoundException(String.format("Group %s does not exist", entityId));
                });
    }

    private void replacePermissions(final Group group, final List<String> permissionIds) {
        Set<String> desired = new LinkedHashSet<>();
        if (permissionIds != null) {
            desired.addAll(permissionIds);
        }

        List<GroupPermission> current = groupPermissionRepository.findByGroup(group);
        List<GroupPermission> obsolete = current.stream()
                .filter(assignment -> !desired.contains(
                        assignment.getGroupPermissionId().getPermission().getEntityId()))
                .toList();
        Set<String> retained = current.stream()
                .map(assignment -> assignment.getGroupPermissionId().getPermission().getEntityId())
                .filter(desired::contains)
                .collect(Collectors.toSet());

        if (!obsolete.isEmpty()) {
            groupPermissionRepository.deleteAll(obsolete);
        }

        List<String> missing = desired.stream()
                .filter(permissionId -> !retained.contains(permissionId))
                .toList();

        if (!missing.isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllByEntityIdIn(missing);
            List<GroupPermission> assignments = permissions.stream()
                    .map(permission -> {
                        GroupPermission assignment = new GroupPermission();
                        assignment.setGroupPermissionId(new GroupPermissionId(group, permission));

                        return assignment;
                    })
                    .toList();

            groupPermissionRepository.saveAll(assignments);
        }
    }

    private void raiseEvent(final Group group, final List<String> permissionIds) {
        List<String> permissions = permissionIds == null ? List.of() : permissionIds;
        ObjectNode payload = objectMapper.valueToTree(groupMapper.toResponse(group));
        payload.set("permissionIds", objectMapper.valueToTree(permissions));

        // Raised, not published: the Kafka write happens in DomainChangeEventListener after commit.
        applicationEventPublisher.publishEvent(new DomainChangeEvent(
                EntityType.GROUP,
                EventAction.UPDATE,
                group.getEntityId(),
                currentCallerProvider.resolveActor(),
                payload));
    }
}
