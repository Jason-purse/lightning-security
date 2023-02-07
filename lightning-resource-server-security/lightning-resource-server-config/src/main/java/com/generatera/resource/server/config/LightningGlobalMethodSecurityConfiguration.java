package com.generatera.resource.server.config;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.resource.server.config.method.security.*;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
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
class LightningGlobalMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

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
                        return predicate == AuthorizationServerComponentProperties.StoreKind.JPA;
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
                        return predicate == AuthorizationServerComponentProperties.StoreKind.MONGO;
                    }
                }
        );
    }




    private final ResourceServerProperties properties;

    private List<LightningExtMethodSecurityMetadataSource> sources;

    //private final LightningAuthorizationManagerBeforeMethodInterceptor preAuthorizeAuthorizationMethodInterceptor;
    //private final LightningPreAuthorizeAuthorizationManager preAuthorizeAuthorizationManager = new LightningPreAuthorizeAuthorizationManager();
    //private final LightningAuthorizationManagerAfterMethodInterceptor postAuthorizeAuthorizationMethodInterceptor;
    //private final LightningPostAuthorizeAuthorizationManager postAuthorizeAuthorizationManager = new LightningPostAuthorizeAuthorizationManager();
    //private final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();

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
        methodSecurityMetadataSources.add(createMethodSecurityMetadataSource());

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
        AuthorizationServerComponentProperties.StoreKind saveKind = properties.getAuthorityConfig().getResourceAuthoritySaveKind();
        if(saveKind != null) {
            saveKind = AuthorizationServerComponentProperties.StoreKind.MEMORY;
        }
        HandlerFactory.HandlerProvider handler = HandlerFactory.getHandler(MethodSecurityMetadataSource.class, saveKind);

        if(handler == null) {
            return new LightningPrePostMethodSecurityMetadataSource(attributeFactory);
        }

        MethodSecurityHandler securityHandler = ((MethodSecurityHandlerProvider) handler).getHandler();
        return securityHandler.getMethodSecurityMetadataSource(attributeFactory);
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
