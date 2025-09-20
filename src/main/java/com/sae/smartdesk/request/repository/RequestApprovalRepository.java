package com.sae.smartdesk.request.repository;

import com.sae.smartdesk.request.entity.RequestApproval;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestApprovalRepository extends JpaRepository<RequestApproval, UUID> {

    List<RequestApproval> findByRequestIdOrderByStepOrder(UUID requestId);
}
