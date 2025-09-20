package com.sae.smartdesk.approval.web;

import com.sae.smartdesk.approval.dto.ApprovalWorkflowDto;
import com.sae.smartdesk.approval.mapper.ApprovalWorkflowMapper;
import com.sae.smartdesk.approval.service.ApprovalRefreshService;
import com.sae.smartdesk.approval.service.ApprovalWorkflowService;
import com.sae.smartdesk.common.dto.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/approval")
@PreAuthorize("hasRole('ADMIN')")
public class ApprovalAdminController {

    private final ApprovalRefreshService refreshService;
    private final ApprovalWorkflowService workflowService;
    private final ApprovalWorkflowMapper mapper;

    public ApprovalAdminController(ApprovalRefreshService refreshService, ApprovalWorkflowService workflowService,
                                   ApprovalWorkflowMapper mapper) {
        this.refreshService = refreshService;
        this.workflowService = workflowService;
        this.mapper = mapper;
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh() {
        int count = refreshService.refreshFromYaml();
        return ResponseEntity.ok(ApiResponse.success("Refreshed " + count + " workflows"));
    }

    @GetMapping("/workflows")
    public ResponseEntity<ApiResponse<List<ApprovalWorkflowDto>>> list() {
        List<ApprovalWorkflowDto> workflows = workflowService.listAll().stream()
            .map(mapper::toDto)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(workflows));
    }
}
