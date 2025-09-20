package com.sae.smartdesk.notify.repository;

import com.sae.smartdesk.common.enums.NotificationStatus;
import com.sae.smartdesk.notify.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findTop50ByStatusOrderByCreatedAt(NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.sentAt = :sentAt WHERE n.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") NotificationStatus status, @Param("sentAt") Instant sentAt);
}
