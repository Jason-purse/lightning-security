package com.generatera.authorization.application.server.config.authorization.store;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;

/**
 * 可以开启是否懒惰解析 ..
 * <p>
 * 暂时不用 ..
 */
public class LazyAuthenticationTokenService<T extends LightningAuthorization> implements LightningAuthenticationTokenService {

    interface TokenClearer {
        void clearInvalidToken();
    }

    public LazyAuthenticationTokenService(
            LightningAuthenticationTokenService tokenService,
            TokenClearer tokenClearAction
    ) {
        this.delegate = tokenService;
        this.tokenClearAction = tokenClearAction;
    }

    private final LightningAuthenticationTokenService delegate;

    private final TokenClearer tokenClearAction;

    private boolean enableClearToken = true;

    public void setEnableClearToken(boolean enableClearToken) {
        this.enableClearToken = enableClearToken;
    }

    @Override
    public void save(DefaultLightningAuthorization authorization) {
        clearInvalidTokens();
        this.delegate.save(authorization);
    }

    @Override
    public void remove(DefaultLightningAuthorization authorization) {
        clearInvalidTokens();
        this.delegate.remove(authorization);
    }

    @Override
    public DefaultLightningAuthorization findAuthorizationById(String id) {
        clearInvalidTokens();
        return this.delegate.findAuthorizationById(id);
    }

    @Override
    public DefaultLightningAuthorization findByToken(String token, LightningTokenType.LightningAuthenticationTokenType tokenType) {
        clearInvalidTokens();
        return this.delegate.findByToken(token, tokenType);
    }

    private void clearInvalidTokens() {
        if(enableClearToken) {
            clearInvalidTokens0();
        }
    }

    protected void clearInvalidTokens0() {
        if (tokenClearAction != null) {
            // 执行 token 清理动作 ..
            tokenClearAction.clearInvalidToken();
        }
    }
}
