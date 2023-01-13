package com.generatera.security.authorization.server.specification.components.token;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:15
 * @Description Token 类型
 */

public interface LightningTokenType {

    @JsonGetter
    String value();


    /**
     * 认证Token ..类型
     *
     *   1. access token
     *   2. refresh token
     */
     class LightningAuthenticationTokenType extends DefaultLightningTokenType {

        public LightningAuthenticationTokenType(String value) {
            super(value);
        }

        public static LightningAuthenticationTokenType ACCESS_TOKEN_TYPE = new LightningAuthenticationTokenType("ACCESS_TOKEN");

        public static LightningAuthenticationTokenType REFRESH_TOKEN_TYPE = new LightningAuthenticationTokenType("REFRESH_TOKE");
    }


    class LightningTokenValueType extends DefaultLightningTokenType {

         public LightningTokenValueType(String value) {super(value);}

        public static final LightningTokenValueType BEARER_TOKEN_TYPE = new LightningTokenValueType("Bearer");
    }

    /**
     * token 值类型的格式 ...
     *
     * 本质上全是 jwt ...
     */
    class LightningTokenValueTypeFormat extends DefaultLightningTokenType {

        public LightningTokenValueTypeFormat(String value) {
            super(value);
        }

        /**
         * jwt
         */
        public static final LightningTokenValueTypeFormat JWT = new LightningTokenValueTypeFormat("jwt");
        /**
         * opaque
         */
        public static final LightningTokenValueTypeFormat OPAQUE = new LightningTokenValueTypeFormat("opaque");

    }


    @Deprecated
    class LightningAccessTokenType extends DefaultLightningTokenType {

        public LightningAccessTokenType(String value) {
            super(value);
        }

        /**
         * 目前只支持 bearer ..
         */
        public static LightningAccessTokenType BEARER_TOKEN_TYPE = new LightningAccessTokenType("Bearer");

    }
    @Deprecated
    class LightningRefreshTokenType extends DefaultLightningTokenType {

        public LightningRefreshTokenType(String value) {
            super(value);
        }

        /**
         *支持简单形式
         */
        public static LightningRefreshTokenType PLAIN_TOKEN_TYPE = new LightningRefreshTokenType("PLAIN");

        public static LightningRefreshTokenType HMAC_TOKEN_TYPE = new LightningRefreshTokenType("HMAC");

        public static LightningRefreshTokenType MD5_TOKEN_TYPE = new LightningRefreshTokenType("MD5");

        public static LightningRefreshTokenType SHA256_TOKEN_TYPE = new LightningRefreshTokenType("SHA-256");

    }

}

@AllArgsConstructor
@EqualsAndHashCode
class DefaultLightningTokenType implements LightningTokenType {

    private final String value;

    @Override
    public String value() {
        return value;
    }

    public DefaultLightningTokenType of(String value) {
        return new DefaultLightningTokenType(value);
    }
}
