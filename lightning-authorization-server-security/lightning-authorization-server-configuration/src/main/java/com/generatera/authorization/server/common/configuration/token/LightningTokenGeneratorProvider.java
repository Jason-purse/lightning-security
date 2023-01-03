package com.generatera.authorization.server.common.configuration.token;

import org.springframework.lang.NonNull;

/**
 * Token Generator ..
 * 提供 token generator
 */
public interface LightningTokenGeneratorProvider {

    @NonNull
    Object get();

}
