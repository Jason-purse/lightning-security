package com.generatera.security.authorization.server.specification.components.token.format;
/**
 * @author FLJ
 * @date 2023/1/17
 * @time 10:47
 * @Description jwt 扩展claims
 */
public interface JwtExtClaimNames {
    /**
     * 不透明token
     */
    public static final String OPAQUE_CLAIM = "opaque";

    public static final String SCOPE_CLAIM = "scope";

    public static final String SCOPE_SHORT_CLAIM = "scp";

    public static final String AUTHORITIES_CLAIM = "authorities";

    public static final String TOKEN_VALUE_TYPE_CLAIM = "token-value-type";

    public static final String TOKEN_VALUE_FORMAT_CLAIM = "toke-value-format";

}
