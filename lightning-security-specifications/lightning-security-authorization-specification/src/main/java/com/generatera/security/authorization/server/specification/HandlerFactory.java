package com.generatera.security.authorization.server.specification;

import com.jianyue.lightning.boot.starter.util.dataflow.impl.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author FLJ
 * @date 2023/1/9
 * @time 12:44
 * @Description Handler 工厂...
 * <p>
 * 实现基于工厂形式的 对应bean 获取 ..
 * 并不保证 bean的单例性,取决于 provider的处理形式 以及 handler的扩展性 ..
 */
public class HandlerFactory {

    public interface HandlerProvider {

        Object key();

        boolean support(Object predicate);

        @NotNull
        Handler getHandler();

        static <Key, Handler extends HandlerFactory.Handler> HandlerProvider of(
                Key key, Predicate<Object> predicate, Handler handler
        ) {
            return new DefaultGenericHandlerProvider<>(key, predicate, handler);
        }

        static <Key, Handler extends HandlerFactory.Handler> List<HandlerProvider> list(
                Key key, List<Tuple<Predicate<Object>, Handler>> handlers
        ) {
            return handlers.stream()
                    .map(ele -> (HandlerProvider) new DefaultGenericHandlerProvider<>(key, ele.getFirst(), ele.getSecond()))
                    .toList();
        }

    }

    static class DefaultGenericHandlerProvider<Key, Handler extends HandlerFactory.Handler> implements HandlerProvider {
        private final Key key;
        private final Handler handler;
        private final Predicate<Object> predicate;

        public DefaultGenericHandlerProvider(Key key, Predicate<Object> predicate, Handler handler) {
            this.key = key;
            this.predicate = predicate;
            this.handler = handler;
        }

        @Override
        public Object key() {
            return key;
        }

        @Override
        public boolean support(Object predicate) {
            return this.predicate.test(predicate);
        }

        @NotNull
        @Override
        public Handler getHandler() {
            return handler;
        }
    }

    public interface Handler {

        /**
         * 返回此handler 包装的内部Handler ..
         *
         * @throws IllegalStateException 如果强转的类型不兼容,则抛出异常 ...
         */
        @SuppressWarnings("unchecked")
        default <T extends Handler> T nativeHandler() {
            try {
                return (T) this;
            } catch (Exception e) {
                throw new IllegalStateException("can't cast to native Handler,current handler is " + this.getClass().getName());
            }
        }
    }

    public interface TransformHandler<DATA, T> extends Handler {

        T get(DATA value);

        static <DATA, T> TransformHandler<DATA, T> of(Function<DATA, T> transformer) {
            return new DefaultTransformHandler<>(transformer);
        }

        static <DATA, T> TransformHandler<DATA, T> of(T transformer) {
            return new DefaultTransformHandler<>((ele) -> transformer);
        }

    }


    public interface SupplierHandler<T> extends Handler {

        T get();

        static <T> SupplierHandler<T> of(Supplier<T> transformer) {
            return new DefaultSupplierHandler<>(transformer);
        }

        static <T> SupplierHandler<T> of(T transformer) {
            return new DefaultSupplierHandler<>(() -> transformer);
        }
    }

    static class DefaultTransformHandler<DATA, T> implements TransformHandler<DATA, T> {
        private final Function<DATA, T> transformer;

        public DefaultTransformHandler(Function<DATA, T> transformer) {
            this.transformer = transformer;
        }


        @Override
        public T get(DATA value) {
            return transformer.apply(value);
        }
    }


    static class DefaultSupplierHandler<T>  implements SupplierHandler<T> {
        private final Supplier<T> supplier;
        public DefaultSupplierHandler(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            return supplier.get();
        }
    }


    private static final ConcurrentHashMap<Object, List<HandlerProvider>> handlerCache = new ConcurrentHashMap<>();

    public static <T extends HandlerProvider> void registerHandler(T handlerProvider) {
        handlerCache
                .computeIfAbsent(handlerProvider.key(), key -> new LinkedList<>())
                .add(handlerProvider);
    }

    public static <T extends HandlerProvider> void registerHandlers(List<T> handlerProviders) {
        for (T handlerProvider : handlerProviders) {
            handlerCache
                    .computeIfAbsent(handlerProvider.key(), key -> new LinkedList<>())
                    .add(handlerProvider);
        }
    }


    @Nullable
    public static List<HandlerProvider> getHandlers(Object key) {
        return handlerCache.get(key);
    }

    @Nullable
    public static HandlerProvider getHandler(Object key, Object predicate) {
        List<HandlerProvider> handlers = getHandlers(key);
        if (handlers != null) {
            for (HandlerProvider handler : handlers) {
                if (handler.support(predicate)) {
                    return handler;
                }
            }
        }
        return null;
    }

    public static HandlerProvider getRequiredHandler(Object key, Object predicate) {
        HandlerProvider handler = getHandler(key, predicate);
        Assert.notNull(handler, "can't found an handler for " + key + "!!!");
        return handler;
    }
}
