package com.sae.smartdesk.notify.service;

import com.sae.smartdesk.common.enums.NotificationChannel;
import com.sae.smartdesk.notify.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationAdapter implements NotificationChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    @Override
    public boolean supports(String channel) {
        return NotificationChannel.EMAIL.name().equalsIgnoreCase(channel);
    }

    @Override
    public void send(Notification notification) {
        log.info("[EMAIL] Sending template={} payload={}", notification.getTemplateKey(), notification.getPayload());
    }
}
