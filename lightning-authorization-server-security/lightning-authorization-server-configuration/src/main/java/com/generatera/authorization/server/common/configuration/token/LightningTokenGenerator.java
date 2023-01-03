package com.generatera.authorization.server.common.configuration.token;

/**
 * Token Generator ..
 * 提供 token generator
 */
public interface LightningTokenGenerator<T extends LightningToken> {

    T generate(LightningSecurityContext context);
}
