package com.sae.smartdesk.request.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RequestDetailDto(
    UUID id,
    String requestType,
    String status,
    UUID detailId,
    UUID requesterId,
    String requesterName,
    Integer currentStep,
    String priority,
    Instant submittedAt,
    Instant dueAt,
    Instant closedAt,
    List<RequestApprovalView> approvals
) {
}
