package com.generatera.authorization.server.common.configuration.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 13:55
 * @Description Lightning Authentication Token 仓库
 *
 * 用于管理  Lightning Authentication Token (继承 BasedOAuth2AuthorizationService 只是为了接洽语义)
 *
 * 表明它是受 OAuth2AuthorizationService 启发而来 ...
 */
public interface LightningAuthenticationTokenService extends LightningAuthorizationService<DefaultLightningAuthorization> {

}
