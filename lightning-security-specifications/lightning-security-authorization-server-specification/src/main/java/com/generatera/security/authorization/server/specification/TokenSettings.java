package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueFormat;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import com.generatera.security.authorization.server.specification.components.token.SignatureAlgorithm;
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
 *
 * 对于 oauth2 授权服务器来说, 这个token 仅仅是作为 兜底策略 ...
 *
 * 所以还有待修改
 * TODO: 2023/1/17  oauth2 和普通授权服务器的分别处理 ..
 */
public final class TokenSettings extends AbstractSettings {

    private TokenSettings(Map<String, Object> settings) {
        super(settings);
    }

    public Duration getAccessTokenTimeToLive() {
        return (Duration)this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
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


    public TokenIssueFormat getRefreshTokenIssueFormat() {
        return this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
    }

    public LightningTokenValueType getRefreshTokenValueType() {
        return this.getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_TYPE);
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

    public List<String> getAudiences() {
        return this.getSetting(ConfigurationSettingNames.Token.AUDIENCE);
    }

    public static Builder builder() {
        return (new Builder()).accessTokenTimeToLive(Duration.ofMinutes(5L)).accessTokenIssueFormat(TokenIssueFormat.SELF_CONTAINED).reuseRefreshTokens(true).refreshTokenTimeToLive(Duration.ofMinutes(60L)).idTokenSignatureAlgorithm(SignatureAlgorithm.RS256);
    }

    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return (Builder)(new Builder()).settings((s) -> {
            s.putAll(settings);
        });
    }

    public static class Builder extends AbstractBuilder<TokenSettings, Builder> {
        private Builder() {
        }

        public Builder accessTokenTimeToLive(Duration accessTokenTimeToLive) {
            Assert.notNull(accessTokenTimeToLive, "accessTokenTimeToLive cannot be null");
            Assert.isTrue(accessTokenTimeToLive.getSeconds() > 0L, "accessTokenTimeToLive must be greater than Duration.ZERO");
            return (Builder)this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, accessTokenTimeToLive);
        }

        public Builder accessTokenIssueFormat(TokenIssueFormat accessTokenIssueTokenFormat) {
            Assert.notNull(accessTokenIssueTokenFormat, "accessTokenFormat cannot be null");
            return (Builder)this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, accessTokenIssueTokenFormat);
        }


        public Builder accessTokenValueType(LightningTokenValueType tokenValueType) {
            Assert.notNull(tokenValueType, "tokenValueType cannot be null");
            return (Builder)this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_TYPE, tokenValueType);
        }

        public Builder accessTokenValueFormat(LightningTokenValueFormat tokenValueFormat) {
            Assert.notNull(tokenValueFormat, "tokenValueFormat cannot be null");
            return this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_FORMAT,tokenValueFormat);
        }


        public Builder refreshTokenIssueFormat(TokenIssueFormat accessTokenIssueTokenFormat) {
            Assert.notNull(accessTokenIssueTokenFormat, "accessTokenFormat cannot be null");
            return (Builder)this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, accessTokenIssueTokenFormat);
        }


        public Builder refreshTokenValueType(LightningTokenValueType tokenValueType) {
            Assert.notNull(tokenValueType, "tokenValueType cannot be null");
            return (Builder)this.setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_VALUE_TYPE, tokenValueType);
        }

        public Builder reuseRefreshTokens(boolean reuseRefreshTokens) {
            return (Builder)this.setting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, reuseRefreshTokens);
        }

        public Builder refreshTokenTimeToLive(Duration refreshTokenTimeToLive) {
            Assert.notNull(refreshTokenTimeToLive, "refreshTokenTimeToLive cannot be null");
            Assert.isTrue(refreshTokenTimeToLive.getSeconds() > 0L, "refreshTokenTimeToLive must be greater than Duration.ZERO");
            return (Builder)this.setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, refreshTokenTimeToLive);
        }

        public Builder idTokenSignatureAlgorithm(SignatureAlgorithm idTokenSignatureAlgorithm) {
            Assert.notNull(idTokenSignatureAlgorithm, "idTokenSignatureAlgorithm cannot be null");
            return (Builder)this.setting(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM, idTokenSignatureAlgorithm);
        }

        public Builder audience(String... audiences) {
            Assert.notNull(audiences,"audience must not be null !!!");
            return this.setting(ConfigurationSettingNames.Token.AUDIENCE, Arrays.asList(audiences));
        }

        public TokenSettings build() {
            return new TokenSettings(this.getSettings());
        }
    }
}