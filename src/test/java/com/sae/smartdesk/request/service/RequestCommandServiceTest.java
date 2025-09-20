package com.sae.smartdesk.request.service;

import com.sae.smartdesk.approval.entity.ApprovalStep;
import com.sae.smartdesk.approval.entity.ApprovalWorkflow;
import com.sae.smartdesk.approval.service.ApprovalWorkflowService;
import com.sae.smartdesk.auth.entity.Role;
import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.service.UserService;
import com.sae.smartdesk.audit.service.AuditService;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.enums.RequestType;
import com.sae.smartdesk.common.event.RequestLifecycleEvent;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.entity.RequestApproval;
import com.sae.smartdesk.request.repository.RequestApprovalRepository;
import com.sae.smartdesk.request.repository.RequestRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RequestCommandServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestApprovalRepository approvalRepository;

    @Mock
    private ApprovalWorkflowService workflowService;

    @Mock
    private UserService userService;

    @Mock
    private RequestAuthorizationService authorizationService;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RequestCommandService requestCommandService;

    private Request request;
    private RequestApproval approval;
    private User approver;

    @BeforeEach
    void setUp() {
        request = new Request();
        request.setId(UUID.randomUUID());
        request.setRequestType(RequestType.HALL_BOOKING);
        request.setStatus(RequestStatus.PENDING_APPROVAL);
        request.setCurrentStep(2);

        ApprovalStep step1 = new ApprovalStep();
        step1.setStepOrder(1);
        step1.setApproverRole("HOD");
        step1.setSlaHours(24);
        ApprovalStep step2 = new ApprovalStep();
        step2.setStepOrder(2);
        step2.setApproverRole("ADMIN");
        step2.setSlaHours(24);
        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setSteps(List.of(step1, step2));
        request.setWorkflow(workflow);

        approval = new RequestApproval();
        approval.setId(UUID.randomUUID());
        approval.setRequest(request);
        approval.setStepOrder(2);
        request.setApprovals(List.of(approval));

        approver = new User();
        approver.setId(UUID.randomUUID());
        approver.setUsername("admin1");
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");
        approver.setRoles(Set.of(adminRole));
    }

    @Test
    void approveFinalStepTransitionsToApproved() {
        UUID requestId = request.getId();
        Mockito.when(requestRepository.findById(requestId)).thenReturn(java.util.Optional.of(request));
        Mockito.when(userService.getById(approver.getId())).thenReturn(approver);
        Mockito.when(requestRepository.save(Mockito.any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestCommandService.approve(requestId, approver.getId(), "ok");

        Assertions.assertEquals(RequestStatus.APPROVED, request.getStatus());
        Assertions.assertNull(request.getCurrentStep());

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(eventPublisher, Mockito.atLeastOnce()).publishEvent(eventCaptor.capture());
        boolean lifecyclePublished = eventCaptor.getAllValues().stream()
            .anyMatch(event -> event instanceof RequestLifecycleEvent lifecycleEvent && lifecycleEvent.getNewStatus() == RequestStatus.APPROVED);
        Assertions.assertTrue(lifecyclePublished);
    }
}
