package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.TokenIssueFormat;

/**
 * Security 访问Token Context
 */
public class LightningAccessTokenContext extends DefaultLightningTokenContext {

    public LightningAccessTokenContext(DefaultLightningTokenContext context) {
        super(context.getContexts());
    }

    public TokenIssueFormat getAccessTokenIssueFormat() {
        return getTokenSettings().getAccessTokenIssueFormat();
    }

}
