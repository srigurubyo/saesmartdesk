package com.sae.smartdesk.request.dto;

import jakarta.validation.constraints.Size;

public record RequestActionRequest(
    @Size(max = 2000) String comment
) {
}
