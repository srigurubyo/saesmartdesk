package com.sae.smartdesk.request.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sae.smartdesk.request.repository.RequestRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class RequestWorkflowIntegrationTest {

    private static final String REQUESTOR = "requestor1";
    private static final String REQUESTOR_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestRepository requestRepository;

    @Test
    void hallBookingApprovalHappyPath() throws Exception {
        String requestorToken = login(REQUESTOR);
        UUID hallBookingId = submitHallBooking(requestorToken);
        UUID requestId = requestRepository.findByDetailId(hallBookingId).orElseThrow().getId();

        assertRequestStatus(requestorToken, requestId, "PENDING_APPROVAL", 1, false);

        String hodToken = login("hod1");
        approve(requestId, hodToken, "Looks good");
        assertRequestStatus(requestorToken, requestId, "PENDING_APPROVAL", 2, false);

        String adminToken = login("admin1");
        approve(requestId, adminToken, "Final approval");
        assertRequestStatus(requestorToken, requestId, "APPROVED", null, true);
    }

    @Test
    void hallBookingRejectedAtFirstStep() throws Exception {
        String requestorToken = login(REQUESTOR);
        UUID hallBookingId = submitHallBooking(requestorToken);
        UUID requestId = requestRepository.findByDetailId(hallBookingId).orElseThrow().getId();

        reject(requestId, login("hod1"), "Not available");
        assertRequestStatus(requestorToken, requestId, "REJECTED", null, true);
    }

    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", username,
                    "password", REQUESTOR_PASSWORD
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = node.path("data").path("accessToken").asText();
        Assertions.assertFalse(token.isEmpty(), "access token must be present");
        return token;
    }

    private UUID submitHallBooking(String token) throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        MvcResult result = mockMvc.perform(post("/hall-bookings")
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of(
                    "hallId", "33333333-3333-3333-3333-333333333333",
                    "startDatetime", start.toString(),
                    "endDatetime", end.toString(),
                    "layout", "TEST-LAYOUT",
                    "participantCount", 12,
                    "equipmentList", "Projector",
                    "purpose", "Integration test booking"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(node.path("data").path("id").asText());
    }

    private void approve(UUID requestId, String token, String comment) throws Exception {
        mockMvc.perform(post("/requests/" + requestId + "/approve")
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("comment", comment))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    private void reject(UUID requestId, String token, String comment) throws Exception {
        mockMvc.perform(post("/requests/" + requestId + "/reject")
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("comment", comment))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    private void assertRequestStatus(String token, UUID requestId, String expectedStatus, Integer expectedStep, boolean expectDueNull)
        throws Exception {
        MvcResult result = mockMvc.perform(get("/requests/" + requestId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value(expectedStatus))
            .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode currentStepNode = node.path("data").path("currentStep");
        if (expectedStep == null) {
            Assertions.assertTrue(currentStepNode.isNull(), "currentStep should be null");
        } else {
            Assertions.assertEquals(expectedStep.intValue(), currentStepNode.asInt(), "currentStep mismatch");
        }
        JsonNode dueAtNode = node.path("data").path("dueAt");
        if (expectDueNull) {
            Assertions.assertTrue(dueAtNode.isNull(), "dueAt should be null");
        } else {
            Assertions.assertTrue(dueAtNode.isTextual(), "dueAt expected to have value");
        }
    }
}
