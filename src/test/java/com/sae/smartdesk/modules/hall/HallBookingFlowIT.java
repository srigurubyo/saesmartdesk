package com.sae.smartdesk.modules.hall;

import com.sae.smartdesk.AbstractIntegrationTest;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.feedback.dto.FeedbackRequest;
import com.sae.smartdesk.feedback.service.FeedbackService;
import com.sae.smartdesk.modules.hall.dto.HallBookingRequest;
import com.sae.smartdesk.modules.hall.dto.HallBookingResponse;
import com.sae.smartdesk.modules.hall.service.HallBookingService;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.repository.RequestRepository;
import com.sae.smartdesk.request.service.RequestCommandService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HallBookingFlowIT extends AbstractIntegrationTest {

    @Autowired
    private HallBookingService hallBookingService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestCommandService requestCommandService;

    @Autowired
    private FeedbackService feedbackService;

    @Test
    void hallBookingHappyPath() {
        UUID requesterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        HallBookingRequest request = new HallBookingRequest(
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            Instant.now().plusSeconds(3600),
            Instant.now().plusSeconds(7200),
            "THEATER",
            30,
            "Projector,PA System",
            "Team Townhall"
        );
        HallBookingResponse response = hallBookingService.createBooking(request, requesterId);
        Request bookingRequest = requestRepository.findByDetailId(response.id())
            .orElseThrow();
        Assertions.assertEquals(RequestStatus.PENDING_APPROVAL, bookingRequest.getStatus());

        UUID hodId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        requestCommandService.approve(bookingRequest.getId(), hodId, "Looks good");
        bookingRequest = requestRepository.findById(bookingRequest.getId()).orElseThrow();
        Assertions.assertEquals(2, bookingRequest.getCurrentStep());

        UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000005");
        requestCommandService.approve(bookingRequest.getId(), adminId, "Approved");
        bookingRequest = requestRepository.findById(bookingRequest.getId()).orElseThrow();
        Assertions.assertEquals(RequestStatus.APPROVED, bookingRequest.getStatus());

        requestCommandService.complete(bookingRequest.getId(), adminId);
        bookingRequest = requestRepository.findById(bookingRequest.getId()).orElseThrow();
        Assertions.assertEquals(RequestStatus.COMPLETED, bookingRequest.getStatus());

        FeedbackRequest feedbackRequest = new FeedbackRequest(5, "Great support");
        feedbackService.submitFeedback(bookingRequest.getId(), requesterId, feedbackRequest);
        Assertions.assertFalse(feedbackService.listFeedback(bookingRequest.getId(), requesterId).isEmpty());
    }
}
