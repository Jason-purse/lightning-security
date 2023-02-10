package com.generatera.resource.server.config.method.security;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
/**
 * @author FLJ
 * @date 2023/2/6
 * @time 14:05
 * @Description lightning 扩展的 method security metadata source
 */
public interface LightningExtMethodSecurityMetadataSource extends MethodSecurityMetadataSource, ApplicationListener<ApplicationEvent>, DisposableBean {
    /**
     * 缓存能力检测
     * 是否是可缓存的 ..
     * 实现可以实现是否缓存,让缓存组件决定是否立即抓取它的数据 ...
     *
     * 此函数不可条件实现, 仅在第一次决定它的作用 ..
     *
     * 如果它是可缓存的,则下一次将不会访问它的函数 ...
     */
    default boolean isCacheable() {
        return true;
    }

    /**
     * 资源释放 回调
     */
    @Override
    default void destroy() throws Exception {
        // pass
    }
}
