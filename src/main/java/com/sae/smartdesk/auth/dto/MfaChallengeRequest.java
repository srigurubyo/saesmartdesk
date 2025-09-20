package com.sae.smartdesk.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaChallengeRequest(
    @NotBlank String mfaToken,
    @NotBlank @Pattern(regexp = "^\\d{6}$") String code
) {
}
