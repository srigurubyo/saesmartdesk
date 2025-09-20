package com.sae.smartdesk.modules.hall.service;

import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.service.UserService;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.enums.RequestType;
import com.sae.smartdesk.common.exception.BadRequestException;
import com.sae.smartdesk.common.exception.ConflictException;
import com.sae.smartdesk.common.exception.NotFoundException;
import com.sae.smartdesk.modules.hall.dto.HallBookingRequest;
import com.sae.smartdesk.modules.hall.dto.HallBookingResponse;
import com.sae.smartdesk.modules.hall.entity.Hall;
import com.sae.smartdesk.modules.hall.entity.HallBooking;
import com.sae.smartdesk.modules.hall.mapper.HallBookingMapper;
import com.sae.smartdesk.modules.hall.repository.HallBookingRepository;
import com.sae.smartdesk.modules.hall.repository.HallRepository;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.repository.RequestRepository;
import com.sae.smartdesk.request.service.RequestAuthorizationService;
import com.sae.smartdesk.request.service.RequestCommandService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class HallBookingService {

    private static final Logger log = LoggerFactory.getLogger(HallBookingService.class);
    private static final List<RequestStatus> OVERLAP_STATUSES = List.of(
        RequestStatus.APPROVED,
        RequestStatus.IN_PROGRESS,
        RequestStatus.COMPLETED
    );

    private final HallRepository hallRepository;
    private final HallBookingRepository hallBookingRepository;
    private final RequestRepository requestRepository;
    private final RequestCommandService requestCommandService;
    private final RequestAuthorizationService authorizationService;
    private final HallBookingMapper mapper;
    private final UserService userService;

    public HallBookingService(HallRepository hallRepository,
                              HallBookingRepository hallBookingRepository,
                              RequestRepository requestRepository,
                              RequestCommandService requestCommandService,
                              RequestAuthorizationService authorizationService,
                              HallBookingMapper mapper,
                              UserService userService) {
        this.hallRepository = hallRepository;
        this.hallBookingRepository = hallBookingRepository;
        this.requestRepository = requestRepository;
        this.requestCommandService = requestCommandService;
        this.authorizationService = authorizationService;
        this.mapper = mapper;
        this.userService = userService;
    }

    public HallBookingResponse createBooking(HallBookingRequest request, UUID requesterId) {
        if (request.startDatetime().isAfter(request.endDatetime())) {
            throw new BadRequestException("Start time must be before end time");
        }
        Hall hall = hallRepository.findById(request.hallId())
            .orElseThrow(() -> new NotFoundException("Hall not found"));
        if (request.participantCount() > hall.getCapacity()) {
            throw new BadRequestException("Participant count exceeds hall capacity");
        }
        validateOverlap(hall.getId(), request.startDatetime(), request.endDatetime());
        HallBooking booking = new HallBooking();
        booking.setId(UUID.randomUUID());
        booking.setHall(hall);
        booking.setStartDatetime(request.startDatetime());
        booking.setEndDatetime(request.endDatetime());
        booking.setLayout(request.layout());
        booking.setParticipantCount(request.participantCount());
        booking.setEquipmentList(request.equipmentList());
        booking.setPurpose(request.purpose());
        hallBookingRepository.save(booking);
        Request linkedRequest = requestCommandService.createAndSubmit(RequestType.HALL_BOOKING, booking.getId(), requesterId);
        log.info("Hall booking {} submitted with request {}", booking.getId(), linkedRequest.getId());
        return mapper.toResponse(booking, linkedRequest.getStatus().name());
    }

    public HallBookingResponse getBooking(UUID bookingId, UUID actorId) {
        HallBooking booking = hallBookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Hall booking not found"));
        Request request = requestRepository.findByDetailId(booking.getId())
            .orElseThrow(() -> new NotFoundException("Request not found for booking"));
        User actor = userService.getById(actorId);
        authorizationService.ensureCanView(request, actor);
        return mapper.toResponse(booking, request.getStatus().name());
    }

    private void validateOverlap(UUID hallId, Instant start, Instant end) {
        List<HallBooking> conflicts = hallBookingRepository.findOverlapping(hallId, start, end, OVERLAP_STATUSES);
        if (!conflicts.isEmpty()) {
            throw new ConflictException("Hall already booked for the selected time range");
        }
    }
}
