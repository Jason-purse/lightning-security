package com.generatera.authorization.server.common.configuration.token;

/**
 *
 * 这一部分内容属于 resource server的东西 ..
 *
 * 但是作为一个token 颁发者,自然自己需要能够解析token ...
 *
 * lightningAuthentication Token parser
 */
public interface LightningAuthenticationTokenParser {

    /**
     * 解析出 LightningAuthenticationToken
     * @param parseContext 解析上下文
     * @return authentication token
     */
    LightningAuthenticationToken parse(LightningAuthenticationParseContext parseContext);
}
