package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 16:37
 * @Description  OAuth2BearerTokenAuthenticationFilterProvider
 */
final class OAuth2BearerTokenAuthenticationFilterProvider {
    private static BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter;


    static void setFilter(BearerTokenAuthenticationFilter filter) {
        bearerTokenAuthenticationFilter = filter;
    }

    @NonNull
    public static BearerTokenAuthenticationFilter getFilter() {
        return bearerTokenAuthenticationFilter;
    }
}
