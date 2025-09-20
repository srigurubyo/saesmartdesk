package com.sae.smartdesk.scheduler;

import com.sae.smartdesk.AbstractIntegrationTest;
import com.sae.smartdesk.modules.hall.dto.HallBookingRequest;
import com.sae.smartdesk.modules.hall.service.HallBookingService;
import com.sae.smartdesk.notify.entity.Notification;
import com.sae.smartdesk.notify.repository.NotificationRepository;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.repository.RequestRepository;
import com.sae.smartdesk.scheduler.task.RequestSlaScheduler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SlaSchedulerIT extends AbstractIntegrationTest {

    @Autowired
    private HallBookingService hallBookingService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestSlaScheduler requestSlaScheduler;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void schedulerEmitsReminderForOverdueApproval() {
        UUID requesterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        HallBookingRequest hallRequest = new HallBookingRequest(
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            Instant.now().plusSeconds(3600),
            Instant.now().plusSeconds(7200),
            "CLASSROOM",
            20,
            null,
            "Overdue reminder test"
        );
        var response = hallBookingService.createBooking(hallRequest, requesterId);
        Request request = requestRepository.findByDetailId(response.id()).orElseThrow();
        request.setDueAt(Instant.now().minusSeconds(3600));
        requestRepository.save(request);

        requestSlaScheduler.sendApprovalReminders();

        List<Notification> notifications = notificationRepository.findAll();
        boolean reminderExists = notifications.stream()
            .anyMatch(n -> "approval.reminder".equals(n.getTemplateKey()) && n.getRequest().getId().equals(request.getId()));
        Assertions.assertTrue(reminderExists, "Expected approval reminder notification");
    }
}
