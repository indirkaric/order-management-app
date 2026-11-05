package com.sqa.oms.order.service.product.impl;

import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.ICreateProductServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductMapper productMapper;

    @Mock
    private ICreateProductServiceValidator validator;

    @Mock
    private ICallerContext callerContext;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CreateProductService createProductService;

    @BeforeEach
    void setUp() {
        createProductService = new CreateProductService(productRepository, productMapper, validator,
                callerContext, applicationEventPublisher);
    }

    @Test
    void shouldRejectCreateProductWhenSkuIsDuplicate() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        doThrow(new BusinessException("A product with sku KBD-1 already exists."))
                .when(validator).validate(request);

        assertThatThrownBy(() -> createProductService.save(request)).isInstanceOf(BusinessException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldPersistAndRaiseEventWhenProductIsCreated() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(any(Product.class))).thenReturn(
                new ProductResponse("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN));

        createProductService.save(request);

        verify(validator).validate(request);
        verify(applicationEventPublisher).publishEvent(any(DomainChangeEvent.class));
    }
}
