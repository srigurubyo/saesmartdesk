package com.sae.smartdesk.modules.hall.repository;

import com.sae.smartdesk.modules.hall.entity.Hall;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository extends JpaRepository<Hall, UUID> {
}
