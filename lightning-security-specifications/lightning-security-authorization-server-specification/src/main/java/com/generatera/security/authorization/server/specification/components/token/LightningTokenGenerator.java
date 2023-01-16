package com.generatera.security.authorization.server.specification.components.token;

/**
 * Token Generator ..
 * 提供 token generator
 */
public interface LightningTokenGenerator<T extends LightningToken> {

    T generate(LightningTokenContext context);
}
