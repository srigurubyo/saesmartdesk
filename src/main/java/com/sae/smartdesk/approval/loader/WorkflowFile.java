package com.sae.smartdesk.approval.loader;

import java.util.List;

public class WorkflowFile {

    private List<WorkflowDefinition> workflows;

    public List<WorkflowDefinition> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<WorkflowDefinition> workflows) {
        this.workflows = workflows;
    }
}
