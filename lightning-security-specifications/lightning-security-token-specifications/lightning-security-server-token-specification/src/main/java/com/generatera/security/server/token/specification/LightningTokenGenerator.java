package com.generatera.security.server.token.specification;

/**
 * Token Generator ..
 * 提供 token generator
 */
public interface LightningTokenGenerator<T extends LightningToken,CONTEXT extends LightningSecurityContext> {

    T generate(CONTEXT context);
}
