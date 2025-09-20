package com.sae.smartdesk.modules.defect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DefectReportRequest(
    @NotBlank @Size(max = 160) String building,
    @NotBlank @Size(max = 120) String defectType,
    @Size(max = 20) String severity,
    @NotBlank @Size(max = 2000) String description,
    @Size(max = 2000) String photoUrls
) {
}
