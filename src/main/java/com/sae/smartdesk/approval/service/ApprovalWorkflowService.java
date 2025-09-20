package com.sae.smartdesk.approval.service;

import com.sae.smartdesk.approval.entity.ApprovalWorkflow;
import com.sae.smartdesk.approval.repository.ApprovalWorkflowRepository;
import com.sae.smartdesk.common.enums.RequestType;
import com.sae.smartdesk.common.exception.NotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ApprovalWorkflowService {

    private final ApprovalWorkflowRepository workflowRepository;

    public ApprovalWorkflowService(ApprovalWorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public ApprovalWorkflow getActiveWorkflow(RequestType requestType) {
        return workflowRepository.findFirstByRequestTypeAndActiveTrueOrderByVersionDesc(requestType)
            .orElseThrow(() -> new NotFoundException("Active workflow not found for type " + requestType));
    }

    public List<ApprovalWorkflow> listAll() {
        return workflowRepository.findAll();
    }
}
