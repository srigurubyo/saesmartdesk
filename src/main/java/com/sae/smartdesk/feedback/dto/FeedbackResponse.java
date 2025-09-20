package com.sae.smartdesk.feedback.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(
    UUID id,
    UUID requestId,
    UUID givenByUserId,
    String givenByRole,
    int rating,
    String comments,
    Instant createdAt
) {
}
