package com.sqa.oms.identity.service.group.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.GroupPermission;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.entity.id.GroupPermissionId;
import com.sqa.oms.identity.event.DomainChangeEvent;
import com.sqa.oms.identity.mapper.IGroupMapper;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.repository.IGroupPermissionRepository;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.group.ICreateGroupService;
import com.sqa.oms.identity.service.validator.ICreateGroupServiceValidator;
import com.sqa.oms.identity.util.Check;
import com.sqa.oms.identity.util.EntityIdGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the group create flow.
 */
@Service
@Transactional
public class CreateGroupService implements ICreateGroupService {

    private final IGroupRepository groupRepository;

    private final IPermissionRepository permissionRepository;

    private final IGroupPermissionRepository groupPermissionRepository;

    private final IGroupMapper groupMapper;

    private final ICreateGroupServiceValidator validator;

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
     * @param validator                 validator of the create request
     * @param currentCallerProvider     resolver of the caller identity
     * @param applicationEventPublisher publisher of the local domain events
     * @param objectMapper              serializer of the event payloads
     */
    public CreateGroupService(final IGroupRepository groupRepository,
                              final IPermissionRepository permissionRepository,
                              final IGroupPermissionRepository groupPermissionRepository,
                              final IGroupMapper groupMapper,
                              final ICreateGroupServiceValidator validator,
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
     * Creates a group from the given request.
     *
     * @param request the group to create
     * @return entityId of the created group
     */
    @Override
    public String save(final CreateGroupRequest request) {
        Check.isNotNull(request, "request");

        validator.validate(request);

        Group group = new Group();
        group.setEntityId(generateEntityId());
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setStatus(Constants.Status.ACTIVE_STATUS);

        Group saved = groupRepository.save(group);
        assignPermissions(saved, request.getPermissionIds());
        raiseEvent(saved, request.getPermissionIds());

        return saved.getEntityId();
    }

    private String generateEntityId() {
        String candidate = EntityIdGenerator.generate();
        while (groupRepository.existsByEntityId(candidate)) {
            candidate = EntityIdGenerator.generate();
        }

        return candidate;
    }

    private void assignPermissions(final Group group, final List<String> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllByEntityIdIn(permissionIds);
        List<GroupPermission> assignments = permissions.stream()
                .map(permission -> {
                    GroupPermission assignment = new GroupPermission();
                    assignment.setGroupPermissionId(new GroupPermissionId(group, permission));

                    return assignment;
                })
                .toList();

        groupPermissionRepository.saveAll(assignments);
    }

    private void raiseEvent(final Group group, final List<String> permissionIds) {
        ObjectNode payload = objectMapper.valueToTree(groupMapper.toResponse(group));
        payload.set("permissionIds", objectMapper.valueToTree(permissionIds));

        // Raised, not published: the Kafka write happens in DomainChangeEventListener after commit.
        applicationEventPublisher.publishEvent(new DomainChangeEvent(
                EntityType.GROUP,
                EventAction.CREATE,
                group.getEntityId(),
                currentCallerProvider.resolveActor(),
                payload));
    }
}
