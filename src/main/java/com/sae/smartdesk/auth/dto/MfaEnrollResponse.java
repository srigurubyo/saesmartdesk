package com.sae.smartdesk.auth.dto;

public record MfaEnrollResponse(
    String secret,
    String otpauthUrl,
    String mfaToken
) {
}
