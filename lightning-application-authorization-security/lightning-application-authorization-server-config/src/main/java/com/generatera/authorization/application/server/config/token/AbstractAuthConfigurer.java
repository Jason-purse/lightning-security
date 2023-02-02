package com.generatera.authorization.application.server.config.token;

import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.util.matcher.RequestMatcher;

public abstract class AbstractAuthConfigurer {
    private final ObjectPostProcessor<Object> objectPostProcessor;

    AbstractAuthConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        this.objectPostProcessor = objectPostProcessor;
    }

    public abstract <B extends HttpSecurityBuilder<B>> void init(B builder);

    public abstract <B extends HttpSecurityBuilder<B>> void configure(B builder);

    public abstract RequestMatcher getRequestMatcher();

    protected final <T> T postProcess(T object) {
        return this.objectPostProcessor.postProcess(object);
    }

    protected final ObjectPostProcessor<Object> getObjectPostProcessor() {
        return this.objectPostProcessor;
    }
}
