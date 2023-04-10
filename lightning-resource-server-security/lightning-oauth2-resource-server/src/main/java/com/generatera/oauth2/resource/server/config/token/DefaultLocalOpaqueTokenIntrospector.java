package com.generatera.oauth2.resource.server.config.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;

/**
 * 实现本地处理 ...
 */
public class DefaultLocalOpaqueTokenIntrospector implements LightningOAuth2OpaqueTokenIntrospector {


    @Override
    public LightningUserPrincipal introspect(String token) {
        return null;
    }
}
