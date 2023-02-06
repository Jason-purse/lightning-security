package com.generatera.resource.server.config;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.authorization.method.PostAuthorizeAuthorizationManager;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 13:44
 * @Description 启用全局方法安全 ...
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class LightningGlobalMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    private List<LightningExtMethodSecurityMetadataSource> sources;

    private final LightningAuthorizationManagerBeforeMethodInterceptor preAuthorizeAuthorizationMethodInterceptor;
    private final LightningPreAuthorizeAuthorizationManager preAuthorizeAuthorizationManager = new LightningPreAuthorizeAuthorizationManager();
    private final LightningAuthorizationManagerAfterMethodInterceptor postAuthorizeAuthorizationMethodInterceptor;
    private final LightningPostAuthorizeAuthorizationManager postAuthorizeAuthorizationManager = new LightningPostAuthorizeAuthorizationManager();
    private final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();

    @Autowired
    public LightningGlobalMethodSecurityConfiguration(ApplicationContext context) {
        this.preAuthorizeAuthorizationManager.setExpressionHandler(this.expressionHandler);
        this.preAuthorizeAuthorizationMethodInterceptor = LightningAuthorizationManagerBeforeMethodInterceptor.preAuthorize(this.preAuthorizeAuthorizationManager);
        this.postAuthorizeAuthorizationManager.setExpressionHandler(this.expressionHandler);
        this.postAuthorizeAuthorizationMethodInterceptor = LightningAuthorizationManagerAfterMethodInterceptor.postAuthorize(this.postAuthorizeAuthorizationManager);
        this.expressionHandler.setApplicationContext(context);
        AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(context);
        this.preAuthorizeAuthorizationMethodInterceptor.setAuthorizationEventPublisher(publisher);
        this.postAuthorizeAuthorizationMethodInterceptor.setAuthorizationEventPublisher(publisher);
    }
    /**
     * 提供 {@link LightningPreAuthorize} 以及 {@link LightningPostAuthorize} 等注解的处理 的元数据来源 ..
     * @return
     */
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        LinkedList<MethodSecurityMetadataSource> methodSecurityMetadataSources = new LinkedList<>();
        methodSecurityMetadataSources.add(createMethodSecurityMetadataSource());

        if (sources != null) {
            methodSecurityMetadataSources.addAll(sources);
        }

        return new DelegateMethodSecurityMetadataSource(methodSecurityMetadataSources);
    }

    private LightningPrePostMethodSecurityMetadataSource createMethodSecurityMetadataSource() {
        ExpressionBasedAnnotationAttributeFactory attributeFactory = new ExpressionBasedAnnotationAttributeFactory(this.getExpressionHandler());
        return new LightningPrePostMethodSecurityMetadataSource(attributeFactory);
    }


    @Autowired
    public void setExtMethodSecurityMetadataSources(@Autowired(required = false) List<LightningExtMethodSecurityMetadataSource> sources) {
        this.sources = sources;
    }

    @Bean
    @Role(2)
    Advisor lightningPreAuthorizeMethodInterceptor() {
        return preAuthorizeAuthorizationMethodInterceptor;
    }

    @Bean
    @Role(2)
    Advisor postAuthorizeAuthorizationMethodInterceptor() {
        return this.postAuthorizeAuthorizationMethodInterceptor;
    }
}
