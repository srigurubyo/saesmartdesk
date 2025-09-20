package com.sae.smartdesk.auth.security;

import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.config.properties.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private final SecurityProperties.Jwt properties;
    private final Key signingKey;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.properties = securityProperties.getJwt();
        this.signingKey = buildKey(securityProperties.getJwt().getSecret());
    }

    public TokenWithExpiry generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTokenValiditySeconds());
        Set<String> roles = new HashSet<>();
        user.getRoles().forEach(role -> roles.add(role.getName()));

        String token = Jwts.builder()
            .setId(UUID.randomUUID().toString())
            .setIssuer(properties.getIssuer())
            .setSubject(user.getId().toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .claim("username", user.getUsername())
            .claim("roles", roles)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
        return new TokenWithExpiry(token, expiry);
    }

    public Optional<JwtPayload> parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
            UUID userId = UUID.fromString(claims.getSubject());
            String username = claims.get("username", String.class);
            Set<String> roles = extractRoles(claims.get("roles"));
            Instant expiresAt = claims.getExpiration().toInstant();
            return Optional.of(new JwtPayload(userId, username, roles, expiresAt));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Key buildKey(String secret) {
        if (secret == null || secret.length() < 32) {
            byte[] keyBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(padKey(keyBytes));
        }
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(decoded);
        } catch (IllegalArgumentException ex) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    private byte[] padKey(byte[] bytes) {
        byte[] padded = new byte[64];
        for (int i = 0; i < padded.length; i++) {
            padded[i] = i < bytes.length ? bytes[i] : (byte) (i * 31);
        }
        return padded;
    }

    private Set<String> extractRoles(Object rolesClaim) {
        Set<String> roles = new HashSet<>();
        if (rolesClaim instanceof Collection<?> collection) {
            collection.forEach(item -> {
                if (item != null) {
                    roles.add(item.toString());
                }
            });
        }
        return roles;
    }

    public record TokenWithExpiry(String token, Instant expiresAt) {
    }

    public record JwtPayload(UUID userId, String username, Set<String> roles, Instant expiresAt) {
    }
}
