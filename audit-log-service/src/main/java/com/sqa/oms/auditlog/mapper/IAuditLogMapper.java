package com.sqa.oms.auditlog.mapper;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.service.store.model.AuditLogEntry;
import com.sqa.oms.common.events.OmsEvent;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Maps between the Kafka event contract, the store model, the internal DTO and the API model.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IAuditLogMapper {

    /**
     * Maps a consumed Kafka event to a store entry.
     *
     * @param event the consumed event
     * @return the store entry
     */
    AuditLogEntry toEntry(OmsEvent event);

    /**
     * Maps a store entry to the internal DTO.
     *
     * @param entry the store entry
     * @return the internal DTO
     */
    AuditLogDTO toDto(AuditLogEntry entry);

    /**
     * Maps the internal DTO to the API response.
     *
     * @param dto the internal DTO
     * @return the API response
     */
    AuditLogResponse toResponse(AuditLogDTO dto);
}
