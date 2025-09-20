package com.sae.smartdesk.modules.hall.mapper;

import com.sae.smartdesk.modules.hall.dto.HallBookingResponse;
import com.sae.smartdesk.modules.hall.entity.HallBooking;
import org.springframework.stereotype.Component;

@Component
public class HallBookingMapper {

    public HallBookingResponse toResponse(HallBooking booking, String requestStatus) {
        return new HallBookingResponse(
            booking.getId(),
            booking.getHall().getId(),
            booking.getHall().getName(),
            booking.getHall().getLocation(),
            booking.getHall().getCapacity(),
            booking.getStartDatetime(),
            booking.getEndDatetime(),
            booking.getLayout(),
            booking.getParticipantCount(),
            booking.getEquipmentList(),
            booking.getPurpose(),
            requestStatus
        );
    }
}
