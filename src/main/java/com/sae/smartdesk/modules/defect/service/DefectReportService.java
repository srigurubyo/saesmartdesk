package com.sae.smartdesk.modules.defect.service;

import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.service.UserService;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.enums.RequestType;
import com.sae.smartdesk.common.exception.ForbiddenException;
import com.sae.smartdesk.common.exception.NotFoundException;
import com.sae.smartdesk.modules.defect.dto.DefectReportRequest;
import com.sae.smartdesk.modules.defect.dto.DefectReportResponse;
import com.sae.smartdesk.modules.defect.entity.DefectReport;
import com.sae.smartdesk.modules.defect.mapper.DefectReportMapper;
import com.sae.smartdesk.modules.defect.repository.DefectReportRepository;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.repository.RequestRepository;
import com.sae.smartdesk.request.service.RequestAuthorizationService;
import com.sae.smartdesk.request.service.RequestCommandService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DefectReportService {

    private static final Logger log = LoggerFactory.getLogger(DefectReportService.class);

    private final DefectReportRepository defectReportRepository;
    private final RequestRepository requestRepository;
    private final RequestCommandService requestCommandService;
    private final RequestAuthorizationService authorizationService;
    private final UserService userService;
    private final DefectReportMapper mapper;

    public DefectReportService(DefectReportRepository defectReportRepository,
                               RequestRepository requestRepository,
                               RequestCommandService requestCommandService,
                               RequestAuthorizationService authorizationService,
                               UserService userService,
                               DefectReportMapper mapper) {
        this.defectReportRepository = defectReportRepository;
        this.requestRepository = requestRepository;
        this.requestCommandService = requestCommandService;
        this.authorizationService = authorizationService;
        this.userService = userService;
        this.mapper = mapper;
    }

    public DefectReportResponse createReport(DefectReportRequest request, UUID reporterId) {
        DefectReport report = new DefectReport();
        report.setId(UUID.randomUUID());
        report.setBuilding(request.building());
        report.setDefectType(request.defectType());
        report.setSeverity(request.severity() != null ? request.severity() : "NORMAL");
        report.setDescription(request.description());
        report.setPhotoUrls(request.photoUrls());
        report.setReportedAt(Instant.now());
        defectReportRepository.save(report);
        Request linkedRequest = requestCommandService.createAndSubmit(RequestType.DEFECT_REPORT, report.getId(), reporterId);
        log.info("Defect report {} submitted with request {}", report.getId(), linkedRequest.getId());
        return mapper.toResponse(report, linkedRequest.getStatus().name());
    }

    public DefectReportResponse getReport(UUID id, UUID actorId) {
        DefectReport report = defectReportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Defect report not found"));
        Request request = requestRepository.findByDetailId(report.getId())
            .orElseThrow(() -> new NotFoundException("Request not found for defect report"));
        User actor = userService.getById(actorId);
        authorizationService.ensureCanView(request, actor);
        return mapper.toResponse(report, request.getStatus().name());
    }

    public DefectReportResponse startWork(UUID id, UUID actorId) {
        Request request = getLinkedRequest(id);
        User actor = userService.getById(actorId);
        ensureUnitOfficer(actor);
        requestCommandService.markInProgress(request.getId(), actorId);
        Request reloaded = requestRepository.findById(request.getId())
            .orElseThrow(() -> new NotFoundException("Request not found"));
        DefectReport report = defectReportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Defect report not found"));
        return mapper.toResponse(report, reloaded.getStatus().name());
    }

    public DefectReportResponse finishWork(UUID id, UUID actorId) {
        Request request = getLinkedRequest(id);
        User actor = userService.getById(actorId);
        ensureUnitOfficer(actor);
        requestCommandService.complete(request.getId(), actorId);
        Request reloaded = requestRepository.findById(request.getId())
            .orElseThrow(() -> new NotFoundException("Request not found"));
        DefectReport report = defectReportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Defect report not found"));
        return mapper.toResponse(report, reloaded.getStatus().name());
    }

    private Request getLinkedRequest(UUID detailId) {
        return requestRepository.findByDetailId(detailId)
            .orElseThrow(() -> new NotFoundException("Linked request not found"));
    }

    private void ensureUnitOfficer(User user) {
        boolean allowed = user.getRoles().stream()
            .map(role -> role.getName().toUpperCase(Locale.ROOT))
            .anyMatch(name -> name.equals("UNIT_OFFICER") || name.equals("ADMIN"));
        if (!allowed) {
            throw new ForbiddenException("User not authorized for this action");
        }
    }
}
