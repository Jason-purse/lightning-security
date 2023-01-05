package com.generatera.resource.server.config.token;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public interface Context {
    @Nullable
    <V> V get(Object key);

    @Nullable
    default <V> V get(Class<V> key) {
        Assert.notNull(key, "key cannot be null");
        V value = this.get((Object)key);
        return key.isInstance(value) ? value : null;
    }

    boolean hasKey(Object key);
}