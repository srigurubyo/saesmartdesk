package com.sae.smartdesk.approval.loader;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sae.smartdesk.common.enums.RequestType;
import java.util.List;
import java.util.UUID;

public class WorkflowDefinition {

    private UUID id;
    private RequestType requestType;
    private int version;
    private boolean active;
    private List<StepDefinition> steps;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    public void setSteps(List<StepDefinition> steps) {
        this.steps = steps;
    }

    public static class StepDefinition {
        @JsonProperty("id")
        private UUID id;
        @JsonProperty("order")
        private int order;
        private String approverRole;
        private int slaHours;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getApproverRole() {
            return approverRole;
        }

        public void setApproverRole(String approverRole) {
            this.approverRole = approverRole;
        }

        public int getSlaHours() {
            return slaHours;
        }

        public void setSlaHours(int slaHours) {
            this.slaHours = slaHours;
        }
    }
}
