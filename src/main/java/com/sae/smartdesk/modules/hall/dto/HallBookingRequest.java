package com.sae.smartdesk.modules.hall.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record HallBookingRequest(
    @NotNull UUID hallId,
    @NotNull @Future Instant startDatetime,
    @NotNull @Future Instant endDatetime,
    @Size(max = 120) String layout,
    @Positive int participantCount,
    @Size(max = 2000) String equipmentList,
    @Size(max = 500) String purpose
) {
}
