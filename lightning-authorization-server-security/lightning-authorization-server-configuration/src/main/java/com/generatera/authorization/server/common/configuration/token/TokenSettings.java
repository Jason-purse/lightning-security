package com.generatera.authorization.server.common.configuration.token;

import org.springframework.security.oauth2.core.OAuth2TokenFormat;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class TokenSettings extends AbstractSettings {

    private TokenSettings(Map<String, Object> settings) {
        super(settings);
    }

    public Duration getAccessTokenTimeToLive() {
        return (Duration)this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
    }

    public OAuthTokenFormat getAccessTokenFormat() {
        return (OAuthTokenFormat)this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
    }

    public boolean isReuseRefreshTokens() {
        return (Boolean)this.getSetting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS);
    }

    public Duration getRefreshTokenTimeToLive() {
        return (Duration)this.getSetting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
    }

    public SignatureAlgorithm getIdTokenSignatureAlgorithm() {
        return (SignatureAlgorithm)this.getSetting(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM);
    }

    public List<String> getAudience() {
        return null;
    }

    public static TokenSettings.Builder builder() {
        return (new TokenSettings.Builder()).accessTokenTimeToLive(Duration.ofMinutes(5L)).accessTokenFormat(OAuthTokenFormat.SELF_CONTAINED).reuseRefreshTokens(true).refreshTokenTimeToLive(Duration.ofMinutes(60L)).idTokenSignatureAlgorithm(SignatureAlgorithm.RS256);
    }

    public static TokenSettings.Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return (TokenSettings.Builder)(new TokenSettings.Builder()).settings((s) -> {
            s.putAll(settings);
        });
    }

    public static class Builder extends AbstractBuilder<TokenSettings, TokenSettings.Builder> {
        private Builder() {
        }

        public TokenSettings.Builder accessTokenTimeToLive(Duration accessTokenTimeToLive) {
            Assert.notNull(accessTokenTimeToLive, "accessTokenTimeToLive cannot be null");
            Assert.isTrue(accessTokenTimeToLive.getSeconds() > 0L, "accessTokenTimeToLive must be greater than Duration.ZERO");
            return (TokenSettings.Builder)this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, accessTokenTimeToLive);
        }

        public TokenSettings.Builder accessTokenFormat(OAuthTokenFormat accessTokenFormat) {
            Assert.notNull(accessTokenFormat, "accessTokenFormat cannot be null");
            return (TokenSettings.Builder)this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, accessTokenFormat);
        }

        public TokenSettings.Builder reuseRefreshTokens(boolean reuseRefreshTokens) {
            return (TokenSettings.Builder)this.setting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, reuseRefreshTokens);
        }

        public TokenSettings.Builder refreshTokenTimeToLive(Duration refreshTokenTimeToLive) {
            Assert.notNull(refreshTokenTimeToLive, "refreshTokenTimeToLive cannot be null");
            Assert.isTrue(refreshTokenTimeToLive.getSeconds() > 0L, "refreshTokenTimeToLive must be greater than Duration.ZERO");
            return (TokenSettings.Builder)this.setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, refreshTokenTimeToLive);
        }

        public TokenSettings.Builder idTokenSignatureAlgorithm(SignatureAlgorithm idTokenSignatureAlgorithm) {
            Assert.notNull(idTokenSignatureAlgorithm, "idTokenSignatureAlgorithm cannot be null");
            return (TokenSettings.Builder)this.setting(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM, idTokenSignatureAlgorithm);
        }

        public TokenSettings build() {
            return new TokenSettings(this.getSettings());
        }
    }
}