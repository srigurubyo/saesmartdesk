package com.sae.smartdesk.modules.hall.web;

import com.sae.smartdesk.auth.security.UserPrincipal;
import com.sae.smartdesk.common.dto.ApiResponse;
import com.sae.smartdesk.modules.hall.dto.HallBookingRequest;
import com.sae.smartdesk.modules.hall.dto.HallBookingResponse;
import com.sae.smartdesk.modules.hall.service.HallBookingService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hall-bookings")
public class HallBookingController {

    private final HallBookingService hallBookingService;

    public HallBookingController(HallBookingService hallBookingService) {
        this.hallBookingService = hallBookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REQUESTOR','ADMIN')")
    public ResponseEntity<ApiResponse<HallBookingResponse>> create(@Valid @RequestBody HallBookingRequest request,
                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        HallBookingResponse response = hallBookingService.createBooking(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HallBookingResponse>> get(@PathVariable UUID id,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        HallBookingResponse response = hallBookingService.getBooking(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
