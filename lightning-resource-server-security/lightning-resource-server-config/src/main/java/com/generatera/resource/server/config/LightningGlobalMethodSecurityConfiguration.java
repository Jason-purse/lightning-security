package com.generatera.resource.server.config;

import com.generatera.resource.server.common.EnableLightningMethodSecurity;
import com.generatera.resource.server.config.ResourceServerProperties.StoreKind;
import com.generatera.resource.server.config.method.security.*;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 13:44
 * @Description 启用全局方法安全 ...
 *
 * 尝试过使用{@link org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity}的方式
 * 直接加入 Advisor ,但是,相比于那样,加入MetadataSource 可能更加简单 ...
 *
 * 也就是注释中的代码和{@link LightningPrePostMethodSecurityMetadataSource} 二选一即可 ...
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableLightningMethodSecurity
@RequiredArgsConstructor
class LightningGlobalMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration implements ApplicationListener<ApplicationEvent> {


    private final ResourceServerProperties properties;

    private List<LightningExtMethodSecurityMetadataSource> sources;

    //private final LightningAuthorizationManagerBeforeMethodInterceptor preAuthorizeAuthorizationMethodInterceptor;
    //private final LightningPreAuthorizeAuthorizationManager preAuthorizeAuthorizationManager = new LightningPreAuthorizeAuthorizationManager();
    //private final LightningAuthorizationManagerAfterMethodInterceptor postAuthorizeAuthorizationMethodInterceptor;
    //private final LightningPostAuthorizeAuthorizationManager postAuthorizeAuthorizationManager = new LightningPostAuthorizeAuthorizationManager();
    //private final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();


    /**
     * 内部的 pre / post 注解的元数据来源 ..
     */
    private final LightningPrePostMethodSecurityMetadataSource methodSecurityMetadataSource = createMethodSecurityMetadataSource();

    /**
     * 必须同步 ...
     *
     * 可以考虑异步 ...
     * 为了尽快执行 ...
     */
    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
       if(event instanceof ContextRefreshedEvent) {
           MethodSecurityMetadataSource source = methodSecurityMetadataSource();
           AllowCacheModifiedMethodSecurityMetadataSource allowCacheModifiedMethodSecurityMetadataSource = (AllowCacheModifiedMethodSecurityMetadataSource) source;
           // 清理掉 缓存信息 ...
           allowCacheModifiedMethodSecurityMetadataSource.clearTempAttributeCache();

           // 更新可能存在的新数据 ...
       }

