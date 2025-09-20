package com.sae.smartdesk.modules.defect.mapper;

import com.sae.smartdesk.modules.defect.dto.DefectReportResponse;
import com.sae.smartdesk.modules.defect.entity.DefectReport;
import org.springframework.stereotype.Component;

@Component
public class DefectReportMapper {

    public DefectReportResponse toResponse(DefectReport report, String requestStatus) {
        return new DefectReportResponse(
            report.getId(),
            report.getBuilding(),
            report.getDefectType(),
            report.getSeverity(),
            report.getDescription(),
            report.getPhotoUrls(),
            report.getReportedAt(),
            requestStatus
        );
    }
}
