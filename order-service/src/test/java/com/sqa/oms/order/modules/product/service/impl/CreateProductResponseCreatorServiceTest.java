package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.modules.product.service.IProductResponseCreatorService;
import com.sqa.oms.order.service.product.ICreateProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductResponseCreatorServiceTest {

    @Mock
    private ICreateProductService createProductService;

    @Mock
    private IProductResponseCreatorService productResponseCreatorService;

    private CreateProductResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new CreateProductResponseCreatorService(createProductService,
                productResponseCreatorService);
    }

    @Test
    void shouldSaveThenBuildTheResponseFromTheNewEntityId() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductResponse response = new ProductResponse("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);

        when(createProductService.save(request)).thenReturn("PRD-1");
        when(productResponseCreatorService.create("PRD-1")).thenReturn(response);

        ProductResponse result = responseCreatorService.create(request);

        assertThat(result).isEqualTo(response);
    }
}
