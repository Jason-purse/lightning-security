package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.central.oauth2.authorization.server.configuration.OAuth2ConfigurationSettingNames;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.TokenSettingsProperties;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.config.ConfigurationSettingNames;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/18
 * @time 14:07
 * @Description oauth2 Server token settings ...
 */
public final class OAuth2ServerTokenSettings extends TokenSettingsProperties {

    protected OAuth2ServerTokenSettings(Map<String, Object> settings) {
        super(settings);
    }
    

    public SignatureAlgorithm getIdTokenSignatureAlgorithm() {
        // 直接使用 spring oauth2 ConfigurationSettingNames 配置信息 ..
        return (SignatureAlgorithm)this.getSetting(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM);
    }

    public static Builder builder() {
        return (new Builder())
                .accessTokenTimeToLive(Duration.ofMillis(AuthorizationServerComponentProperties.TokenSettings.AccessToken.DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE))
                .accessTokenIssueFormat(TokenIssueFormat.SELF_CONTAINED)
                .reuseRefreshTokens(true)
                .refreshTokenTimeToLive(Duration.ofMillis(AuthorizationServerComponentProperties.TokenSettings.RefreshToken.DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE))
                .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256);
    }

    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return (Builder)(new Builder()).settings((s) -> {
            s.putAll(settings);
        });
    }

    public static class Builder extends TokenSettingsProperties.Builder {
        private Builder() {
        }

        public Builder accessTokenTimeToLive(Duration accessTokenTimeToLive) {
            return (Builder) super.accessTokenTimeToLive(accessTokenTimeToLive);
        }

        public Builder accessTokenIssueFormat(TokenIssueFormat accessTokenIssueTokenFormat) {
            return (Builder) super.accessTokenIssueFormat(accessTokenIssueTokenFormat);
        }

        public Builder reuseRefreshTokens(boolean reuseRefreshTokens) {
           return (Builder) super.reuseRefreshTokens(reuseRefreshTokens);
        }

        public Builder refreshTokenTimeToLive(Duration refreshTokenTimeToLive) {
            return (Builder) super.refreshTokenTimeToLive(refreshTokenTimeToLive);
        }

        public Builder idTokenSignatureAlgorithm(SignatureAlgorithm idTokenSignatureAlgorithm) {
            Assert.notNull(idTokenSignatureAlgorithm, "idTokenSignatureAlgorithm cannot be null");
            return (Builder)this.setting(OAuth2ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM, idTokenSignatureAlgorithm);
        }

        public OAuth2ServerTokenSettings build() {
            return new OAuth2ServerTokenSettings(this.getSettings());
        }
    }
}
