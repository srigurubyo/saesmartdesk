package com.sae.smartdesk.request.repository;

import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.request.entity.Request;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RequestRepository extends JpaRepository<Request, UUID>, JpaSpecificationExecutor<Request> {

    @EntityGraph(attributePaths = {"approvals", "approvals.approver", "requester", "workflow", "workflow.steps"})
    Optional<Request> findDetailedById(UUID id);

    Optional<Request> findByDetailId(UUID detailId);

    List<Request> findByStatusAndDueAtBefore(RequestStatus status, Instant dueAt);
}
