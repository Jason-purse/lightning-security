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


    /**
     * 基于代理Token 实现,不管Token 它本身存在什么东西 ..
     * 组成部分其实也就是 基础token信息 加上当前访问token 需要的一些信息 ...
     *
     * 例如token value 类型 ->  Bearer
     * 例如token value 格式 -> jwt / opaque
     */
    class LightningAuthenticationAccessToken extends DelegateLightningToken implements LightningToken.LightningAccessToken {

        private final LightningTokenValueType tokenValueType;

        private final LightningTokenValueFormat tokenValueFormat;

        public LightningAuthenticationAccessToken(LightningToken delegate,
                                                  LightningTokenValueType tokenValueType,
                                                  LightningTokenValueFormat tokenValueFormat) {
            super(delegate);
            this.tokenValueType = tokenValueType;
            this.tokenValueFormat = tokenValueFormat;
        }

        public LightningTokenValueType getTokenValueType() {
            return tokenValueType;
        }

        @Override
        public LightningTokenValueFormat getTokenValueFormat() {
            return tokenValueFormat;
        }
    }
}



