package com.sae.smartdesk.modules.hall.dto;

import java.time.Instant;
import java.util.UUID;

public record HallBookingResponse(
    UUID id,
    UUID hallId,
    String hallName,
    String hallLocation,
    int hallCapacity,
    Instant startDatetime,
    Instant endDatetime,
    String layout,
    int participantCount,
    String equipmentList,
    String purpose,
    String requestStatus
) {
}
