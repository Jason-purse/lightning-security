package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtClaimsSet;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import com.jianyue.lightning.boot.starter.generic.crud.service.support.ThreadLocalSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DefaultOpaqueAwareTokenCustomizer implements LightningTokenCustomizer<LightningTokenContext> {

    private List<String> authoritiesName = Arrays.asList(JwtExtClaimNames.SCOPE_CLAIM,JwtExtClaimNames.SCOPE_SHORT_CLAIM);

    public void setAuthoritiesName(List<String> authoritiesName) {
        Assert.notNull(authoritiesName,"authoritiesName must not be null !!!");
        this.authoritiesName = authoritiesName;
    }

    @Override
    public void customize(LightningTokenContext context) {

        if (context instanceof JwtEncodingContext jwtEncodingContext) {
            if ( LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value().equalsIgnoreCase(context.getTokenType().value())) {
                JwtClaimsSet.Builder claims = jwtEncodingContext.getClaims();
                claims.claims(claimsHandle());
            }

        } else if (context instanceof LightningTokenClaimsContext tokenClaimsContext) {
            if (LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value().equalsIgnoreCase(context.getTokenType().value())) {
                LightningTokenClaimsSet.Builder claims = tokenClaimsContext.getClaims();
                claims.claims(claimsHandle());
            }
        }
    }


    @NotNull
    private Consumer<Map<String, Object>> claimsHandle() {
        return claims ->
        {
            Object tokenValueTypeFormat = claims.get(JwtExtClaimNames.TOKEN_VALUE_FORMAT_CLAIM);
            Assert.notNull(tokenValueTypeFormat,"tokenValueTypeFormat must not be null !!!");
            if (tokenValueTypeFormat.toString().equalsIgnoreCase(LightningTokenType.LightningTokenValueFormat.OPAQUE.value())) {

                for (String authorityName : authoritiesName) {
                    Object scope = claims.get(authorityName);
                    if (scope != null) {
                        scopeThreadLocal.set(scope);
                        claims.remove(authorityName);
                        break;
                    }
                }

                claims.put(JwtExtClaimNames.OPAQUE_CLAIM, true);
            } else {
                claims.put(JwtExtClaimNames.OPAQUE_CLAIM, false);
            }
        };
    }

    /**
     * 和线程绑定相关 ...
     */
    public static ThreadLocalSupport<Object> scopeThreadLocal = ThreadLocalSupport.Companion.of();
}
