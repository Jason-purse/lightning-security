package com.generatera.resource.server.config.token;

import javax.servlet.http.HttpServletRequest;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:14
 * @Description token 解析器 ..
 */
@FunctionalInterface
public interface LightningAuthenticationTokenResolver {

    public static final String TOKEN_IDENTITY_NAME = "Authorization";

    String resolve(HttpServletRequest request);
}