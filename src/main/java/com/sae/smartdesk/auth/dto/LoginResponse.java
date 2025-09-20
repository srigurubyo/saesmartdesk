package com.sae.smartdesk.auth.dto;

import java.time.Instant;
import java.util.Set;

public record LoginResponse(
    boolean success,
    boolean mfaRequired,
    boolean mfaEnrollmentRequired,
    String accessToken,
    Instant accessTokenExpiresAt,
    String mfaToken,
    Set<String> roles
) {
}
