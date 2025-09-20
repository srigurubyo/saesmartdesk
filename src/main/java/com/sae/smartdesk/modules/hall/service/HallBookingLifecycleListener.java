package com.sae.smartdesk.modules.hall.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sae.smartdesk.common.enums.NotificationChannel;
import com.sae.smartdesk.common.event.RequestApprovalEvent;
import com.sae.smartdesk.modules.hall.entity.HallBooking;
import com.sae.smartdesk.modules.hall.repository.HallBookingRepository;
import com.sae.smartdesk.notify.service.NotificationService;
import com.sae.smartdesk.request.entity.Request;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class HallBookingLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(HallBookingLifecycleListener.class);
    private static final DateTimeFormatter ICS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final HallBookingRepository hallBookingRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public HallBookingLifecycleListener(HallBookingRepository hallBookingRepository,
                                        NotificationService notificationService,
                                        ObjectMapper objectMapper) {
        this.hallBookingRepository = hallBookingRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproval(RequestApprovalEvent event) {
        Request request = event.getRequest();
        if (!event.isFinalStep() || request.getRequestType() != com.sae.smartdesk.common.enums.RequestType.HALL_BOOKING) {
            return;
        }
        hallBookingRepository.findById(request.getDetailId()).ifPresent(booking -> {
            String ics = buildIcs(request.getId(), booking);
            log.info("Generated ICS for hall booking {}:\n{}", booking.getId(), ics);
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("requestId", request.getId());
                payload.put("hallId", booking.getHall().getId());
                payload.put("ics", ics);
                notificationService.enqueue(request, NotificationChannel.EMAIL, "hall.booking.calendar",
                    objectMapper.writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize hall booking payload", e);
            }
        });
    }

    private String buildIcs(UUID requestId, HallBooking booking) {
        StringBuilder builder = new StringBuilder();
        builder.append("BEGIN:VCALENDAR\n");
        builder.append("VERSION:2.0\n");
        builder.append("PRODID:-//SmartDesk//EN\n");
        builder.append("BEGIN:VEVENT\n");
        builder.append("UID:").append(requestId).append("@smartdesk\n");
        builder.append("DTSTAMP:").append(ICS_FORMAT.format(booking.getStartDatetime())).append("\n");
        builder.append("DTSTART:").append(ICS_FORMAT.format(booking.getStartDatetime())).append("\n");
        builder.append("DTEND:").append(ICS_FORMAT.format(booking.getEndDatetime())).append("\n");
        builder.append("SUMMARY:Hall Booking - ").append(booking.getPurpose()).append("\n");
        builder.append("LOCATION:").append(booking.getHall().getName()).append("\n");
        builder.append("END:VEVENT\n");
        builder.append("END:VCALENDAR");
        return builder.toString();
    }
}
