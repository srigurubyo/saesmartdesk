package com.sae.smartdesk.audit.repository;

import com.sae.smartdesk.audit.entity.AuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityIdOrderByOccurredAtAsc(UUID entityId);
}
