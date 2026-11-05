package com.sqa.oms.identity.modules.group.service.impl;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.service.group.ICreateGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGroupResponseCreatorServiceTest {

    @Mock
    private ICreateGroupService createGroupService;

    private CreateGroupResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new CreateGroupResponseCreatorService(createGroupService);
    }

    @Test
    void createWrapsTheSavedEntityId() {
        CreateGroupRequest request = new CreateGroupRequest("OMS_MANAGER", "desc", List.of("ORDER_READ"));
        when(createGroupService.save(request)).thenReturn("GROUP0001");

        EntityCreatedResponse response = responseCreatorService.create(request);

        assertThat(response).isEqualTo(new EntityCreatedResponse("GROUP0001"));
    }
}
