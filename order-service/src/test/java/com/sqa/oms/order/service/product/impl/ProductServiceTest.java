package com.sqa.oms.order.service.product.impl;

import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.product.dto.ProductDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.modules.product.repository.projection.ProductProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductMapper productMapper;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, productMapper);
    }

    @Test
    void shouldThrowNotFoundWhenProductDoesNotExist() {
        when(productRepository.findProjectionByEntityId("PRD-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getByEntityId("PRD-1")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldMapTheProjectionIntoADto() {
        ProductProjection projection = new ProductProjection("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductDTO dto = new ProductDTO();
        dto.setEntityId("PRD-1");

        when(productRepository.findProjectionByEntityId("PRD-1")).thenReturn(Optional.of(projection));
        when(productMapper.toDto(projection)).thenReturn(dto);

        ProductDTO result = productService.getByEntityId("PRD-1");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void shouldMapThePagedListIntoAPagedListOfDtos() {
        ProductListRequest request = new ProductListRequest(null, null);
        ProductProjection projection = new ProductProjection("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductDTO dto = new ProductDTO();
        dto.setEntityId("PRD-1");

        when(productRepository.findPagedList(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(new PageImpl<>(List.of(projection)));
        when(productMapper.toDto(projection)).thenReturn(dto);

        var result = productService.getPagedList(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getItems()).containsExactly(dto);
    }
}
