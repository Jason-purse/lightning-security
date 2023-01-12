package com.generatera.security.authorization.server.specification.components.token.format.opaque;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 14:35
 * @Description  lightning opaque token 检测器 ..
 */
public interface LightningOpaqueTokenIntrospector {
    LightningUserPrincipal introspect(String token);
}
