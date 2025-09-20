package com.sae.smartdesk.request.web;

import com.sae.smartdesk.audit.service.AuditEntryDto;
import com.sae.smartdesk.audit.service.AuditService;
import com.sae.smartdesk.auth.security.UserPrincipal;
import com.sae.smartdesk.common.dto.ApiResponse;
import com.sae.smartdesk.common.dto.PageResponse;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.enums.RequestType;
import com.sae.smartdesk.request.dto.RequestActionRequest;
import com.sae.smartdesk.request.dto.RequestApprovalView;
import com.sae.smartdesk.request.dto.RequestDetailDto;
import com.sae.smartdesk.request.dto.RequestListItem;
import com.sae.smartdesk.request.dto.RequestSearchCriteria;
import com.sae.smartdesk.request.service.RequestCommandService;
import com.sae.smartdesk.request.service.RequestQueryService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestCommandService commandService;
    private final RequestQueryService queryService;
    private final AuditService auditService;

    public RequestController(RequestCommandService commandService, RequestQueryService queryService,
                             AuditService auditService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.auditService = auditService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestDetailDto>> getRequest(@PathVariable UUID id,
                                                                    @AuthenticationPrincipal UserPrincipal principal) {
        RequestDetailDto dto = queryService.getRequest(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestListItem>>> search(
        @RequestParam(value = "type", required = false) RequestType type,
        @RequestParam(value = "status", required = false) RequestStatus status,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(value = "mine", required = false) Boolean mine,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        Pageable pageable = PageRequest.of(page, size);
        RequestSearchCriteria criteria = new RequestSearchCriteria(type, status, from, to, mine);
        Page<RequestListItem> result = queryService.search(criteria, principal.getId(), pageable);
        PageResponse<RequestListItem> response = new PageResponse<>(
            result.getContent(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.getNumber(),
            result.getSize()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RequestDetailDto>> approve(@PathVariable UUID id,
                                                                  @Valid @RequestBody RequestActionRequest body,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        commandService.approve(id, principal.getId(), body.comment());
        RequestDetailDto dto = queryService.getRequest(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RequestDetailDto>> reject(@PathVariable UUID id,
                                                                 @Valid @RequestBody RequestActionRequest body,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        commandService.reject(id, principal.getId(), body.comment());
        RequestDetailDto dto = queryService.getRequest(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<RequestDetailDto>> complete(@PathVariable UUID id,
                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        commandService.complete(id, principal.getId());
        RequestDetailDto dto = queryService.getRequest(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{id}/approvals")
    public ResponseEntity<ApiResponse<List<RequestApprovalView>>> approvals(@PathVariable UUID id,
                                                                            @AuthenticationPrincipal UserPrincipal principal) {
        RequestDetailDto dto = queryService.getRequest(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(dto.approvals()));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<AuditEntryDto>>> timeline(@PathVariable UUID id,
                                                                     @AuthenticationPrincipal UserPrincipal principal) {
        queryService.getRequest(id, principal.getId());
        List<AuditEntryDto> timeline = auditService.timeline(id);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }
}
