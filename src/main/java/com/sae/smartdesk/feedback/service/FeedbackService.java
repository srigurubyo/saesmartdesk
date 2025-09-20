package com.sae.smartdesk.feedback.service;

import com.sae.smartdesk.audit.service.AuditService;
import com.sae.smartdesk.common.enums.AuditAction;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.exception.BadRequestException;
import com.sae.smartdesk.common.exception.ForbiddenException;
import com.sae.smartdesk.common.exception.NotFoundException;
import com.sae.smartdesk.feedback.dto.FeedbackRequest;
import com.sae.smartdesk.feedback.dto.FeedbackResponse;
import com.sae.smartdesk.feedback.entity.Feedback;
import com.sae.smartdesk.feedback.mapper.FeedbackMapper;
import com.sae.smartdesk.feedback.repository.FeedbackRepository;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.repository.RequestRepository;
import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.service.UserService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final FeedbackMapper feedbackMapper;
    private final AuditService auditService;

    public FeedbackService(FeedbackRepository feedbackRepository, RequestRepository requestRepository,
                           UserService userService, FeedbackMapper feedbackMapper, AuditService auditService) {
        this.feedbackRepository = feedbackRepository;
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.feedbackMapper = feedbackMapper;
        this.auditService = auditService;
    }

    public FeedbackResponse submitFeedback(UUID requestId, UUID userId, FeedbackRequest request) {
        Request linkedRequest = requestRepository.findDetailedById(requestId)
            .orElseThrow(() -> new NotFoundException("Request not found"));
        if (linkedRequest.getStatus() != RequestStatus.COMPLETED) {
            throw new BadRequestException("Feedback allowed only for completed requests");
        }
        User user = userService.getById(userId);
        if (!isAllowedToFeedback(linkedRequest, user)) {
            throw new ForbiddenException("User cannot submit feedback");
        }
        Feedback feedback = new Feedback();
        feedback.setId(UUID.randomUUID());
        feedback.setRequest(linkedRequest);
        feedback.setGivenBy(user);
        feedback.setGivenByRole(resolvePrimaryRole(user));
        feedback.setRating(request.rating());
        feedback.setComments(request.comments());
        Feedback saved = feedbackRepository.save(feedback);
        auditService.record(userId, AuditAction.FEEDBACK_CREATE, "REQUEST", linkedRequest.getId(), "rating=" + request.rating());
        return feedbackMapper.toResponse(saved);
    }

    public List<FeedbackResponse> listFeedback(UUID requestId, UUID userId) {
        Request linkedRequest = requestRepository.findDetailedById(requestId)
            .orElseThrow(() -> new NotFoundException("Request not found"));
        User user = userService.getById(userId);
        if (!isAllowedToFeedback(linkedRequest, user)) {
            throw new ForbiddenException("User cannot view feedback");
        }
        return feedbackRepository.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
            .map(feedbackMapper::toResponse)
            .toList();
    }

    private boolean isAllowedToFeedback(Request request, User user) {
        if (request.getRequester().getId().equals(user.getId())) {
            return true;
        }
        return user.getRoles().stream()
            .map(role -> role.getName().toUpperCase(Locale.ROOT))
            .anyMatch(name -> name.equals("ADMIN") || name.equals("UNIT_OFFICER"));
    }

    private String resolvePrimaryRole(User user) {
        return user.getRoles().stream()
            .findFirst()
            .map(role -> role.getName())
            .orElse("UNKNOWN");
    }
}
