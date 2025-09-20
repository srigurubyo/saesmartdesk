package com.sae.smartdesk.audit.service;

import com.sae.smartdesk.audit.entity.AuditLog;
import java.time.Instant;
import java.util.UUID;

public record AuditEntryDto(
    UUID id,
    String action,
    String actorName,
    Instant occurredAt,
    String details
) {
    public static AuditEntryDto fromEntity(AuditLog log) {
        String actorName = log.getActor() != null ? log.getActor().getFullName() : null;
        return new AuditEntryDto(log.getId(), log.getAction(), actorName, log.getOccurredAt(), log.getDetails());
    }
}
