package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import org.springframework.lang.NonNull;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:55
 * @Description authorization server refresh token generator
 */
public interface LightningRefreshTokenGenerator extends LightningTokenGenerator<LightningToken.LightningRefreshToken> {

    class LightningAuthenticationRefreshToken extends DelegateLightningToken implements LightningToken.LightningRefreshToken {

        public LightningAuthenticationRefreshToken(LightningToken delegate) {
            super(delegate);
        }
    }

}
