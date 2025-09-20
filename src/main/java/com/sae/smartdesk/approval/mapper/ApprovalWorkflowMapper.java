package com.sae.smartdesk.approval.mapper;

import com.sae.smartdesk.approval.dto.ApprovalStepDto;
import com.sae.smartdesk.approval.dto.ApprovalWorkflowDto;
import com.sae.smartdesk.approval.entity.ApprovalStep;
import com.sae.smartdesk.approval.entity.ApprovalWorkflow;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApprovalWorkflowMapper {

    @Mapping(target = "requestType", expression = "java(workflow.getRequestType().name())")
    ApprovalWorkflowDto toDto(ApprovalWorkflow workflow);

    @Mapping(target = "order", source = "stepOrder")
    ApprovalStepDto toDto(ApprovalStep step);

    List<ApprovalStepDto> toStepDtos(List<ApprovalStep> steps);
}
