package com.sqa.oms.ui.modules.order;

import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.order.model.CreateOrderRequest;
import com.sqa.oms.ui.modules.order.model.OrderListResponse;
import com.sqa.oms.ui.modules.order.model.OrderResponse;
import com.sqa.oms.ui.modules.order.model.OrderStatus;
import com.sqa.oms.ui.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.ui.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.ui.service.order.IOrderService;
import com.sqa.oms.ui.util.Check;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxies the order resources of order-service. The frontend only ever calls this gateway.
 */
@RestController
@RequestMapping(path = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Order Controller", description = "Proxy to order-service order APIs.")
public class OrderController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final IOrderService orderService;

    /**
     * Constructor with dependency injection.
     *
     * @param orderService proxy service
     */
    public OrderController(final IOrderService orderService) {
        Check.isNotNull(orderService, "orderService");
        this.orderService = orderService;
    }

    /**
     * Searches orders.
     *
     * @param status   optional status filter
     * @param pageable the paging information
     * @return a page of orders
     */
    @Operation(
            summary = "Search orders",
            description = "Returns a paged list of orders visible to the caller, optionally filtered by status",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Page of orders",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No orders are found", content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.ORDER_READ_AUTHORITY)
    public ResponseEntity<OrderListResponse> getAll(
            @Parameter(description = "Filter by order status") @RequestParam(required = false)
            final OrderStatus status,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
        OrderListResponse response = orderService.getAll(status, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single order.
     *
     * @param id order identifier
     * @return the order
     */
    @Operation(
            summary = "Get a single order",
            description = "Returns one order visible to the caller",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The order",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.ORDER_READ_AUTHORITY)
    public ResponseEntity<OrderResponse> getById(@PathVariable final String id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    /**
     * Creates an order.
     *
     * @param request request body
     * @return the created order
     */
    @Operation(
            summary = "Create an order",
            description = "Creates an order for the caller, or for an explicit customer when the caller is "
                    + "elevated",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "201",
                    description = "Order created",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class))}),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PostMapping
    @PreAuthorize(Constants.Authority.ORDER_WRITE_AUTHORITY)
    public ResponseEntity<OrderResponse> create(@RequestBody final CreateOrderRequest request) {
        return new ResponseEntity<>(orderService.create(request), HttpStatus.CREATED);
    }

    /**
     * Updates an order.
     *
     * @param id      order identifier
     * @param request request body
     * @return the updated order
     */
    @Operation(
            summary = "Update an order",
            description = "Replaces the items of an existing order; the total is always recomputed server side",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Order updated",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PutMapping("/{id}")
    @PreAuthorize(Constants.Authority.ORDER_WRITE_AUTHORITY)
    public ResponseEntity<OrderResponse> update(@PathVariable final String id,
                                                @RequestBody final UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    /**
     * Updates the status of an order.
     *
     * @param id      order identifier
     * @param request request body
     * @return the updated order
     */
    @Operation(
            summary = "Update the status of an order",
            description = "Moves an order to a new status when the transition is legal",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Status updated",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Illegal status transition", content =
                    @Content)})
    @PatchMapping("/{id}/status")
    @PreAuthorize(Constants.Authority.ORDER_WRITE_AUTHORITY)
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable final String id,
                                                       @RequestBody final UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    /**
     * Deletes an order.
     *
     * @param id order identifier
     * @return no content
     */
    @Operation(
            summary = "Delete an order",
            description = "Deletes an order",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(responseCode = "204", description = "Order deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)})
    @DeleteMapping("/{id}")
    @PreAuthorize(Constants.Authority.ORDER_DELETE_AUTHORITY)
    public ResponseEntity<Void> delete(@PathVariable final String id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
