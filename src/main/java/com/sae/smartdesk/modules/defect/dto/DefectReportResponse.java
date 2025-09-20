package com.sae.smartdesk.modules.defect.dto;

import java.time.Instant;
import java.util.UUID;

public record DefectReportResponse(
    UUID id,
    String building,
    String defectType,
    String severity,
    String description,
    String photoUrls,
    Instant reportedAt,
    String requestStatus
) {
}
