package com.sae.smartdesk.auth.web;

import com.sae.smartdesk.auth.dto.LoginRequest;
import com.sae.smartdesk.auth.dto.LoginResponse;
import com.sae.smartdesk.auth.dto.MfaChallengeRequest;
import com.sae.smartdesk.auth.service.AuthService;
import com.sae.smartdesk.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/mfa/challenge")
    public ResponseEntity<ApiResponse<LoginResponse>> challenge(@Valid @RequestBody MfaChallengeRequest request) {
        LoginResponse response = authService.challenge(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
