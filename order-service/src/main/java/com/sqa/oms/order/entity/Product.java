package com.sqa.oms.order.entity;

import com.sqa.oms.order.entity.common.BaseEntity;
import com.sqa.oms.order.entity.listener.BaseEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Data
@EntityListeners(BaseEntityListener.class)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "product", schema = "order_management")
public class Product extends BaseEntity {

    @Column(name = "entity_id", nullable = false, unique = true)
    private String entityId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "description")
    private String description;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
}
