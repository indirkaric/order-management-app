package com.sqa.oms.identity.service.user.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.entity.UserGroup;
import com.sqa.oms.identity.entity.id.UserGroupId;
import com.sqa.oms.identity.event.DomainChangeEvent;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.IUserGroupRepository;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import com.sqa.oms.identity.service.user.ICreateUserService;
import com.sqa.oms.identity.service.validator.ICreateUserServiceValidator;
import com.sqa.oms.identity.util.Check;
import com.sqa.oms.identity.util.EntityIdGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the user create flow.
 */
@Service
@Transactional
public class CreateUserService implements ICreateUserService {

    private final IUserRepository userRepository;

    private final IGroupRepository groupRepository;

    private final IUserGroupRepository userGroupRepository;

    private final IUserMapper userMapper;

    private final ICreateUserServiceValidator validator;

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
     * @param validator                 validator of the create request
     * @param currentCallerProvider     resolver of the caller identity
     * @param applicationEventPublisher publisher of the local domain events
     * @param objectMapper              serializer of the event payloads
     */
    public CreateUserService(final IUserRepository userRepository,
                             final IGroupRepository groupRepository,
                             final IUserGroupRepository userGroupRepository,
                             final IUserMapper userMapper,
                             final ICreateUserServiceValidator validator,
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

    /**
     * Creates a user from the given request.
     *
     * @param request the user to create
     * @return entityId of the created user
     */
    @Override
    public String save(final CreateUserRequest request) {
        Check.isNotNull(request, "request");

        validator.validate(request);

        User user = new User();
        user.setEntityId(generateEntityId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setStatus(Constants.Status.ACTIVE_STATUS);

        User saved = userRepository.save(user);
        assignGroups(saved, request.getGroupIds());
        raiseEvent(saved, request.getGroupIds());

        return saved.getEntityId();
    }

    private String generateEntityId() {
        String candidate = EntityIdGenerator.generate();
        while (userRepository.existsByEntityId(candidate)) {
            candidate = EntityIdGenerator.generate();
        }

        return candidate;
    }

    private void assignGroups(final User user, final List<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return;
        }

        List<Group> groups = groupRepository.findAllByEntityIdIn(groupIds);
        List<UserGroup> memberships = groups.stream()
                .map(group -> {
                    UserGroup membership = new UserGroup();
                    membership.setUserGroupId(new UserGroupId(user, group));

                    return membership;
                })
                .toList();

        userGroupRepository.saveAll(memberships);
    }

    private void raiseEvent(final User user, final List<String> groupIds) {
        List<String> groups = groupIds == null ? List.of() : groupIds;
        ObjectNode payload = objectMapper.valueToTree(userMapper.toResponse(user));
        payload.set("groupIds", objectMapper.valueToTree(groups));

        // Raised, not published: the Kafka write happens in DomainChangeEventListener after commit.
        applicationEventPublisher.publishEvent(new DomainChangeEvent(
                EntityType.USER,
                EventAction.CREATE,
                user.getEntityId(),
                currentCallerProvider.resolveActor(),
                payload));
    }
}
