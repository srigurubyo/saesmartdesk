package com.sae.smartdesk.auth.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginReturnsJwtWithoutMfaInDevProfile() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "requestor1",
                    "password", "password"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.mfaRequired").value(false))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "requestor1",
                    "password", "wrong-password"
                ))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }
}
