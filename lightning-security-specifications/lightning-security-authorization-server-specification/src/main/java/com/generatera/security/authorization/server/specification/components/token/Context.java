package com.generatera.security.authorization.server.specification.components.token;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/17
 * @time 10:02
 * @Description 上下文抽象 接口
 *
 * 本质上能够便利的存放某些内容,能够基于上下文获取各种需要的东西 ...
 * 详情查看它的子类实现类 ..
 *
 * @see LightningTokenContext
 */
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