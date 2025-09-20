package com.sae.smartdesk.common.event;

import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.common.enums.RequestStatus;
import java.util.UUID;

public class RequestLifecycleEvent {

    private final Request request;
    private final RequestStatus previousStatus;
    private final RequestStatus newStatus;
    private final UUID actorId;
    private final String comment;

    public RequestLifecycleEvent(Request request, RequestStatus previousStatus, RequestStatus newStatus,
                                 UUID actorId, String comment) {
        this.request = request;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.actorId = actorId;
        this.comment = comment;
    }

    public Request getRequest() {
        return request;
    }

    public RequestStatus getPreviousStatus() {
        return previousStatus;
    }

    public RequestStatus getNewStatus() {
        return newStatus;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getComment() {
        return comment;
    }
}
