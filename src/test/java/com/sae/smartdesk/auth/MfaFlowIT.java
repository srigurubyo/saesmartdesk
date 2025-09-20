package com.sae.smartdesk.auth;

import com.sae.smartdesk.AbstractIntegrationTest;
import com.sae.smartdesk.auth.dto.LoginRequest;
import com.sae.smartdesk.auth.dto.LoginResponse;
import com.sae.smartdesk.auth.dto.MfaChallengeRequest;
import com.sae.smartdesk.auth.dto.MfaEnrollResponse;
import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.repository.UserRepository;
import com.sae.smartdesk.auth.service.AuthService;
import com.sae.smartdesk.config.properties.SecurityProperties;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MfaFlowIT extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CodeGenerator codeGenerator;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    void enrollAndChallengeMfa() {
        LoginResponse loginResponse = authService.login(new LoginRequest("requestor1", "password"));
        Assertions.assertTrue(loginResponse.mfaEnrollmentRequired());
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        MfaEnrollResponse enrollResponse = authService.enroll(userId);
        Assertions.assertNotNull(enrollResponse.secret());
        Assertions.assertNotNull(enrollResponse.mfaToken());

        long timeWindow = Instant.now().getEpochSecond() / securityProperties.getTotp().getPeriod();
        String code;
        try {
            code = codeGenerator.generate(enrollResponse.secret(), timeWindow);
        } catch (CodeGenerationException e) {
            throw new IllegalStateException("Unable to generate TOTP for test", e);
        }

        LoginResponse finalResponse = authService.challenge(new MfaChallengeRequest(enrollResponse.mfaToken(), code));
        Assertions.assertNotNull(finalResponse.accessToken());
        Assertions.assertFalse(finalResponse.mfaRequired());

        Optional<User> updated = userRepository.findById(userId);
        Assertions.assertTrue(updated.isPresent());
        Assertions.assertNotNull(updated.get().getMfaTotpSecret());
    }
}