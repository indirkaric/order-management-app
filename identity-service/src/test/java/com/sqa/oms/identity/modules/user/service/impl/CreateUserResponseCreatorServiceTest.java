package com.sqa.oms.identity.modules.user.service.impl;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.service.user.ICreateUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserResponseCreatorServiceTest {

    @Mock
    private ICreateUserService createUserService;

    private CreateUserResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new CreateUserResponseCreatorService(createUserService);
    }

    @Test
    void createWrapsTheSavedEntityId() {
        CreateUserRequest request = new CreateUserRequest("Jane", "Doe", "jane.doe@oms.local", List.of());
        when(createUserService.save(request)).thenReturn("USR00001");

        EntityCreatedResponse response = responseCreatorService.create(request);

        assertThat(response).isEqualTo(new EntityCreatedResponse("USR00001"));
    }
}
