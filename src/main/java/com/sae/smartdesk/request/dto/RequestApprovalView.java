package com.sae.smartdesk.request.dto;

import java.time.Instant;
import java.util.UUID;

public record RequestApprovalView(
    UUID id,
    int order,
    String expectedRole,
    String approverName,
    String decision,
    String comment,
    Instant decidedAt
) {
}
