package com.sae.smartdesk.modules.defect.web;

import com.sae.smartdesk.auth.security.UserPrincipal;
import com.sae.smartdesk.common.dto.ApiResponse;
import com.sae.smartdesk.modules.defect.dto.DefectReportRequest;
import com.sae.smartdesk.modules.defect.dto.DefectReportResponse;
import com.sae.smartdesk.modules.defect.service.DefectReportService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/defect-reports")
public class DefectReportController {

    private final DefectReportService defectReportService;

    public DefectReportController(DefectReportService defectReportService) {
        this.defectReportService = defectReportService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REQUESTOR','ADMIN')")
    public ResponseEntity<ApiResponse<DefectReportResponse>> create(@Valid @RequestBody DefectReportRequest request,
                                                                     @AuthenticationPrincipal UserPrincipal principal) {
        DefectReportResponse response = defectReportService.createReport(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DefectReportResponse>> get(@PathVariable UUID id,
                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        DefectReportResponse response = defectReportService.getReport(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('UNIT_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<DefectReportResponse>> start(@PathVariable UUID id,
                                                                    @AuthenticationPrincipal UserPrincipal principal) {
        DefectReportResponse response = defectReportService.startWork(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('UNIT_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<DefectReportResponse>> finish(@PathVariable UUID id,
                                                                     @AuthenticationPrincipal UserPrincipal principal) {
        DefectReportResponse response = defectReportService.finishWork(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
