package com.sqa.oms.order.modules.order.mapper;

import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.entity.OrderItem;
import com.sqa.oms.order.modules.order.dto.OrderDTO;
import com.sqa.oms.order.modules.order.model.OrderItemResponse;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.projection.OrderItemProjection;
import com.sqa.oms.order.modules.order.repository.projection.OrderProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IOrderMapper {

    /**
     * Maps an order entity to its API representation, used only to build the domain event
     * payload off the just persisted entity.
     *
     * @param order order entity
     * @return OrderResponse
     */
    OrderResponse toResponse(Order order);

    /**
     * Maps an order projection to its DTO.
     *
     * @param projection projection to map
     * @return OrderDTO
     */
    OrderDTO toDto(OrderProjection projection);

    /**
     * Combines an order DTO with its separately fetched items into the final API representation.
     *
     * @param dto   source order dto
     * @param items the order's items, already mapped to their response representation
     * @return OrderResponse
     */
    OrderResponse toResponse(OrderDTO dto, List<OrderItemResponse> items);

    /**
     * Maps a single order item, resolving the optional catalog product reference.
     *
     * @param item order item entity
     * @return OrderItemResponse
     */
    @Mapping(target = "productId", source = "product.entityId")
    @Mapping(target = "lineTotal", expression = "java(OrderAmountCalculator.lineTotal(item))")
    OrderItemResponse toItemResponse(OrderItem item);

    /**
     * Maps a single order item projection.
     *
     * @param item order item projection
     * @return OrderItemResponse
     */
    @Mapping(target = "lineTotal",
            expression = "java(OrderAmountCalculator.lineTotal(item.getUnitPrice(), item.getQuantity()))")
    OrderItemResponse toItemResponse(OrderItemProjection item);

    /**
     * Maps a list of order item entities.
     *
     * @param items order item entities
     * @return list of OrderItemResponse
     */
    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);

    /**
     * Maps a list of order item projections.
     *
     * @param items order item projections
     * @return list of OrderItemResponse
     */
    List<OrderItemResponse> toItemProjectionResponseList(List<OrderItemProjection> items);
}