        // 这个时候, 获取 methodSecurityMetadataSource中的 已经被处理过的数据信息
        // 触发内部的逻辑 ... 也就是缓存丢弃 ..  开始真正的权限信息抓取 ..
        // 例如从 数据库中获取 ...
        methodSecurityMetadataSource.onApplicationEvent(event);
    }

    interface MethodSecurityHandlerProvider extends HandlerFactory.HandlerProvider {
        @Override
        default Object key() {
            return MethodSecurityMetadataSource.class;
        }

        @NotNull
        @Override
        MethodSecurityHandler getHandler();
    }




    interface MethodSecurityHandler  extends HandlerFactory.Handler {
        LightningPrePostMethodSecurityMetadataSource getMethodSecurityMetadataSource(PrePostInvocationAttributeFactory prePostInvocationAttributeFactory);
    }

    static  {
        HandlerFactory.registerHandler(
                new MethodSecurityHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return predicate == StoreKind.JPA;
                    }

                    @NotNull
                    @Override
                    public MethodSecurityHandler getHandler() {
                        return new MethodSecurityHandler() {
                            @Override
                            public LightningPrePostMethodSecurityMetadataSource getMethodSecurityMetadataSource(PrePostInvocationAttributeFactory prePostInvocationAttributeFactory) {
                                return new JpaPrePostMethodSecurityMetadataSource(prePostInvocationAttributeFactory);
                            }
                        };
                    }
                }
        );

        HandlerFactory.registerHandler(
                new MethodSecurityHandlerProvider() {
                    @NotNull
                    @Override
                    public MethodSecurityHandler getHandler() {
                        return new MethodSecurityHandler() {
                            @Override
                            public LightningPrePostMethodSecurityMetadataSource getMethodSecurityMetadataSource(PrePostInvocationAttributeFactory prePostInvocationAttributeFactory) {
                                return new MongoPrePostMethodSecurityMetadataSource(prePostInvocationAttributeFactory);
                            }
                        };
                    }

                    @Override
                    public boolean support(Object predicate) {
                        return predicate == StoreKind.MONGO;
                    }
                }
        );
    }


    @Autowired
    public LightningGlobalMethodSecurityConfiguration(ApplicationContext context,ResourceServerProperties resourceServerProperties) {
        //this.preAuthorizeAuthorizationManager.setExpressionHandler(this.expressionHandler);
        //this.preAuthorizeAuthorizationMethodInterceptor = LightningAuthorizationManagerBeforeMethodInterceptor.preAuthorize(this.preAuthorizeAuthorizationManager);
        //this.postAuthorizeAuthorizationManager.setExpressionHandler(this.expressionHandler);
        //this.postAuthorizeAuthorizationMethodInterceptor = LightningAuthorizationManagerAfterMethodInterceptor.postAuthorize(this.postAuthorizeAuthorizationManager);
        //this.expressionHandler.setApplicationContext(context);
        //AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(context);
        //this.preAuthorizeAuthorizationMethodInterceptor.setAuthorizationEventPublisher(publisher);
        //this.postAuthorizeAuthorizationMethodInterceptor.setAuthorizationEventPublisher(publisher);
        this.properties = resourceServerProperties;
    }
    /**
     * 提供 {@link LightningPreAuthorize} 以及 {@link LightningPostAuthorize} 等注解的处理 的元数据来源 ..
     * @return
     */
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        LinkedList<MethodSecurityMetadataSource> methodSecurityMetadataSources = new LinkedList<>();
        methodSecurityMetadataSources.add(methodSecurityMetadataSource);
        if (sources != null) {
            methodSecurityMetadataSources.addAll(sources);
        }
        return new DelegateMethodSecurityMetadataSource(methodSecurityMetadataSources);
    }

    @Bean
    public ExpressionBasedAnnotationAttributeFactory attributeFactory() {
        return new ExpressionBasedAnnotationAttributeFactory(getExpressionHandler());
    }

    private LightningPrePostMethodSecurityMetadataSource createMethodSecurityMetadataSource() {

        ExpressionBasedAnnotationAttributeFactory attributeFactory = attributeFactory();
        StoreKind saveKind = properties.getAuthorityConfig().getResourceAuthoritySaveKind();
        if(saveKind != null) {
            saveKind = StoreKind.MEMORY;
        }
        HandlerFactory.HandlerProvider handler = HandlerFactory.getHandler(MethodSecurityMetadataSource.class, saveKind);

        if(handler == null) {
            return new LightningPrePostMethodSecurityMetadataSource(attributeFactory);
        }

        MethodSecurityHandler securityHandler = ((MethodSecurityHandlerProvider) handler).getHandler();
        return securityHandler.getMethodSecurityMetadataSource(attributeFactory);
    }

    @Override
    public MethodSecurityMetadataSource methodSecurityMetadataSource() {
        MethodSecurityMetadataSource source = super.methodSecurityMetadataSource();
        DelegatingMethodSecurityMetadataSource delegatingMethodSecurityMetadataSource = (DelegatingMethodSecurityMetadataSource) source;
        return new AllowCacheModifiedMethodSecurityMetadataSource(new DelegateMethodSecurityMetadataSource(delegatingMethodSecurityMetadataSource.getMethodSecurityMetadataSources()));
    }

    @Autowired
    public void setExtMethodSecurityMetadataSources(@Autowired(required = false) List<LightningExtMethodSecurityMetadataSource> sources) {
        this.sources = sources;
    }

    //@Bean
    //@Role(2)
    //Advisor lightningPreAuthorizeMethodInterceptor() {
    //    return preAuthorizeAuthorizationMethodInterceptor;
    //}

    //@Bean
    //@Role(2)
    //Advisor postAuthorizeAuthorizationMethodInterceptor() {
    //    return this.postAuthorizeAuthorizationMethodInterceptor;
    //}
}
