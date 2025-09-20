package com.sae.smartdesk.request.service;

import com.sae.smartdesk.common.exception.ForbiddenException;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.entity.RequestApproval;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import com.sae.smartdesk.auth.entity.User;
import org.springframework.stereotype.Service;

@Service
public class RequestAuthorizationService {

    public void ensureCanView(Request request, User user) {
        if (isRequester(request, user) || hasAnyWorkflowRole(request, user) || hasRole(user, "ADMIN")) {
            return;
        }
        throw new ForbiddenException("Access denied to request");
    }

    public void ensureCanApprove(Request request, User user) {
        RequestApproval current = request.getApprovals().stream()
            .filter(appr -> appr.getStepOrder() == request.getCurrentStep())
            .findFirst()
            .orElseThrow(() -> new ForbiddenException("Approval step missing"));
        String requiredRole = request.getWorkflow().getSteps().stream()
            .filter(step -> step.getStepOrder() == current.getStepOrder())
            .map(step -> step.getApproverRole())
            .findFirst()
            .orElse(null);
        if (requiredRole != null && hasRole(user, requiredRole)) {
            return;
        }
        throw new ForbiddenException("User cannot approve this request");
    }

    public void ensureCanReject(Request request, User user) {
        ensureCanApprove(request, user);
    }

    private boolean isRequester(Request request, User user) {
        return request.getRequester().getId().equals(user.getId());
    }

    private boolean hasAnyWorkflowRole(Request request, User user) {
        Set<String> workflowRoles = request.getWorkflow().getSteps().stream()
            .map(step -> step.getApproverRole().toUpperCase(Locale.ROOT))
            .collect(Collectors.toSet());
        return user.getRoles().stream()
            .map(role -> role.getName().toUpperCase(Locale.ROOT))
            .anyMatch(workflowRoles::contains);
    }

    private boolean hasRole(User user, String roleName) {
        String required = roleName.toUpperCase(Locale.ROOT);
        return user.getRoles().stream()
            .map(role -> role.getName().toUpperCase(Locale.ROOT))
            .anyMatch(required::equals);
    }
}
