package com.sqa.oms.order.modules.order;

import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderListResponse;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.order.modules.order.service.ICreateOrderResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IModifyOrderResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IOrderListResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IOrderResponseCreatorService;
import com.sqa.oms.order.service.order.IDeleteOrderService;
import com.sqa.oms.order.util.Check;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * CRUD API over orders.
 */
@RestController
@RequestMapping(path = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Order Controller", description = "Order APIs.")
public class OrderController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ICreateOrderResponseCreatorService createOrderResponseCreatorService;

    private final IModifyOrderResponseCreatorService modifyOrderResponseCreatorService;

    private final IDeleteOrderService deleteOrderService;

    private final IOrderResponseCreatorService orderResponseCreatorService;

    private final IOrderListResponseCreatorService orderListResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param createOrderResponseCreatorService builds the response of the create endpoint
     * @param modifyOrderResponseCreatorService builds the response of the update endpoints
     * @param deleteOrderService                core order delete flow
     * @param orderResponseCreatorService       builds the single order response
     * @param orderListResponseCreatorService   builds the order list response
     */
    public OrderController(final ICreateOrderResponseCreatorService createOrderResponseCreatorService,
                           final IModifyOrderResponseCreatorService modifyOrderResponseCreatorService,
                           final IDeleteOrderService deleteOrderService,
                           final IOrderResponseCreatorService orderResponseCreatorService,
                           final IOrderListResponseCreatorService orderListResponseCreatorService) {
        Check.isNotNull(createOrderResponseCreatorService, "createOrderResponseCreatorService");
        Check.isNotNull(modifyOrderResponseCreatorService, "modifyOrderResponseCreatorService");
        Check.isNotNull(deleteOrderService, "deleteOrderService");
        Check.isNotNull(orderResponseCreatorService, "orderResponseCreatorService");
        Check.isNotNull(orderListResponseCreatorService, "orderListResponseCreatorService");

        this.createOrderResponseCreatorService = createOrderResponseCreatorService;
        this.modifyOrderResponseCreatorService = modifyOrderResponseCreatorService;
        this.deleteOrderService = deleteOrderService;
        this.orderResponseCreatorService = orderResponseCreatorService;
        this.orderListResponseCreatorService = orderListResponseCreatorService;
    }

    /**
     * Creates a new order for the resolved customer.
     *
     * @param request create request
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
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody final CreateOrderRequest request) {
        Check.isNotNull(request, "request");

        return new ResponseEntity<>(createOrderResponseCreatorService.create(request), HttpStatus.CREATED);
    }

    /**
     * Returns a paged list of orders visible to the caller, optionally filtered by status.
     *
     * @param status   optional status filter
     * @param pageable paging information
     * @return paged list of orders
     */
    @Operation(
            summary = "Search orders",
            description = "Returns a paged list of orders visible to the caller, optionally filtered by status. "
                    + "A non elevated caller only ever sees its own orders",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Paged list of orders",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No orders are found", content = @Content)})
    @GetMapping
    @Parameters({
            @Parameter(name = "pageable", hidden = true),
            @Parameter(
                    name = "status",
                    schema = @Schema(type = "string"),
                    in = ParameterIn.QUERY,
                    description = "Filter by order status"),
            @Parameter(
                    name = "page",
                    schema = @Schema(type = "integer", example = "0", defaultValue = "0"),
                    in = ParameterIn.QUERY,
                    description = "Page number to retrieve."),
            @Parameter(
                    name = "size",
                    schema = @Schema(type = "integer", example = "20", defaultValue = "20"),
                    in = ParameterIn.QUERY,
                    description = "Page size to retrieve."),
            @Parameter(
                    name = "sort",
                    schema = @Schema(example = "createdUtc", defaultValue = ""),
                    in = ParameterIn.QUERY,
                    description = "Name of the field to sort data.")
    })
    @PreAuthorize(Constants.Authority.ORDER_READ_AUTHORITY)
    public ResponseEntity<OrderListResponse> listOrders(
            @RequestParam(required = false) final OrderStatus status,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        OrderListResponse response = orderListResponseCreatorService.create(status, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single order visible to the caller.
     *
     * @param entityId business key of the order
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
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("id") final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return ResponseEntity.ok(orderResponseCreatorService.create(entityId));
    }

    /**
     * Replaces the items of an existing order and recomputes its total.
     *
     * @param entityId business key of the order
     * @param request  update request
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
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable("id") final String entityId,
                                                           @Valid @RequestBody final UpdateOrderRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        return ResponseEntity.ok(modifyOrderResponseCreatorService.update(entityId, request));
    }

    /**
     * Moves an order to a new status when the transition is legal.
     *
     * @param entityId business key of the order
     * @param request  requested status
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
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable("id") final String entityId, @Valid @RequestBody final UpdateOrderStatusRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        return ResponseEntity.ok(modifyOrderResponseCreatorService.updateStatus(entityId, request));
    }

    /**
     * Deletes an order.
     *
     * @param entityId business key of the order
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
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        deleteOrderService.delete(entityId);

        return ResponseEntity.noContent().build();
    }
}
