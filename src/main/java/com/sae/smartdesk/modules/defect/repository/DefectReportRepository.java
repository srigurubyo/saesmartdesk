package com.sae.smartdesk.modules.defect.repository;

import com.sae.smartdesk.modules.defect.entity.DefectReport;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefectReportRepository extends JpaRepository<DefectReport, UUID> {
}
