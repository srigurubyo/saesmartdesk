package com.sae.smartdesk.modules.defect;

import com.sae.smartdesk.AbstractIntegrationTest;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.feedback.dto.FeedbackRequest;
import com.sae.smartdesk.feedback.service.FeedbackService;
import com.sae.smartdesk.modules.defect.dto.DefectReportRequest;
import com.sae.smartdesk.modules.defect.dto.DefectReportResponse;
import com.sae.smartdesk.modules.defect.service.DefectReportService;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.repository.RequestRepository;
import com.sae.smartdesk.request.service.RequestCommandService;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DefectReportFlowIT extends AbstractIntegrationTest {

    @Autowired
    private DefectReportService defectReportService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestCommandService requestCommandService;

    @Autowired
    private FeedbackService feedbackService;

    @Test
    void defectReportLifecycle() {
        UUID reporterId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        DefectReportRequest request = new DefectReportRequest(
            "Building B",
            "Electrical",
            "HIGH",
            "Power outage in lab",
            "http://example.com/photo1.jpg"
        );
        DefectReportResponse response = defectReportService.createReport(request, reporterId);
        Request defectRequest = requestRepository.findByDetailId(response.id()).orElseThrow();
        Assertions.assertEquals(RequestStatus.PENDING_APPROVAL, defectRequest.getStatus());

        UUID hodId = UUID.fromString("00000000-0000-0000-0000-000000000004");
        requestCommandService.approve(defectRequest.getId(), hodId, "Approve repair");

        UUID unitOfficerId = UUID.fromString("00000000-0000-0000-0000-000000000007");
        requestCommandService.approve(defectRequest.getId(), unitOfficerId, "Will handle");
        defectRequest = requestRepository.findById(defectRequest.getId()).orElseThrow();
        Assertions.assertEquals(RequestStatus.APPROVED, defectRequest.getStatus());

        defectReportService.startWork(response.id(), unitOfficerId);
        defectRequest = requestRepository.findById(defectRequest.getId()).orElseThrow();
        Assertions.assertEquals(RequestStatus.IN_PROGRESS, defectRequest.getStatus());

        defectReportService.finishWork(response.id(), unitOfficerId);
        defectRequest = requestRepository.findById(defectRequest.getId()).orElseThrow();
        Assertions.assertEquals(RequestStatus.COMPLETED, defectRequest.getStatus());

        FeedbackRequest feedbackRequest = new FeedbackRequest(4, "Thanks for fixing quickly");
        feedbackService.submitFeedback(defectRequest.getId(), reporterId, feedbackRequest);
        Assertions.assertFalse(feedbackService.listFeedback(defectRequest.getId(), reporterId).isEmpty());
    }
}
