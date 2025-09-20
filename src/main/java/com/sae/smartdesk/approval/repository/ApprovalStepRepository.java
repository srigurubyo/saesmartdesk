package com.sae.smartdesk.approval.repository;

import com.sae.smartdesk.approval.entity.ApprovalStep;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, UUID> {

    List<ApprovalStep> findByWorkflowIdOrderByStepOrder(UUID workflowId);
}
