package com.generatera.security.authorization.server.specification.components.token;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/16
 * @time 14:47
 * @Description 默认的TokenContext
 *
 * 其他的token 上下文以它 进行扩展 ...
 */
public class DefaultLightningTokenContext implements LightningTokenContext {

    private final Map<Object, Object> context;

    protected DefaultLightningTokenContext(Map<Object, Object> context) {
        this.context = Map.copyOf(context);
    }

    public Map<Object, Object> getContexts() {
        return context;
    }


    @Nullable
    @SuppressWarnings("unchecked")
    public <V> V get(Object key) {
        return this.hasKey(key) ? (V)this.context.get(key) : null;
    }

    public boolean hasKey(Object key) {
        Assert.notNull(key, "key cannot be null");
        return this.context.containsKey(key);
    }

    public static DefaultLightningTokenContext.Builder builder() {
        return new DefaultLightningTokenContext.Builder();
    }

    public static final class Builder extends AbstractBuilder<DefaultLightningTokenContext, DefaultLightningTokenContext.Builder> {
        private Builder() {
        }

        public DefaultLightningTokenContext build() {
            return new DefaultLightningTokenContext(this.getContext());
        }
    }
}
