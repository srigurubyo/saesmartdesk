package com.sae.smartdesk.auth.service;

import com.sae.smartdesk.auth.dto.LoginRequest;
import com.sae.smartdesk.auth.dto.LoginResponse;
import com.sae.smartdesk.auth.dto.MfaChallengeRequest;
import com.sae.smartdesk.auth.dto.MfaEnrollResponse;
import com.sae.smartdesk.auth.dto.ProfileResponse;
import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.mapper.UserMapper;
import com.sae.smartdesk.auth.security.JwtTokenService;
import com.sae.smartdesk.common.exception.BadRequestException;
import com.sae.smartdesk.common.exception.ForbiddenException;
import com.sae.smartdesk.common.exception.NotFoundException;
import com.sae.smartdesk.config.properties.SecurityProperties;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.secret.SecretGenerator;
import jakarta.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final MfaEnrollmentCache mfaEnrollmentCache;
    private final CodeVerifier codeVerifier;
    private final SecretGenerator secretGenerator;
    private final SecurityProperties securityProperties;
    private final UserMapper userMapper;

    public AuthService(AuthenticationManager authenticationManager, UserService userService,
                       JwtTokenService jwtTokenService, MfaEnrollmentCache mfaEnrollmentCache,
                       CodeVerifier codeVerifier, SecretGenerator secretGenerator,
                       SecurityProperties securityProperties, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.mfaEnrollmentCache = mfaEnrollmentCache;
        this.codeVerifier = codeVerifier;
        this.secretGenerator = secretGenerator;
        this.securityProperties = securityProperties;
        this.userMapper = userMapper;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        if (!authentication.isAuthenticated()) {
            throw new ForbiddenException("Invalid credentials");
        }
        User user = userService.findByUsername(request.username())
            .orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.isEnabled()) {
            throw new ForbiddenException("Account disabled");
        }
        Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        if (securityProperties.isDisableMfa()) {
            JwtTokenService.TokenWithExpiry tokenWithExpiry = jwtTokenService.generateAccessToken(user);
            return new LoginResponse(true, false, false, tokenWithExpiry.token(), tokenWithExpiry.expiresAt(), null, roles);
        }
        if (user.getMfaTotpSecret() == null || user.getMfaTotpSecret().isBlank()) {
            JwtTokenService.TokenWithExpiry tokenWithExpiry = jwtTokenService.generateAccessToken(user);
            return new LoginResponse(true, false, true, tokenWithExpiry.token(), tokenWithExpiry.expiresAt(), null, roles);
        }
        String mfaToken = mfaEnrollmentCache.put(user.getId(), user.getMfaTotpSecret(), false);
        return new LoginResponse(true, true, false, null, null, mfaToken, roles);
    }

    public MfaEnrollResponse enroll(UUID userId) {
        User user = userService.getById(userId);
        if (user.getMfaTotpSecret() != null && !user.getMfaTotpSecret().isBlank()) {
            throw new BadRequestException("MFA already enabled");
        }
        String secret = secretGenerator.generate();
        String otpauthUrl = buildOtpAuthUrl(user.getUsername(), secret);
        String mfaToken = mfaEnrollmentCache.put(user.getId(), secret, true);
        log.info("Generated MFA enrollment secret for user {}", user.getUsername());
        return new MfaEnrollResponse(secret, otpauthUrl, mfaToken);
    }

    public LoginResponse challenge(MfaChallengeRequest request) {
        if (securityProperties.isDisableMfa()) {
            throw new BadRequestException("MFA is disabled in this environment");
        }
        MfaEnrollmentCache.Session session = mfaEnrollmentCache.get(request.mfaToken())
            .orElseThrow(() -> new BadRequestException("Invalid or expired MFA token"));
        User user = userService.getById(session.userId());
        String secret = session.secret();
        boolean enrollmentFlow = session.enrollment();
        if (!enrollmentFlow) {
            secret = user.getMfaTotpSecret();
        }
        if (secret == null || secret.isBlank()) {
            throw new BadRequestException("MFA not configured");
        }
        boolean validCode = codeVerifier.isValidCode(secret, request.code());
        if (!validCode) {
            throw new ForbiddenException("Invalid MFA code");
        }
        if (enrollmentFlow) {
            user.setMfaTotpSecret(secret);
            userService.save(user);
            log.info("User {} enabled MFA", user.getUsername());
        }
        mfaEnrollmentCache.remove(request.mfaToken());
        JwtTokenService.TokenWithExpiry tokenWithExpiry = jwtTokenService.generateAccessToken(user);
        Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        return new LoginResponse(true, false, false, tokenWithExpiry.token(), tokenWithExpiry.expiresAt(), null, roles);
    }

    public ProfileResponse currentProfile(UUID userId) {
        User user = userService.getById(userId);
        return userMapper.toProfile(user);
    }

    private String buildOtpAuthUrl(String username, String secret) {
        SecurityProperties.Totp totp = securityProperties.getTotp();
        String issuer = urlEncode(totp.getIssuer());
        String label = urlEncode(totp.getIssuer() + ":" + username);
        return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + issuer +
            "&period=" + totp.getPeriod() + "&digits=" + totp.getDigits();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
