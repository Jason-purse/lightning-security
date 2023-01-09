package com.generatera.authorization.server.common.configuration.authorization;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType;
import org.springframework.lang.Nullable;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 13:54
 * @Description 基于 OAuth2 抽象的 Authorization 的服务
 *
 * 主要是为了存储 授权中心派发的 Token 以及删除 ..
 */
public interface LightningAuthorizationService<T extends LightningAuthorization> {

    void save(T authorization);

    void remove(T authorization);

    @Nullable
    T findAuthorizationById(String id);

    @Nullable
    T findByToken(String token, @Nullable LightningAuthenticationTokenType tokenType);
}
