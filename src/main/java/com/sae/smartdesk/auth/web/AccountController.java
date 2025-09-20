package com.sae.smartdesk.auth.web;

import com.sae.smartdesk.auth.dto.ProfileResponse;
import com.sae.smartdesk.auth.security.UserPrincipal;
import com.sae.smartdesk.auth.service.AuthService;
import com.sae.smartdesk.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    private final AuthService authService;

    public AccountController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        ProfileResponse profile = authService.currentProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
