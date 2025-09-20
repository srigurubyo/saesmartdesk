package com.sae.smartdesk.modules.hall.repository;

import com.sae.smartdesk.modules.hall.entity.HallBooking;
import com.sae.smartdesk.common.enums.RequestStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HallBookingRepository extends JpaRepository<HallBooking, UUID> {

    @Query("SELECT hb FROM HallBooking hb JOIN Request r ON r.detailId = hb.id " +
        "WHERE hb.hall.id = :hallId AND r.status IN :statuses " +
        "AND hb.startDatetime < :end AND hb.endDatetime > :start")
    List<HallBooking> findOverlapping(@Param("hallId") UUID hallId,
                                      @Param("start") Instant start,
                                      @Param("end") Instant end,
                                      @Param("statuses") List<RequestStatus> statuses);
}
