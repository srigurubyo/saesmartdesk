package com.sae.smartdesk.feedback.mapper;

import com.sae.smartdesk.feedback.dto.FeedbackResponse;
import com.sae.smartdesk.feedback.entity.Feedback;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public FeedbackResponse toResponse(Feedback feedback) {
        return new FeedbackResponse(
            feedback.getId(),
            feedback.getRequest().getId(),
            feedback.getGivenBy().getId(),
            feedback.getGivenByRole(),
            feedback.getRating(),
            feedback.getComments(),
            feedback.getCreatedAt()
        );
    }
}
