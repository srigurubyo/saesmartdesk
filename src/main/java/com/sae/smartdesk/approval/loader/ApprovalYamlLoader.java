package com.sae.smartdesk.approval.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ApprovalYamlLoader {

    private static final Logger log = LoggerFactory.getLogger(ApprovalYamlLoader.class);
    private static final String WORKFLOW_YAML = "approval/workflows.yaml";

    private final ObjectMapper objectMapper;

    public ApprovalYamlLoader() {
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        this.objectMapper.findAndRegisterModules();
    }

    public List<WorkflowDefinition> loadWorkflows() {
        ClassPathResource resource = new ClassPathResource(WORKFLOW_YAML);
        if (!resource.exists()) {
            log.warn("Workflow YAML not found at {}", WORKFLOW_YAML);
            return Collections.emptyList();
        }
        try (InputStream inputStream = resource.getInputStream()) {
            WorkflowFile file = objectMapper.readValue(inputStream, WorkflowFile.class);
            return file.getWorkflows() != null ? file.getWorkflows() : Collections.emptyList();
        } catch (IOException e) {
            log.error("Failed to read workflow YAML", e);
            return Collections.emptyList();
        }
    }
}
