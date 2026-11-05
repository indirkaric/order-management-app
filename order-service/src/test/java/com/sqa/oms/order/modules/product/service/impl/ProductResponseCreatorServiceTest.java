package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.dto.ProductDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.service.product.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductResponseCreatorServiceTest {

    @Mock
    private IProductService productService;

    @Mock
    private IProductMapper productMapper;

    private ProductResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new ProductResponseCreatorService(productService, productMapper);
    }

    @Test
    void shouldDelegateCreateToTheServiceAndMapper() {
        ProductDTO dto = new ProductDTO();
        dto.setEntityId("PRD-1");
        ProductResponse response = new ProductResponse("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);

        when(productService.getByEntityId("PRD-1")).thenReturn(dto);
        when(productMapper.toResponse(dto)).thenReturn(response);

        ProductResponse result = responseCreatorService.create("PRD-1");

        assertThat(result).isEqualTo(response);
    }
}
