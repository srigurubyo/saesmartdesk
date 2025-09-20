package com.sae.smartdesk.notify.service;

import com.sae.smartdesk.common.enums.NotificationChannel;
import com.sae.smartdesk.notify.entity.Notification;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationService notificationService;
    private final List<NotificationChannelAdapter> adapters;

    public NotificationDispatcher(NotificationService notificationService, List<NotificationChannelAdapter> adapters) {
        this.notificationService = notificationService;
        this.adapters = adapters;
    }

    public void dispatchPending() {
        List<Notification> pending = notificationService.fetchPendingBatch(50);
        for (Notification notification : pending) {
            try {
                NotificationChannel channel = notification.getChannel();
                NotificationChannelAdapter adapter = adapters.stream()
                    .filter(a -> a.supports(channel.name()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No adapter for channel " + channel));
                adapter.send(notification);
                notificationService.markSent(notification);
            } catch (Exception ex) {
                log.error("Failed to send notification {}", notification.getId(), ex);
                notificationService.markFailed(notification);
            }
        }
    }
}
