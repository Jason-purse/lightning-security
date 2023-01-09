package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:50
 * @Description Lightning access token generator
 */
public interface LightningAccessTokenGenerator extends LightningTokenGenerator<LightningToken.LightningAccessToken> {


    class LightningAuthenticationAccessToken extends DelegateLightningToken implements LightningToken.LightningAccessToken {

        private final LightningTokenValueType tokenValueType;

        public LightningAuthenticationAccessToken(LightningToken delegate,
                                                  LightningTokenValueType tokenValueType) {
            super(delegate);
            this.tokenValueType = tokenValueType;
        }

        public LightningTokenValueType getTokenValueType() {
            return tokenValueType;
        }
    }
}



