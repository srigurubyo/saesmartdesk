package com.sae.smartdesk.approval.service;

import com.sae.smartdesk.approval.entity.ApprovalStep;
import com.sae.smartdesk.approval.entity.ApprovalWorkflow;
import com.sae.smartdesk.approval.loader.ApprovalYamlLoader;
import com.sae.smartdesk.approval.loader.WorkflowDefinition;
import com.sae.smartdesk.approval.repository.ApprovalWorkflowRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ApprovalRefreshService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalRefreshService.class);

    private final ApprovalWorkflowRepository workflowRepository;
    private final ApprovalYamlLoader yamlLoader;

    public ApprovalRefreshService(ApprovalWorkflowRepository workflowRepository, ApprovalYamlLoader yamlLoader) {
        this.workflowRepository = workflowRepository;
        this.yamlLoader = yamlLoader;
    }

    public int refreshFromYaml() {
        List<WorkflowDefinition> definitions = yamlLoader.loadWorkflows();
        int updated = 0;
        Set<UUID> activeIds = new HashSet<>();
        for (WorkflowDefinition definition : definitions) {
            ApprovalWorkflow workflow = workflowRepository.findById(definition.getId())
                .orElseGet(() -> {
                    ApprovalWorkflow wf = new ApprovalWorkflow();
                    wf.setId(definition.getId() != null ? definition.getId() : UUID.randomUUID());
                    return wf;
                });
            workflow.setRequestType(definition.getRequestType());
            workflow.setVersion(definition.getVersion());
            workflow.setActive(definition.isActive());
            List<ApprovalStep> steps = new ArrayList<>();
            if (definition.getSteps() != null) {
                for (WorkflowDefinition.StepDefinition stepDefinition : definition.getSteps()) {
                    ApprovalStep step = new ApprovalStep();
                    step.setId(stepDefinition.getId() != null ? stepDefinition.getId() : UUID.randomUUID());
                    step.setWorkflow(workflow);
                    step.setStepOrder(stepDefinition.getOrder());
                    step.setApproverRole(stepDefinition.getApproverRole());
                    step.setSlaHours(stepDefinition.getSlaHours());
                    steps.add(step);
                }
            }
            workflow.getSteps().clear();
            workflow.getSteps().addAll(steps);
            workflowRepository.save(workflow);
            activeIds.add(workflow.getId());
            updated++;
        }
        log.info("Refreshed {} workflows from YAML", updated);
        return updated;
    }
}
