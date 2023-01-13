package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.function.Consumer;
/**
 * @author FLJ
 * @date 2023/1/13
 * @time 14:31
 * @Description 添加了 token value type 以及 token value format 类型 ...
 */
public class DefaultTokenDetailAwareOAuth2TokenCustomizer implements LightningCentralOAuth2TokenCustomizer<OAuth2TokenContext> {

    public static final String TOKEN_VALUE_TYPE_CLAIM_NAME = "tokenValueType";

    public static final String TOKEN_VALUE_FORMAT_TYPE_CLAIM_NAME = "tokenValueTypeFormat";

    private final AuthorizationServerComponentProperties.TokenSettings tokenSettings;

    public DefaultTokenDetailAwareOAuth2TokenCustomizer(AuthorizationServerComponentProperties.TokenSettings properties) {
        Assert.notNull(properties,"tokenSettings cannot be null !!!");
        this.tokenSettings = properties;
    }

    @Override
    public void customize(OAuth2TokenContext context) {
        if(context instanceof JwtEncodingContext jwtEncodingContext) {
            jwtEncodingContext.getClaims().claims(claimsHandle());
        }
        else if(context instanceof OAuth2TokenClaimsContext oAuth2TokenClaimsContext) {
            oAuth2TokenClaimsContext.getClaims().claims(claimsHandle());
        }

        // pass
    }

    @NotNull
    private Consumer<Map<String, Object>> claimsHandle() {
        return claims -> {
            LightningTokenType.LightningTokenValueType tokenValueType = tokenSettings.getTokenValueType();
            claims.put(TOKEN_VALUE_TYPE_CLAIM_NAME,tokenValueType.value());
            LightningTokenType.LightningTokenValueTypeFormat tokenValueFormat = tokenSettings.getTokenValueFormat();
            claims.put(TOKEN_VALUE_FORMAT_TYPE_CLAIM_NAME,tokenValueFormat.value());
        };
    }
}
