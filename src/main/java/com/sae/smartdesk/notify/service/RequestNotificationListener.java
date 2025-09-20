package com.sae.smartdesk.notify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sae.smartdesk.common.enums.NotificationChannel;
import com.sae.smartdesk.common.event.RequestApprovalEvent;
import com.sae.smartdesk.common.event.RequestLifecycleEvent;
import com.sae.smartdesk.request.entity.Request;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RequestNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(RequestNotificationListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public RequestNotificationListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLifecycle(RequestLifecycleEvent event) {
        Request request = event.getRequest();
        switch (event.getNewStatus()) {
            case PENDING_APPROVAL -> enqueue(request, "request.submitted");
            case APPROVED -> enqueue(request, "request.approved");
            case REJECTED -> enqueue(request, "request.rejected");
            case COMPLETED -> {
                enqueue(request, "request.completed");
                enqueue(request, "feedback.invite");
            }
            default -> {
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproval(RequestApprovalEvent event) {
        // Hook for module-specific listeners; no-op here.
    }

    private void enqueue(Request request, String templateKey) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("requestId", request.getId());
            payload.put("requestType", request.getRequestType().name());
            payload.put("status", request.getStatus().name());
            String payloadJson = objectMapper.writeValueAsString(payload);
            notificationService.enqueue(request, NotificationChannel.EMAIL, templateKey, payloadJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification payload", e);
        }
    }
}
