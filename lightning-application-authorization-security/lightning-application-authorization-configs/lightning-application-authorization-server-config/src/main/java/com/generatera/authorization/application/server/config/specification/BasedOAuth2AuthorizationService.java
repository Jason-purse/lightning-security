package com.generatera.authorization.application.server.config.specification;

import com.generatera.authorization.server.common.configuration.token.LightningAuthenticationToken;
import com.generatera.authorization.server.common.configuration.token.LightningToken;
import org.springframework.lang.Nullable;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 13:54
 * @Description 基于 OAuth2 抽象的 Authorization 的服务
 *
 * 主要是为了存储 授权中心派发的 Token 以及删除 ..
 */
public interface BasedOAuth2AuthorizationService {

    void save(LightningAuthenticationToken authorization);

    void remove(LightningAuthenticationToken authorization);

    @Nullable
    LightningAuthenticationToken findById(String id);

    @Nullable
    LightningAuthenticationToken findByToken(String token, @Nullable LightningToken.TokenType tokenType);
}
