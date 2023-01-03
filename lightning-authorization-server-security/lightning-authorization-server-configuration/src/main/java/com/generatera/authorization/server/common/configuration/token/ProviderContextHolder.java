package com.generatera.authorization.server.common.configuration.token;
/**
 * @author FLJ
 * @date 2023/1/3
 * @time 14:36
 * @Description oauth2 copy
 */
public final class ProviderContextHolder {
    private static final ThreadLocal<ProviderContext> holder = new ThreadLocal<>();

    private ProviderContextHolder() {
    }

    public static ProviderContext getProviderContext() {
        return (ProviderContext)holder.get();
    }

    public static void setProviderContext(ProviderContext providerContext) {
        if (providerContext == null) {
            resetProviderContext();
        } else {
            holder.set(providerContext);
        }

    }

    public static void resetProviderContext() {
        holder.remove();
    }
}