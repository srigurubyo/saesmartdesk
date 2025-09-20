package com.sae.smartdesk.request.mapper;

import com.sae.smartdesk.request.dto.RequestApprovalView;
import com.sae.smartdesk.request.dto.RequestDetailDto;
import com.sae.smartdesk.request.dto.RequestListItem;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.entity.RequestApproval;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public RequestDetailDto toDetail(Request request) {
        List<RequestApprovalView> approvalViews = request.getApprovals().stream()
            .sorted(Comparator.comparingInt(RequestApproval::getStepOrder))
            .map(approval -> toApprovalView(request, approval))
            .toList();
        return new RequestDetailDto(
            request.getId(),
            request.getRequestType().name(),
            request.getStatus().name(),
            request.getDetailId(),
            request.getRequester().getId(),
            request.getRequester().getFullName(),
            request.getCurrentStep(),
            request.getPriority().name(),
            request.getSubmittedAt(),
            request.getDueAt(),
            request.getClosedAt(),
            approvalViews
        );
    }

    public RequestListItem toListItem(Request request) {
        return new RequestListItem(
            request.getId(),
            request.getRequestType().name(),
            request.getStatus().name(),
            request.getPriority().name(),
            request.getRequester().getId(),
            request.getRequester().getFullName(),
            request.getSubmittedAt()
        );
    }

    private RequestApprovalView toApprovalView(Request request, RequestApproval approval) {
        String expectedRole = request.getWorkflow().getSteps().stream()
            .filter(step -> step.getStepOrder() == approval.getStepOrder())
            .map(step -> step.getApproverRole())
            .findFirst()
            .orElse(null);
        String approverName = Optional.ofNullable(approval.getApprover())
            .map(approver -> approver.getFullName())
            .orElse(null);
        String decision = Optional.ofNullable(approval.getDecision())
            .map(Enum::name)
            .orElse(null);
        return new RequestApprovalView(
            approval.getId(),
            approval.getStepOrder(),
            expectedRole,
            approverName,
            decision,
            approval.getComment(),
            approval.getDecidedAt()
        );
    }
}
