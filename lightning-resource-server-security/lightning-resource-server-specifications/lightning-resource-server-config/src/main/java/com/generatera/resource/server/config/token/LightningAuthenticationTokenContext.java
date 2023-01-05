package com.generatera.resource.server.config.token;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:05
 * @Description 认证上下文 ...
 */
public class LightningAuthenticationTokenContext implements Context {
    private final Map<Object, Object> context;

    public LightningAuthenticationTokenContext(Authentication authentication, @Nullable Map<Object, Object> context) {
        Assert.notNull(authentication, "authentication cannot be null");
        Map<Object, Object> ctx = new HashMap<>();
        if (!CollectionUtils.isEmpty(context)) {
            ctx.putAll(context);
        }

        ctx.put(Authentication.class, authentication);
        this.context = Collections.unmodifiableMap(ctx);
    }

    public LightningAuthenticationTokenContext(Map<Object, Object> context) {
        Assert.notEmpty(context, "context cannot be empty");
        Assert.notNull(context.get(Authentication.class), "authentication cannot be null");
        this.context = Map.copyOf(context);
    }

    public <T extends Authentication> T getAuthentication() {
        return (T)this.get(Authentication.class);
    }

    @Nullable
    public <V> V get(Object key) {
        return this.hasKey(key) ? (V)this.context.get(key) : null;
    }

    public boolean hasKey(Object key) {
        Assert.notNull(key, "key cannot be null");
        return this.context.containsKey(key);
    }

    protected abstract static class AbstractBuilder<T extends LightningAuthenticationTokenContext, B extends LightningAuthenticationTokenContext.AbstractBuilder<T, B>> {
        private final Map<Object, Object> context = new HashMap<>();

        protected AbstractBuilder(Authentication authentication) {
            Assert.notNull(authentication, "authentication cannot be null");
            this.put(Authentication.class, authentication);
        }

        public B put(Object key, Object value) {
            Assert.notNull(key, "key cannot be null");
            Assert.notNull(value, "value cannot be null");
            this.getContext().put(key, value);
            return this.getThis();
        }

        public B context(Consumer<Map<Object, Object>> contextConsumer) {
            contextConsumer.accept(this.getContext());
            return this.getThis();
        }

        protected <V> V get(Object key) {
            return (V)this.getContext().get(key);
        }

        protected Map<Object, Object> getContext() {
            return this.context;
        }

        protected final B getThis() {
            return (B)this;
        }

        public abstract T build();
    }
}