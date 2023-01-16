package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.TokenIssueFormat;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 15:20
 * @Description 刷新Token上下文 ...
 */
public class LightningRefreshTokenContext extends DefaultLightningTokenContext {
    public LightningRefreshTokenContext(DefaultLightningTokenContext tokenContext) {
        super(tokenContext.getContexts());
    }

    // todo
    public TokenIssueFormat getRefreshTokenIssueFormat() {
        return getTokenSettings().getRefreshTokenIssueFormat();
    }
}
