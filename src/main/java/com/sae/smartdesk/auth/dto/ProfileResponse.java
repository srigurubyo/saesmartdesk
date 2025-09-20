package com.sae.smartdesk.auth.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    String username,
    String email,
    String fullName,
    boolean enabled,
    boolean mfaEnabled,
    Set<String> roles,
    Instant createdAt,
    Instant updatedAt
) {
}
