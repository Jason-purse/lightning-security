package com.generatera.security.authorization.server.specification;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author FLJ
 * @date 2023/1/9
 * @time 12:44
 * @Description Handler 工厂...
 *
 * 实现基于工厂形式的 对应bean 获取 ..
 * 并不保证 bean的单例性,取决于 provider的处理形式 以及 handler的扩展性 ..
 */
public class HandlerFactory {

    public interface HandlerProvider {

        Object key();

        boolean support(Object predicate);

        @NotNull
        Handler getHandler();
    }

    public interface Handler {

    }

    private static final ConcurrentHashMap<Object, List<HandlerProvider>> handlerCache = new ConcurrentHashMap<>();

    public static <T extends HandlerProvider> void registerHandler(T handlerProvider) {
        handlerCache
                .computeIfAbsent(handlerProvider.key(), key -> new LinkedList<>())
                .add(handlerProvider);
    }

    @Nullable
    public static List<HandlerProvider> getHandlers(Object key) {
        return handlerCache.get(key);
    }

    @Nullable
    public static HandlerProvider getHandler(Object key,Object predicate) {
        List<HandlerProvider> handlers = getHandlers(key);
        if(handlers != null) {
            for (HandlerProvider handler : handlers) {
                if (handler.support(predicate)) {
                    return handler;
                }
            }
        }
        return null;
    }
}
