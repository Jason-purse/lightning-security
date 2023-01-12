package com.generatera.oauth2.resource.server.config.token;

import com.generatera.oauth2.resource.server.config.LightningOAuth2UserPrincipal;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.format.opaque.LightningOpaqueTokenIntrospector;
/**
 * @author FLJ
 * @date 2023/1/12
 * @time 16:59
 * @Description oauth2 opaque token ...
 */
public interface LightningOAuth2OpaqueTokenIntrospector extends LightningOpaqueTokenIntrospector  {
    @Override
    default LightningUserPrincipal introspect(String token) {
        return doIntrospect(token);
    }

    LightningOAuth2UserPrincipal doIntrospect(String token);
}
