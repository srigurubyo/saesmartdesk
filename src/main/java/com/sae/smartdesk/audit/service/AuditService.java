package com.sae.smartdesk.audit.service;

import com.sae.smartdesk.audit.entity.AuditLog;
import com.sae.smartdesk.audit.repository.AuditLogRepository;
import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.service.UserService;
import com.sae.smartdesk.common.enums.AuditAction;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    public AuditService(AuditLogRepository auditLogRepository, UserService userService) {
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
    }

    public void record(UUID actorId, AuditAction action, String entityType, UUID entityId, String details) {
        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID());
        if (actorId != null) {
            User actor = userService.getById(actorId);
            log.setActor(actor);
        }
        log.setAction(action.name());
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    public List<AuditEntryDto> timeline(UUID entityId) {
        return auditLogRepository.findByEntityIdOrderByOccurredAtAsc(entityId).stream()
            .map(AuditEntryDto::fromEntity)
            .toList();
    }
}
