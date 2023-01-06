package com.generatera.security.server.token.specification.format.jwt;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:15
 * @Description JWKSource 提供器
 */
public class JWKSourceProvider {

    @Nullable
    public static <T> T getJWKSource() {
        return internalGetJWKSource();
    }

    @SuppressWarnings("unchecked")
    public static void setJWKSource(Object source) {
        if (source instanceof JWKSource data) {
            NimBusJWKSourceContainer.setSource(((JWKSource<SecurityContext>) data));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T internalGetJWKSource() {
        return (T) NimBusJWKSourceContainer.source;
    }

    /**
     * 取消对  NimBus的强依赖
     */
    private static class NimBusJWKSourceContainer {

        private static JWKSource<SecurityContext> source;

        public static void setSource(@NotNull JWKSource<SecurityContext> source) {
            if (NimBusJWKSourceContainer.source != null) {
                throw new IllegalArgumentException("jwk source already set !!!");
            }
            NimBusJWKSourceContainer.source = source;
        }

        public static JWKSource<SecurityContext> getSource() {
            if(source == null) {
                synchronized (source) {
                    if(source == null) {
                        // todo
                    }
                }
            }
            return source;
        }
    }
}
