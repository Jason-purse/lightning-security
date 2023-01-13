package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.jianyue.lightning.boot.starter.generic.crud.service.support.ThreadLocalSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 13:14
 */
public class DefaultOpaqueAwareOAuth2TokenCustomizer implements LightningCentralOAuth2TokenCustomizer<OAuth2TokenContext> {

    /**
     * 默认 jwt ..
     */
    private LightningTokenType.LightningTokenValueTypeFormat valueTypeFormat = LightningTokenType.LightningTokenValueTypeFormat.JWT;

    @Override
    public void customize(OAuth2TokenContext context) {
        if (context instanceof JwtEncodingContext jwtEncodingContext) {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                JwtClaimsSet.Builder claims = jwtEncodingContext.getClaims();
                claims.claims(claimsHandle());
            }

        } else if (context instanceof OAuth2TokenClaimsContext oAuth2TokenClaimsContext) {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                OAuth2TokenClaimsSet.Builder claims = oAuth2TokenClaimsContext.getClaims();
                claims.claims(claimsHandle());
            }
        }
        // pass
    }

    public void setValueTypeFormat(LightningTokenType.LightningTokenValueTypeFormat valueTypeFormat) {
        Assert.notNull(valueTypeFormat, "value type format must not be null !!!");
        this.valueTypeFormat = valueTypeFormat;
    }


    @NotNull
    private Consumer<Map<String, Object>> claimsHandle() {
        return claims ->
        {
            Object tokenValueTypeFormat = claims.get(DefaultTokenDetailAwareOAuth2TokenCustomizer.TOKEN_VALUE_FORMAT_TYPE_CLAIM_NAME);
            Assert.notNull(tokenValueTypeFormat,"tokenValueTypeFormat must not be null !!!");
            if (tokenValueTypeFormat.toString().equalsIgnoreCase(LightningTokenType.LightningTokenValueTypeFormat.OPAQUE.value())) {

                Object scope = claims.get("scope");
                if (scope != null) {
                    scopeThreadLocal.set(scope);
                } else {
                    scopeThreadLocal.set(claims.get("scp"));
                }

                claims.remove("scope");
                claims.remove("scp");
                claims.put("isOpaque", true);
            } else {
                claims.put("isOpaque", false);
            }
        };
    }

    /**
     * 和线程绑定相关 ...
     */
    public static ThreadLocalSupport<Object> scopeThreadLocal = ThreadLocalSupport.Companion.of();
}
