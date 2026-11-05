package com.sqa.oms.order.modules.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged response of the order list endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderListResponse {

    private List<OrderResponse> data = new ArrayList<>();

    private long count;
}
