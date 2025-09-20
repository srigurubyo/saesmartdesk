package com.sae.smartdesk.feedback.repository;

import com.sae.smartdesk.feedback.entity.Feedback;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findByRequestIdOrderByCreatedAtAsc(UUID requestId);
}
