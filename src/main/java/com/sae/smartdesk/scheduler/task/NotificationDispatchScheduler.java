package com.sae.smartdesk.scheduler.task;

import com.sae.smartdesk.notify.service.NotificationDispatcher;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatchScheduler {

    private final NotificationDispatcher dispatcher;

    public NotificationDispatchScheduler(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${sae.scheduler.notification-delay-millis:30000}")
    @Transactional
    public void dispatch() {
        dispatcher.dispatchPending();
    }
}
