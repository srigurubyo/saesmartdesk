package com.sae.smartdesk.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sae.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final Totp totp = new Totp();

    public Jwt getJwt() {
        return jwt;
    }

    public Totp getTotp() {
        return totp;
    }

    public static class Jwt {
        private String issuer;
        private String secret;
        private long accessTokenValiditySeconds;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenValiditySeconds() {
            return accessTokenValiditySeconds;
        }

        public void setAccessTokenValiditySeconds(long accessTokenValiditySeconds) {
            this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        }
    }

    public static class Totp {
        private String issuer;
        private int digits;
        private int period;
        private int window;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public int getDigits() {
            return digits;
        }

        public void setDigits(int digits) {
            this.digits = digits;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        public int getWindow() {
            return window;
        }

        public void setWindow(int window) {
                this.window = window;
        }
    }
}
