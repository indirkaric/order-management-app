package com.sqa.oms.identity.service.user.impl;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.exception.EntityNotFoundException;
import com.sqa.oms.identity.mapper.IUserMapper;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.repository.projection.UserProjection;
import com.sqa.oms.identity.service.model.PagedList;
import com.sqa.oms.identity.service.user.IUserService;
import com.sqa.oms.identity.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the user read access.
 */
@Service
@Transactional(readOnly = true)
public class UserService implements IUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final IUserRepository userRepository;

    private final IUserMapper userMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param userRepository access to the user table
     * @param userMapper     user projection mapper
     */
    public UserService(final IUserRepository userRepository, final IUserMapper userMapper) {
        Check.isNotNull(userRepository, "userRepository");
        Check.isNotNull(userMapper, "userMapper");

        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Reads a single user by its entityId.
     *
     * @param entityId entityId of the user
     * @return the user
     */
    @Override
    public UserDTO getByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        UserProjection projection = userRepository.findProjectionByEntityId(entityId)
                .orElseThrow(() -> {
                    LOGGER.error("User with id {} does not exist", entityId);
                    return new EntityNotFoundException(String.format("User %s does not exist", entityId));
                });

        return userMapper.toDto(projection);
    }

    /**
     * Reads a page of users matching the given filter.
     *
     * @param request  filter criteria
     * @param pageable paging and sorting
     * @return the matching page of users
     */
    @Override
    public PagedList<UserDTO> getPagedList(final UserListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        Page<UserProjection> page = userRepository.findPagedList(request, pageable);
        List<UserDTO> items = page.getContent().stream().map(userMapper::toDto).toList();

        return new PagedList<>(items, page.getTotalElements());
    }
}
