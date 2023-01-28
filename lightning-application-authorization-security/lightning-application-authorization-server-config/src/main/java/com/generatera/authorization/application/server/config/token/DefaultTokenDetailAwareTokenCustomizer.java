package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenClaimsContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenCustomizer;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 14:31
 * @Description 添加了 token value type 以及 token value format 类型 ...
 *
 * 应该仅仅处理 access token / 或者自签名 jwt ..
 */
public class DefaultTokenDetailAwareTokenCustomizer implements LightningTokenCustomizer<LightningTokenContext> {

    private final TokenSettingsProvider tokenSettingsProvider;

    public DefaultTokenDetailAwareTokenCustomizer(TokenSettingsProvider tokenSettingsProvider) {
        Assert.notNull(tokenSettingsProvider,"tokenSettingsProvider cannot be null !!!");
        this.tokenSettingsProvider = tokenSettingsProvider;
    }

    @Override
    public void customize(LightningTokenContext context) {
        if(context instanceof JwtEncodingContext jwtEncodingContext) {
            jwtEncodingContext.getClaims().claims(claimsHandle());
        }
        else if(context instanceof LightningTokenClaimsContext oAuth2TokenClaimsContext) {
            oAuth2TokenClaimsContext.getClaims().claims(claimsHandle());
        }

        // pass
    }

    @NotNull
    private Consumer<Map<String, Object>> claimsHandle() {
        return claims -> {
            LightningTokenType.LightningTokenValueType tokenValueType = tokenSettingsProvider.getTokenSettings().getAccessTokenValueType();
            claims.put(JwtExtClaimNames.TOKEN_VALUE_TYPE_CLAIM,tokenValueType.value());
            LightningTokenType.LightningTokenValueFormat tokenValueFormat = tokenSettingsProvider.getTokenSettings().getAccessTokenValueFormat();
            claims.put(JwtExtClaimNames.TOKEN_VALUE_FORMAT_CLAIM,tokenValueFormat.value());
        };
    }
}
