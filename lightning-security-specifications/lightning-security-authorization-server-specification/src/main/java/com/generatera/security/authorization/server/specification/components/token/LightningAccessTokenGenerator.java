package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueFormat;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:50
 * @Description Lightning access token generator
 */
public interface LightningAccessTokenGenerator extends LightningTokenGenerator<LightningToken.LightningAccessToken> {


    class LightningAuthenticationAccessToken extends DelegateLightningToken implements LightningToken.LightningAccessToken {

        private final LightningTokenValueType tokenValueType;

        private final LightningTokenValueFormat tokenValueTypeFormat;

        public LightningAuthenticationAccessToken(LightningToken delegate,
                                                  LightningTokenValueType tokenValueType,
                                                  LightningTokenValueFormat tokenValueTypeFormat) {
            super(delegate);
            this.tokenValueType = tokenValueType;
            this.tokenValueTypeFormat = tokenValueTypeFormat;
        }

        public LightningTokenValueType getTokenValueType() {
            return tokenValueType;
        }

        @Override
        public LightningTokenValueFormat getTokenValueFormat() {
            return tokenValueTypeFormat;
        }
    }
}



