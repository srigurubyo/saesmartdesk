package com.sae.smartdesk.common.event;

import com.sae.smartdesk.request.entity.Request;
import java.util.UUID;

public class RequestApprovalEvent {

    private final Request request;
    private final int stepOrder;
    private final UUID actorId;
    private final boolean finalStep;

    public RequestApprovalEvent(Request request, int stepOrder, UUID actorId, boolean finalStep) {
        this.request = request;
        this.stepOrder = stepOrder;
        this.actorId = actorId;
        this.finalStep = finalStep;
    }

    public Request getRequest() {
        return request;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public UUID getActorId() {
        return actorId;
    }

    public boolean isFinalStep() {
        return finalStep;
    }
}
