package com.sae.smartdesk.request.dto;

import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.enums.RequestType;
import java.time.Instant;

public record RequestSearchCriteria(
    RequestType type,
    RequestStatus status,
    Instant from,
    Instant to,
    Boolean mine
) {
}
