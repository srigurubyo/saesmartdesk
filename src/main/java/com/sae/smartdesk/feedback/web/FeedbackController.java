package com.sae.smartdesk.feedback.web;

import com.sae.smartdesk.auth.security.UserPrincipal;
import com.sae.smartdesk.common.dto.ApiResponse;
import com.sae.smartdesk.feedback.dto.FeedbackRequest;
import com.sae.smartdesk.feedback.dto.FeedbackResponse;
import com.sae.smartdesk.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests/{requestId}/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> create(@PathVariable UUID requestId,
                                                                 @Valid @RequestBody FeedbackRequest request,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        FeedbackResponse response = feedbackService.submitFeedback(requestId, principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> list(@PathVariable UUID requestId,
                                                                     @AuthenticationPrincipal UserPrincipal principal) {
        List<FeedbackResponse> responses = feedbackService.listFeedback(requestId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
