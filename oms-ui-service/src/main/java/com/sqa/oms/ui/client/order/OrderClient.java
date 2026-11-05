package com.sqa.oms.ui.client.order;

import com.sqa.oms.ui.client.BaseClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.order.model.OrderStatus;
import com.sqa.oms.ui.service.security.IScopeContext;
import com.sqa.oms.ui.util.QueryParamsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for the order resources of the order service.
 */
@Service
public class OrderClient extends BaseClient {

    private static final String ORDERS_PATH = "orders";

    @Value("${client.order.base-uri}")
    private String baseUri;

    /**
     * Constructor with dependency injection.
     *
     * @param webClient    shared WebClient
     * @param scopeContext resolver of the authenticated caller
     */
    public OrderClient(final WebClient webClient, final IScopeContext scopeContext) {
        super(webClient, scopeContext);
    }

    /**
     * Retrieves the order list.
     *
     * @param status   optional status filter
     * @param pageable paging information
     * @return downstream response
     */
    public ClientResponseWrapper getOrders(final OrderStatus status, final Pageable pageable) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addIfPresent(params, "status", status);
        QueryParamsBuilder.addPaging(params, pageable);

        return get(ORDERS_PATH, params);
    }

    /**
     * Retrieves a single order.
     *
     * @param orderId order identifier
     * @return downstream response
     */
    public ClientResponseWrapper getOrder(final String orderId) {
        return get(ORDERS_PATH + "/" + orderId, null);
    }

    /**
     * Creates an order.
     *
     * @param body request body
     * @return downstream response
     */
    public ClientResponseWrapper createOrder(final Object body) {
        return send(HttpMethod.POST, ORDERS_PATH, body);
    }

    /**
     * Updates an order.
     *
     * @param orderId order identifier
     * @param body    request body
     * @return downstream response
     */
    public ClientResponseWrapper updateOrder(final String orderId, final Object body) {
        return send(HttpMethod.PUT, ORDERS_PATH + "/" + orderId, body);
    }

    /**
     * Updates the status of an order.
     *
     * @param orderId order identifier
     * @param body    request body
     * @return downstream response
     */
    public ClientResponseWrapper updateOrderStatus(final String orderId, final Object body) {
        return send(HttpMethod.PATCH, ORDERS_PATH + "/" + orderId + "/status", body);
    }

    /**
     * Deletes an order.
     *
     * @param orderId order identifier
     * @return downstream response
     */
    public ClientResponseWrapper deleteOrder(final String orderId) {
        return send(HttpMethod.DELETE, ORDERS_PATH + "/" + orderId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String baseUri() {
        return baseUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String clientId() {
        return Constants.GATEWAY_ORDER_CLIENT;
    }
}
