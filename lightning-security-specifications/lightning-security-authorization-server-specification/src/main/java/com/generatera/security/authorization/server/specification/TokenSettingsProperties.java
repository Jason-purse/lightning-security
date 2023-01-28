package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueFormat;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 15:51
 * @Description Token 的基本配置 ..
 * <p>
 * 对于 oauth2 授权服务器来说,需要扩展配置 ...
 */
public class TokenSettingsProperties extends AbstractSettings {

    protected TokenSettingsProperties(Map<String, Object> settings) {
        super(settings);
    }

    public Duration getAccessTokenTimeToLive() {
        return this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
    }

    public TokenIssueFormat getAccessTokenIssueFormat() {
        return this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
    }

    public LightningTokenValueType getAccessTokenValueType() {
        return this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_TYPE);
    }

    public LightningTokenValueFormat getAccessTokenValueFormat() {
        return this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_FORMAT);
    }


    public List<LightningTokenType.LightningAuthenticationTokenType> getGrantTypes() {
        return this.getSetting(ConfigurationSettingNames.Token.GRANT_TYPES);
    }

    /**
     * 刷新token 和 token 访问 token value Type 是一致的类型 ...
     *
     * @return
     */
    public LightningTokenValueFormat getRefreshTokenValueFormat() {
        return this.getSetting(ConfigurationSettingNames.Token.REFRESH_TOKEN_VALUE_FORMAT);
    }

    public LightningTokenValueType getRefreshTokenValueType() {
        return this.getSetting(ConfigurationSettingNames.Token.REFRESH_TOKEN_VALUE_TYPE);
    }


    public boolean isReuseRefreshToken() {
        return this.getSetting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKEN);
    }

    public Duration getRefreshTokenTimeToLive() {
        return this.getSetting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
    }


    public List<String> getAudiences() {
        return this.getSetting(ConfigurationSettingNames.Token.AUDIENCE);
    }

    public static Builder builder() {
        return (new Builder())
                .accessTokenTimeToLive(Duration.ofMinutes(5L))
                .accessTokenIssueFormat(TokenIssueFormat.SELF_CONTAINED)
                .reuseRefreshTokens(true)
                .refreshTokenTimeToLive(Duration.ofMinutes(60L));
    }

    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return (new Builder()).settings((s) -> {
            s.putAll(settings);
        });
    }

    public static class Builder extends AbstractBuilder<TokenSettingsProperties, Builder> {
        protected Builder() {
        }

        public Builder accessTokenTimeToLive(Duration accessTokenTimeToLive) {
            Assert.notNull(accessTokenTimeToLive, "accessTokenTimeToLive cannot be null");
            Assert.isTrue(accessTokenTimeToLive.getSeconds() > 0L, "accessTokenTimeToLive must be greater than Duration.ZERO");
            return this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, accessTokenTimeToLive);
        }

        public Builder accessTokenIssueFormat(TokenIssueFormat accessTokenIssueTokenFormat) {
            Assert.notNull(accessTokenIssueTokenFormat, "accessTokenFormat cannot be null");
            return this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, accessTokenIssueTokenFormat);
        }


        public Builder accessTokenValueType(LightningTokenValueType tokenValueType) {
            Assert.notNull(tokenValueType, "tokenValueType cannot be null");
            return this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_TYPE, tokenValueType);
        }

        public Builder accessTokenValueFormat(LightningTokenValueFormat tokenValueFormat) {
            Assert.notNull(tokenValueFormat, "tokenValueFormat cannot be null");
            return this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_FORMAT, tokenValueFormat);
        }


        public Builder refreshTokenValueType(LightningTokenValueType tokenValueType) {
            Assert.notNull(tokenValueType, "tokenValueType cannot be null");
            return this.setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_VALUE_TYPE, tokenValueType);
        }

        public Builder refreshTokenValueFormat(LightningTokenValueFormat tokenValueFormat) {
            Assert.notNull(tokenValueFormat, "tokenValueFormat cannot be null");
            return this.setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_VALUE_FORMAT, tokenValueFormat);
        }

        public Builder reuseRefreshTokens(boolean reuseRefreshTokens) {
            return this.setting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKEN, reuseRefreshTokens);
        }

        public Builder refreshTokenTimeToLive(Duration refreshTokenTimeToLive) {
            Assert.notNull(refreshTokenTimeToLive, "refreshTokenTimeToLive cannot be null");
            Assert.isTrue(refreshTokenTimeToLive.getSeconds() > 0L, "refreshTokenTimeToLive must be greater than Duration.ZERO");
            return this.setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, refreshTokenTimeToLive);
        }

        public Builder grantTypes(LightningTokenType.LightningAuthenticationTokenType... types) {
            return this.setting(ConfigurationSettingNames.Token.GRANT_TYPES, Arrays.asList(types));
        }


        public Builder audience(String... audiences) {
            Assert.notNull(audiences, "audience must not be null !!!");
            return this.setting(ConfigurationSettingNames.Token.AUDIENCE, Arrays.asList(audiences));
        }

        public TokenSettingsProperties build() {
            return new TokenSettingsProperties(this.getSettings());
        }
    }
}