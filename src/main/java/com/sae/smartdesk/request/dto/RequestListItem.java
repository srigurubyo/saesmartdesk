package com.sae.smartdesk.request.dto;

import java.time.Instant;
import java.util.UUID;

public record RequestListItem(
    UUID id,
    String requestType,
    String status,
    String priority,
    UUID requesterId,
    String requesterName,
    Instant submittedAt
) {
}
