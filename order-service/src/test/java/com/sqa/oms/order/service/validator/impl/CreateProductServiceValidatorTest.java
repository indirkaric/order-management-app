package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceValidatorTest {

    @Mock
    private IProductRepository productRepository;

    private CreateProductServiceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CreateProductServiceValidator(productRepository);
    }

    @Test
    void shouldRejectWhenSkuIsDuplicate() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        when(productRepository.existsBySku("KBD-1")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldAcceptWhenSkuIsFree() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        when(productRepository.existsBySku("KBD-1")).thenReturn(false);

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
