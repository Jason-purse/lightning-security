package com.generatera.authorization.server.common.configuration.token;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenClaimsContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenCustomizer;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtClaimAccessor;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/17
 * @time 10:31
 * @Description 感知Opaque token 的 token生成器
 */
public class DefaultOpaqueAwareTokenCustomizer implements LightningTokenCustomizer<LightningTokenContext> {

    private final boolean isOpaque;

    private List<String> authoritiesNames = Arrays.asList(JwtExtClaimNames.SCOPE_CLAIM, JwtExtClaimNames.SCOPE_SHORT_CLAIM);


    public DefaultOpaqueAwareTokenCustomizer(boolean isOpaque) {
        this.isOpaque = isOpaque;
    }


    public void setAuthoritiesNames(List<String> authoritiesNames) {
        Assert.notEmpty(authoritiesNames, "authoritiesNames must not be null !!!");
        this.authoritiesNames = authoritiesNames;
    }

    @Override
    public void customize(LightningTokenContext tokenContext) {

        if(tokenContext instanceof LightningTokenClaimsContext tokenClaimsContext) {
            doTokenClaimsCustomize(tokenClaimsContext.getTokenType(),tokenClaimsContext.getClaims());
        }
        else if(tokenContext instanceof JwtEncodingContext jwtEncodingContext) {
            doTokenClaimsCustomize(jwtEncodingContext.getTokenType(),jwtEncodingContext.getClaims());
        }
    }

    private void doTokenClaimsCustomize(LightningTokenType.LightningAuthenticationTokenType tokenType,
                                        JwtClaimAccessor builder) {
        // 访问 token 生成判断
        if (tokenType.value().equals(LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value())) {
            if (isOpaque) {
                boolean status = false;
                for (String name : authoritiesNames) {
                    if (builder.hasClaim(name)) {
                        builder.removeClaim(name);
                        builder.claim(JwtExtClaimNames.OPAQUE_CLAIM, Boolean.TRUE);
                        status = true;
                        break;
                    }
                }

                if (!status) {
                    builder.claim(JwtExtClaimNames.OPAQUE_CLAIM, Boolean.FALSE);
                }
            }

        }
    }

}
