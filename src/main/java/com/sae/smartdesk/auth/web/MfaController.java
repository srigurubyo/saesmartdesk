package com.sae.smartdesk.auth.web;

import com.sae.smartdesk.auth.dto.MfaEnrollResponse;
import com.sae.smartdesk.auth.security.UserPrincipal;
import com.sae.smartdesk.auth.service.AuthService;
import com.sae.smartdesk.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/mfa")
public class MfaController {

    private final AuthService authService;

    public MfaController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<MfaEnrollResponse>> enroll(@AuthenticationPrincipal UserPrincipal principal) {
        MfaEnrollResponse response = authService.enroll(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
