package com.sqa.oms.order.entity;

import com.sqa.oms.order.entity.common.BaseEntity;
import com.sqa.oms.order.entity.listener.BaseEntityListener;
import com.sqa.oms.order.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EntityListeners(BaseEntityListener.class)
@ToString(callSuper = true, exclude = {"items"})
@EqualsAndHashCode(callSuper = true, exclude = {"items"})
@Table(name = "orders", schema = "order_management")
public class Order extends BaseEntity {

    @Column(name = "entity_id", nullable = false, unique = true)
    private String entityId;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();
}
