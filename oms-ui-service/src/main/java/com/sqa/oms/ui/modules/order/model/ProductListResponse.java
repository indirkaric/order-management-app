package com.sqa.oms.ui.modules.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged response of the product list endpoint, mirroring order-service's own
 * {@code ProductListResponse} shape ({@code data}/{@code count}, not a Spring Data {@code Page}).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponse {

    private List<ProductResponse> data = new ArrayList<>();

    private long count;
}
