package com.sae.smartdesk.modules.hall.service;

import com.sae.smartdesk.common.exception.ConflictException;
import com.sae.smartdesk.modules.hall.dto.HallBookingRequest;
import com.sae.smartdesk.modules.hall.entity.Hall;
import com.sae.smartdesk.modules.hall.entity.HallBooking;
import com.sae.smartdesk.modules.hall.mapper.HallBookingMapper;
import com.sae.smartdesk.modules.hall.repository.HallBookingRepository;
import com.sae.smartdesk.modules.hall.repository.HallRepository;
import com.sae.smartdesk.request.repository.RequestRepository;
import com.sae.smartdesk.request.service.RequestAuthorizationService;
import com.sae.smartdesk.request.service.RequestCommandService;
import com.sae.smartdesk.auth.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HallBookingServiceTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private HallBookingRepository hallBookingRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestCommandService requestCommandService;

    @Mock
    private RequestAuthorizationService authorizationService;

    @Mock
    private HallBookingMapper hallBookingMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private HallBookingService hallBookingService;

    @Test
    void createBookingThrowsWhenOverlapDetected() {
        UUID hallId = UUID.randomUUID();
        Hall hall = new Hall();
        hall.setId(hallId);
        hall.setCapacity(100);
        hall.setName("Test Hall");
        hall.setLocation("Building X");
        Mockito.when(hallRepository.findById(hallId)).thenReturn(Optional.of(hall));

        HallBooking conflict = new HallBooking();
        Mockito.when(hallBookingRepository.findOverlapping(Mockito.eq(hallId), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(List.of(conflict));

        HallBookingRequest request = new HallBookingRequest(hallId,
            Instant.now().plusSeconds(3600),
            Instant.now().plusSeconds(7200),
            null,
            10,
            null,
            "Overlap test");

        Assertions.assertThrows(ConflictException.class, () -> hallBookingService.createBooking(request, UUID.randomUUID()));
        Mockito.verifyNoInteractions(requestCommandService);
    }
}
