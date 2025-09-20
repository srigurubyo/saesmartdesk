package com.sae.smartdesk.approval.repository;

import com.sae.smartdesk.approval.entity.ApprovalWorkflow;
import com.sae.smartdesk.common.enums.RequestType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, UUID> {

    @EntityGraph(attributePaths = "steps")
    Optional<ApprovalWorkflow> findFirstByRequestTypeAndActiveTrueOrderByVersionDesc(RequestType requestType);
}
