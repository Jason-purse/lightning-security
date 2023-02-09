package com.generatera.resource.server.config.method.security;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
/**
 * @author FLJ
 * @date 2023/2/6
 * @time 14:05
 * @Description lightning 扩展的 method security metadata source
 */
public interface LightningExtMethodSecurityMetadataSource extends MethodSecurityMetadataSource, ApplicationListener<ApplicationEvent> {
}
