package com.sae.smartdesk.approval.dto;

import java.util.List;
import java.util.UUID;

public record ApprovalWorkflowDto(
    UUID id,
    String requestType,
    int version,
    boolean active,
    List<ApprovalStepDto> steps
) {
}
