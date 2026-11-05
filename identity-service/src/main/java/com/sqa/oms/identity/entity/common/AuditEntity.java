package com.sqa.oms.identity.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;

/**
 * Audit columns shared by every table of this service.
 */
@Data
@MappedSuperclass
public abstract class AuditEntity {

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_utc", nullable = false)
    private Date createdUtc;

    @Column(name = "updated_by")
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_utc")
    private Date updatedUtc;
}
