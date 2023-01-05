package com.generatera.resource.server.config.token;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:01
 * @Description Token 验证器 ..
 */
public interface LightningAuthenticationTokenValidator {

    public static final String TOKEN_HEADER_NAME = "Authorization";

    void validate(LightningAuthenticationTokenContext authenticationContext) throws LightningAuthenticationException;
}
