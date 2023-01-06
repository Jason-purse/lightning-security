package com.generatera.authorization.application.server.config.specification;

import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationToken;
import com.generatera.security.server.token.specification.LightningTokenType;
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

    void save(LightningApplicationLevelAuthenticationToken authorization);

    void remove(LightningApplicationLevelAuthenticationToken authorization);

    @Nullable
    LightningApplicationLevelAuthenticationToken findById(String id);

    @Nullable
    LightningApplicationLevelAuthenticationToken findByToken(String token, @Nullable LightningTokenType.LightningAuthenticationTokenType tokenType);
}
