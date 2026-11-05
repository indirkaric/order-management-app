package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.dto.ProductDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.model.ProductListResponse;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.service.model.PagedList;
import com.sqa.oms.order.service.product.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductListResponseCreatorServiceTest {

    private static final int PAGE_SIZE = 20;

    @Mock
    private IProductService productService;

    @Mock
    private IProductMapper productMapper;

    private ProductListResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new ProductListResponseCreatorService(productService, productMapper);
    }

    @Test
    void shouldMapThePagedListIntoAListResponse() {
        ProductDTO dto = new ProductDTO();
        dto.setEntityId("PRD-1");
        ProductListRequest request = new ProductListRequest(null, null);
        ProductResponse response = new ProductResponse("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);

        when(productService.getPagedList(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(new PagedList<>(List.of(dto), 1));
        when(productMapper.toResponse(dto)).thenReturn(response);

        ProductListResponse result = responseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getData()).containsExactly(response);
        assertThat(result.getCount()).isEqualTo(1);
    }
}
