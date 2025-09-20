package com.sae.smartdesk.scheduler.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sae.smartdesk.audit.service.AuditService;
import com.sae.smartdesk.common.enums.AuditAction;
import com.sae.smartdesk.common.enums.NotificationChannel;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.notify.service.NotificationService;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.repository.RequestRepository;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RequestSlaScheduler {

    private static final Logger log = LoggerFactory.getLogger(RequestSlaScheduler.class);

    private final RequestRepository requestRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public RequestSlaScheduler(RequestRepository requestRepository, NotificationService notificationService,
                               ObjectMapper objectMapper, AuditService auditService) {
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelayString = "${sae.scheduler.sla-delay-millis:60000}")
    @Transactional
    public void sendApprovalReminders() {
        List<Request> overdue = requestRepository.findByStatusAndDueAtBefore(RequestStatus.PENDING_APPROVAL, Instant.now());
        for (Request request : overdue) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("requestId", request.getId());
                payload.put("requestType", request.getRequestType().name());
                payload.put("currentStep", request.getCurrentStep());
                String payloadJson = objectMapper.writeValueAsString(payload);
                notificationService.enqueue(request, NotificationChannel.EMAIL, "approval.reminder", payloadJson);
                auditService.record(null, AuditAction.NOTIFICATION_DISPATCH, "REQUEST", request.getId(), "approval.reminder");
                request.setDueAt(Instant.now().plus(Duration.ofHours(4)));
                log.info("Dispatched SLA reminder for request {}", request.getId());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize reminder payload", e);
            }
        }
    }
}
