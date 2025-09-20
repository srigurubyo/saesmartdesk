package com.sae.smartdesk.notify.service;

import com.sae.smartdesk.common.enums.NotificationChannel;
import com.sae.smartdesk.common.enums.NotificationStatus;
import com.sae.smartdesk.notify.entity.Notification;
import com.sae.smartdesk.notify.repository.NotificationRepository;
import com.sae.smartdesk.request.entity.Request;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification enqueue(Request request, NotificationChannel channel, String templateKey, String payload) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setRequest(request);
        notification.setChannel(channel);
        notification.setTemplateKey(templateKey);
        notification.setPayload(payload);
        notification.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);
        log.info("Enqueued notification {} for request {}", saved.getId(), request != null ? request.getId() : null);
        return saved;
    }

    public List<Notification> fetchPendingBatch(int limit) {
        List<Notification> batch = notificationRepository.findTop50ByStatusOrderByCreatedAt(NotificationStatus.PENDING);
        if (batch.size() > limit) {
            return batch.subList(0, limit);
        }
        return batch;
    }

    public void markSent(Notification notification) {
        notificationRepository.updateStatus(notification.getId(), NotificationStatus.SENT, Instant.now());
    }

    public void markFailed(Notification notification) {
        notificationRepository.updateStatus(notification.getId(), NotificationStatus.FAILED, Instant.now());
    }
}
