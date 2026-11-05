package com.sqa.oms.identity.service.user.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.entity.UserGroup;
import com.sqa.oms.identity.entity.id.UserGroupId;
import com.sqa.oms.identity.event.DomainChangeEvent;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.user.IModifyUserService;
import com.sqa.oms.identity.service.validator.IModifyUserServiceValidator;
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
 * Default implementation of the user update flow.
 */
@Service
@Transactional
public class ModifyUserService implements IModifyUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyUserService.class);

    private final IUserRepository userRepository;

    private final IGroupRepository groupRepository;

    private final IUserGroupRepository userGroupRepository;

    private final IUserMapper userMapper;

    private final IModifyUserServiceValidator validator;

    private final ICurrentCallerProvider currentCallerProvider;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ObjectMapper objectMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param userRepository            access to the user table
     * @param groupRepository           access to the group table
     * @param userGroupRepository       access to the user_group table
     * @param userMapper                user entity mapper
     * @param validator                 validator of the modify request
     * @param currentCallerProvider     resolver of the caller identity
     * @param applicationEventPublisher publisher of the local domain events
     * @param objectMapper              serializer of the event payloads
     */
    public ModifyUserService(final IUserRepository userRepository,
                             final IGroupRepository groupRepository,
                             final IUserGroupRepository userGroupRepository,
                             final IUserMapper userMapper,
                             final IModifyUserServiceValidator validator,
                             final ICurrentCallerProvider currentCallerProvider,
                             final ApplicationEventPublisher applicationEventPublisher,
                             final ObjectMapper objectMapper) {

        Check.isNotNull(userRepository, "userRepository");
        Check.isNotNull(groupRepository, "groupRepository");
        Check.isNotNull(userGroupRepository, "userGroupRepository");
        Check.isNotNull(userMapper, "userMapper");
        Check.isNotNull(validator, "validator");
        Check.isNotNull(currentCallerProvider, "currentCallerProvider");
        Check.isNotNull(applicationEventPublisher, "applicationEventPublisher");
        Check.isNotNull(objectMapper, "objectMapper");

        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
        this.userMapper = userMapper;
        this.validator = validator;
        this.currentCallerProvider = currentCallerProvider;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void update(final String entityId, final ModifyUserRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        User user = findUserOrThrow(entityId);

        validator.validate(user, request);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());

        User saved = userRepository.save(user);
        replaceGroups(saved, request.getGroupIds());
        raiseEvent(saved, request.getGroupIds());
    }

    private User findUserOrThrow(final String entityId) {
        return userRepository.findByEntityId(entityId)
                .orElseThrow(() -> {
                    LOGGER.error("User with id {} does not exist", entityId);
                    return new EntityNotFoundException(String.format("User %s does not exist", entityId));
                });
    }

    private void replaceGroups(final User user, final List<String> groupIds) {
        Set<String> desired = new LinkedHashSet<>();
        if (groupIds != null) {
            desired.addAll(groupIds);
        }

        List<UserGroup> current = userGroupRepository.findByUser(user);
        List<UserGroup> obsolete = current.stream()
                .filter(membership -> !desired.contains(membership.getUserGroupId().getGroup().getEntityId()))
                .toList();
        Set<String> retained = current.stream()
                .map(membership -> membership.getUserGroupId().getGroup().getEntityId())
                .filter(desired::contains)
                .collect(Collectors.toSet());

        if (!obsolete.isEmpty()) {
            userGroupRepository.deleteAll(obsolete);
        }

        List<String> missing = desired.stream()
                .filter(groupId -> !retained.contains(groupId))
                .toList();

        if (!missing.isEmpty()) {
            List<Group> groups = groupRepository.findAllByEntityIdIn(missing);
            List<UserGroup> memberships = groups.stream()
                    .map(group -> {
                        UserGroup membership = new UserGroup();
                        membership.setUserGroupId(new UserGroupId(user, group));

                        return membership;
                    })
                    .toList();

            userGroupRepository.saveAll(memberships);
        }
    }

    private void raiseEvent(final User user, final List<String> groupIds) {
        List<String> groups = groupIds == null ? List.of() : groupIds;
        ObjectNode payload = objectMapper.valueToTree(userMapper.toResponse(user));
        payload.set("groupIds", objectMapper.valueToTree(groups));

        // Raised, not published: the Kafka write happens in DomainChangeEventListener after commit.
        applicationEventPublisher.publishEvent(new DomainChangeEvent(
                EntityType.USER,
                EventAction.UPDATE,
                user.getEntityId(),
                currentCallerProvider.resolveActor(),
                payload));
    }
}
