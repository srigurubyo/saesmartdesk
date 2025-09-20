package com.sae.smartdesk.request.service;

import com.sae.smartdesk.approval.entity.ApprovalStep;
import com.sae.smartdesk.approval.entity.ApprovalWorkflow;
import com.sae.smartdesk.approval.service.ApprovalWorkflowService;
import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.service.UserService;
import com.sae.smartdesk.audit.service.AuditService;
import com.sae.smartdesk.common.enums.ApprovalDecision;
import com.sae.smartdesk.common.enums.AuditAction;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.enums.RequestType;
import com.sae.smartdesk.common.event.RequestApprovalEvent;
import com.sae.smartdesk.common.event.RequestLifecycleEvent;
import com.sae.smartdesk.common.exception.BadRequestException;
import com.sae.smartdesk.common.exception.ConflictException;
import com.sae.smartdesk.common.exception.NotFoundException;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.entity.RequestApproval;
import com.sae.smartdesk.request.repository.RequestApprovalRepository;
import com.sae.smartdesk.request.repository.RequestRepository;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RequestCommandService {

    private static final Logger log = LoggerFactory.getLogger(RequestCommandService.class);

    private final RequestRepository requestRepository;
    private final RequestApprovalRepository approvalRepository;
    private final ApprovalWorkflowService workflowService;
    private final UserService userService;
    private final RequestAuthorizationService authorizationService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public RequestCommandService(RequestRepository requestRepository,
                                 RequestApprovalRepository approvalRepository,
                                 ApprovalWorkflowService workflowService,
                                 UserService userService,
                                 RequestAuthorizationService authorizationService,
                                 AuditService auditService,
                                 ApplicationEventPublisher eventPublisher) {
        this.requestRepository = requestRepository;
        this.approvalRepository = approvalRepository;
        this.workflowService = workflowService;
        this.userService = userService;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    public Request createAndSubmit(RequestType requestType, UUID detailId, UUID requesterId) {
        User requester = userService.getById(requesterId);
        ApprovalWorkflow workflow = workflowService.getActiveWorkflow(requestType);
        Request request = new Request();
        request.setId(UUID.randomUUID());
        request.setRequestType(requestType);
        request.setDetailId(detailId);
        request.setRequester(requester);
        request.setWorkflow(workflow);
        request.setCurrentStep(1);
        request.setStatus(RequestStatus.PENDING_APPROVAL);
        request.setSubmittedAt(Instant.now());
        request.getApprovals().clear();

        ApprovalStep firstStep = workflow.getSteps().stream()
            .min(Comparator.comparingInt(ApprovalStep::getStepOrder))
            .orElseThrow(() -> new BadRequestException("Workflow has no steps"));
        request.setDueAt(request.getSubmittedAt().plus(Duration.ofHours(firstStep.getSlaHours())));

        workflow.getSteps().stream()
            .sorted(Comparator.comparingInt(ApprovalStep::getStepOrder))
            .forEach(step -> {
                RequestApproval approval = new RequestApproval();
                approval.setId(UUID.randomUUID());
                approval.setRequest(request);
                approval.setStepOrder(step.getStepOrder());
                request.getApprovals().add(approval);
            });

        Request saved = requestRepository.save(request);
        auditService.record(requesterId, AuditAction.REQUEST_CREATE, "REQUEST", saved.getId(), requestType.name());
        auditService.record(requesterId, AuditAction.REQUEST_SUBMIT, "REQUEST", saved.getId(), null);
        eventPublisher.publishEvent(new RequestLifecycleEvent(saved, RequestStatus.CREATED, RequestStatus.PENDING_APPROVAL, requesterId, null));
        log.info("Created request {} of type {}", saved.getId(), saved.getRequestType());
        return saved;
    }

    public Request approve(UUID requestId, UUID approverId, String comment) {
        Request request = getRequestOrThrow(requestId);
        ensurePending(request);
        User approver = userService.getById(approverId);
        authorizationService.ensureCanApprove(request, approver);
        RequestApproval currentApproval = findCurrentApproval(request);
        if (currentApproval.getDecision() != null) {
            throw new ConflictException("Approval already decided");
        }
        currentApproval.setDecision(ApprovalDecision.APPROVED);
        currentApproval.setApprover(approver);
        currentApproval.setComment(comment);
        currentApproval.setDecidedAt(Instant.now());
        approvalRepository.save(currentApproval);
        auditService.record(approverId, AuditAction.REQUEST_APPROVE, "REQUEST", request.getId(), "step=" + currentApproval.getStepOrder());

        int nextStep = request.getCurrentStep() + 1;
        boolean hasNext = request.getWorkflow().getSteps().stream()
            .anyMatch(step -> step.getStepOrder() == nextStep);
        if (hasNext) {
            request.setCurrentStep(nextStep);
            int slaHours = request.getWorkflow().getSteps().stream()
                .filter(step -> step.getStepOrder() == nextStep)
                .map(ApprovalStep::getSlaHours)
                .findFirst().orElse(24);
            request.setDueAt(Instant.now().plus(Duration.ofHours(slaHours)));
        } else {
            RequestStatus previousStatus = request.getStatus();
            request.setCurrentStep(null);
            request.setStatus(RequestStatus.APPROVED);
            request.setDueAt(null);
            eventPublisher.publishEvent(new RequestLifecycleEvent(request, previousStatus, RequestStatus.APPROVED, approverId, comment));
        }
        Request saved = requestRepository.save(request);
        boolean finalStep = !hasNext;
        eventPublisher.publishEvent(new RequestApprovalEvent(saved, currentApproval.getStepOrder(), approverId, finalStep));
        log.info("Request {} approved by {}", request.getId(), approver.getUsername());
        return saved;
    }

    public Request reject(UUID requestId, UUID approverId, String comment) {
        Request request = getRequestOrThrow(requestId);
        ensurePending(request);
        User approver = userService.getById(approverId);
        authorizationService.ensureCanReject(request, approver);
        RequestApproval currentApproval = findCurrentApproval(request);
        if (currentApproval.getDecision() != null) {
            throw new ConflictException("Approval already decided");
        }
        currentApproval.setDecision(ApprovalDecision.REJECTED);
        currentApproval.setApprover(approver);
        currentApproval.setComment(comment);
        currentApproval.setDecidedAt(Instant.now());
        approvalRepository.save(currentApproval);
        request.setStatus(RequestStatus.REJECTED);
        request.setCurrentStep(null);
        request.setDueAt(null);
        Request saved = requestRepository.save(request);
        auditService.record(approverId, AuditAction.REQUEST_REJECT, "REQUEST", request.getId(), comment);
        eventPublisher.publishEvent(new RequestLifecycleEvent(saved, RequestStatus.PENDING_APPROVAL, RequestStatus.REJECTED, approverId, comment));
        log.info("Request {} rejected by {}", request.getId(), approver.getUsername());
        return saved;
    }

    public Request markInProgress(UUID requestId, UUID actorId) {
        Request request = getRequestOrThrow(requestId);
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new ConflictException("Request not approved yet");
        }
        User actor = userService.getById(actorId);
        authorizationService.ensureCanView(request, actor);
        RequestStatus previousStatus = request.getStatus();
        request.setStatus(RequestStatus.IN_PROGRESS);
        Request saved = requestRepository.save(request);
        auditService.record(actorId, AuditAction.REQUEST_PROGRESS, "REQUEST", request.getId(), null);
        eventPublisher.publishEvent(new RequestLifecycleEvent(saved, previousStatus, RequestStatus.IN_PROGRESS, actorId, null));
        return saved;
    }

    public Request complete(UUID requestId, UUID actorId) {
        Request request = getRequestOrThrow(requestId);
        if (request.getStatus() != RequestStatus.IN_PROGRESS && request.getStatus() != RequestStatus.APPROVED) {
            throw new ConflictException("Request cannot be completed");
        }
        User actor = userService.getById(actorId);
        authorizationService.ensureCanView(request, actor);
        RequestStatus previousStatus = request.getStatus();
        request.setStatus(RequestStatus.COMPLETED);
        request.setClosedAt(Instant.now());
        Request saved = requestRepository.save(request);
        auditService.record(actorId, AuditAction.REQUEST_COMPLETE, "REQUEST", request.getId(), null);
        eventPublisher.publishEvent(new RequestLifecycleEvent(saved, previousStatus, RequestStatus.COMPLETED, actorId, null));
        return saved;
    }

    private Request getRequestOrThrow(UUID id) {
        return requestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Request not found"));
    }

    private void ensurePending(Request request) {
        if (request.getStatus() != RequestStatus.PENDING_APPROVAL) {
            throw new ConflictException("Request not pending approval");
        }
    }

    private RequestApproval findCurrentApproval(Request request) {
        return request.getApprovals().stream()
            .filter(approval -> approval.getStepOrder() == request.getCurrentStep())
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Current approval step not found"));
    }
}
