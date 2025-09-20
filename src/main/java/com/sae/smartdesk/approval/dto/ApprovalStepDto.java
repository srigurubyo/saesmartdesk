package com.sae.smartdesk.approval.dto;

import java.util.UUID;

public record ApprovalStepDto(
    UUID id,
    int order,
    String approverRole,
    int slaHours
) {
}
