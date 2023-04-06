package com.generatera.resource.server.config;

import com.generatera.resource.server.common.EnableLightningMethodSecurity;
import com.generatera.resource.server.config.method.security.*;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.*;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.intercept.AfterInvocationManager;
import org.springframework.security.access.intercept.AfterInvocationProviderManager;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.prepost.*;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 13:44
 * @Description 启用全局方法安全 ...
 * <p>
 * 尝试过使用{@link org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity}的方式
 * 直接加入 Advisor ,但是,相比于那样,加入MetadataSource 可能更加简单 ...
 * <p>
 * 也就是注释中的代码和{@link LightningPrePostMethodSecurityMetadataSource} 二选一即可 ...
 */
@Configuration
@Import(MethodSecurityMetadataRepositoryConfiguration.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableLightningMethodSecurity
@Slf4j
class LightningGlobalMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration implements ApplicationListener<ApplicationEvent>, DisposableBean {


    private final ResourceServerProperties properties;

    private List<LightningExtMethodSecurityMetadataSource> sources;

    /**
     * application context
     */
    private final ApplicationContext applicationContext;

    //private final LightningAuthorizationManagerBeforeMethodInterceptor preAuthorizeAuthorizationMethodInterceptor;
    //private final LightningPreAuthorizeAuthorizationManager preAuthorizeAuthorizationManager = new LightningPreAuthorizeAuthorizationManager();
    //private final LightningAuthorizationManagerAfterMethodInterceptor postAuthorizeAuthorizationMethodInterceptor;
    //private final LightningPostAuthorizeAuthorizationManager postAuthorizeAuthorizationManager = new LightningPostAuthorizeAuthorizationManager();
    //private final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();


    /**
     * 内部的 pre / post 注解的元数据来源 ..
     */
    private ForcedCacheMethodSecurityMetadataSource methodSecurityMetadataSource;

    private List<ApplicationEvent> earlyApplicationEvents = new LinkedList<>();

    private final MethodSecurityMetadataRepositoryManager repositoryManager;

    public ForcedCacheMethodSecurityMetadataSource getMethodSecurityMetadataSource() {
        return methodSecurityMetadataSource;
    }

    @Override
    public void afterSingletonsInstantiated() {
        super.afterSingletonsInstantiated();

        // 初始化的时候,才会有methodSecurityMetadataSource
        methodSecurityMetadataSource = methodSecurityMetadataSource();

        //执行之前的 事件 ...
        List<ApplicationEvent> earlyApplicationEvents = this.earlyApplicationEvents;
        this.earlyApplicationEvents = new LinkedList<>();
        for (ApplicationEvent earlyApplicationEvent : earlyApplicationEvents) {
            methodSecurityMetadataSource.onApplicationEvent(earlyApplicationEvent);
        }
    }

    @Autowired
    public LightningGlobalMethodSecurityConfiguration(ApplicationContext context, ResourceServerProperties resourceServerProperties) {
        //this.preAuthorizeAuthorizationManager.setExpressionHandler(this.expressionHandler);
        //this.preAuthorizeAuthorizationMethodInterceptor = LightningAuthorizationManagerBeforeMethodInterceptor.preAuthorize(this.preAuthorizeAuthorizationManager);
        //this.postAuthorizeAuthorizationManager.setExpressionHandler(this.expressionHandler);
        //this.postAuthorizeAuthorizationMethodInterceptor = LightningAuthorizationManagerAfterMethodInterceptor.postAuthorize(this.postAuthorizeAuthorizationManager);
        //this.expressionHandler.setApplicationContext(context);
        //AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(context);
        //this.preAuthorizeAuthorizationMethodInterceptor.setAuthorizationEventPublisher(publisher);
        //this.postAuthorizeAuthorizationMethodInterceptor.setAuthorizationEventPublisher(publisher);
        this.properties = resourceServerProperties;
        this.applicationContext = context;
        this.repositoryManager = new MethodSecurityMetadataRepositoryManager(applicationContext, properties);
    }


    /**
     * 必须同步 ...
     * <p>
     * 可以考虑异步 ...
     * 为了尽快执行 ...
     */
    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {

        // 方法评估事件
        if(event instanceof AuthorizedEvent) {
            methodSecurityMetadataSource.onApplicationEvent(event);
        }
        else if (properties.getAuthorityConfig().isEnableMethodPrePostAuthorityScan()) {
            // 这个时候, 获取 methodSecurityMetadataSource中的 已经被处理过的数据信息
            // 触发内部的逻辑 ... 也就是缓存丢弃 ..  开始真正的权限信息抓取 ..
            // 例如从 数据库中获取 ...
            if (methodSecurityMetadataSource == null) {
                earlyApplicationEvents.add(event);
            } else {
                methodSecurityMetadataSource.onApplicationEvent(event);
            }
        }
    }

    ///**
    // * 对 {@link LightningResourceMethodSecurityHolder} 的支持 ..
    // */
    //@Override
    //protected AccessDecisionManager accessDecisionManager() {
    //
    //    AccessDecisionManager accessDecisionManager = super.accessDecisionManager();
    //    List<AccessDecisionVoter<?>> decisionVoters = ((AbstractAccessDecisionManager) accessDecisionManager).getDecisionVoters();
    //    int index = -1;
    //    for (int i = 0; i < decisionVoters.size(); i++) {
    //        if (decisionVoters.get(i) instanceof PreInvocationAuthorizationAdviceVoter) {
    //           index = i;
    //        }
    //    }
    //    if(index >= 0) {
    //        AccessDecisionVoter<?> accessDecisionVoter = decisionVoters.get(index);
    //        LightningPreInvocationAuthorizationAdviceVoter preInvocationAuthorizationAdviceVoter = new LightningPreInvocationAuthorizationAdviceVoter(((PreInvocationAuthorizationAdviceVoter) accessDecisionVoter));
    //        LinkedList<AccessDecisionVoter<?>> accessDecisionVoters = new LinkedList<>(decisionVoters);
    //        accessDecisionVoters.add(index,preInvocationAuthorizationAdviceVoter);
    //        // 删除之前的配置 ..
    //        accessDecisionVoters.remove(index + 1);
    //        return new AffirmativeBased(accessDecisionVoters);
    //    }
    //
    //    return accessDecisionManager;
    //}
    //
    //@Override
    //protected AfterInvocationManager afterInvocationManager() {
    //
    //    AfterInvocationManager afterInvocationManager = super.afterInvocationManager();
    //    if(afterInvocationManager != null) {
    //        List<AfterInvocationProvider> providers = ((AfterInvocationProviderManager) afterInvocationManager).getProviders();
    //        int index = -1;
    //        for (int i = 0; i < providers.size(); i++) {
    //            if (providers.get(i) instanceof PostInvocationAdviceProvider) {
    //                index = i;
    //            }
    //        }
    //
    //        if(index > 0) {
    //            AfterInvocationProvider afterInvocationProvider = providers.get(index);
    //            LightningPostInvocationAuthorizationProvider preInvocationAuthorizationAdviceVoter = new LightningPostInvocationAuthorizationProvider(afterInvocationProvider);
    //            LinkedList<AfterInvocationProvider> accessDecisionVoters = new LinkedList<>(providers);
    //            accessDecisionVoters.add(index,preInvocationAuthorizationAdviceVoter);
    //            accessDecisionVoters.remove(index + 1);
    //            AfterInvocationProviderManager afterInvocationProviderManager = new AfterInvocationProviderManager();
    //            afterInvocationProviderManager.setProviders(accessDecisionVoters);
    //            return afterInvocationProviderManager;
    //        }
    //    }
    //
    //    return afterInvocationManager;
    //}

    @Override
    public void destroy() throws Exception {
        // 关闭资源
        repositoryManager.destroy();
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


    interface MethodSecurityHandler extends HandlerFactory.Handler {
        LightningPrePostMethodSecurityMetadataSource getMethodSecurityMetadataSource(PrePostInvocationAttributeFactory prePostInvocationAttributeFactory, ApplicationContext applicationContext);
    }

    /**
     * 提供 {@link LightningPreAuthorize} 以及 {@link LightningPostAuthorize} 等注解的处理 的元数据来源 ..
     *
     * @return
     */
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        LinkedList<LightningExtMethodSecurityMetadataSource> methodSecurityMetadataSources = new LinkedList<>();
        methodSecurityMetadataSources.add(createMethodSecurityMetadataSource());
        if (sources != null) {
            methodSecurityMetadataSources.addAll(sources);
        }
        return new DelegateLightningExtMethodSecurityMetadataSource(methodSecurityMetadataSources);
    }

    private LightningExtMethodSecurityMetadataSource createMethodSecurityMetadataSource() {

        ExpressionBasedAnnotationAttributeFactory attributeFactory = new ExpressionBasedAnnotationAttributeFactory(getExpressionHandler());
        return repositoryManager.getRepository(attributeFactory);
    }


    @Override
    public ForcedCacheMethodSecurityMetadataSource methodSecurityMetadataSource() {
        MethodSecurityMetadataSource source = super.methodSecurityMetadataSource();
        DelegatingMethodSecurityMetadataSource delegatingMethodSecurityMetadataSource = (DelegatingMethodSecurityMetadataSource) source;
        // 让一部分的method security metadata Source 有能力 不受外部缓存控制,依靠自己进行缓存控制 ..
        ResourceServerProperties.AuthorityConfiguration.CacheConfig cacheConfig = properties.getAuthorityConfig().getCacheConfig();
        return new ForcedCacheMethodSecurityMetadataSource(
                new AllowPartialCacheMethodSecurityMetadataSource(delegatingMethodSecurityMetadataSource.getMethodSecurityMetadataSources()),
                cacheConfig.isSupportForceSupport(),
                cacheConfig.getExpiredDuration() > 0 ? cacheConfig.getExpiredDuration() : ResourceServerProperties.AuthorityConfiguration.CacheConfig.DEFAULT_EXPIRED_DURATION
        );
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

class LightningPreInvocationAuthorizationAdviceVoter implements AccessDecisionVoter<MethodInvocation> {
    private final PreInvocationAuthorizationAdviceVoter voter;

    public LightningPreInvocationAuthorizationAdviceVoter(PreInvocationAuthorizationAdviceVoter voter) {
        this.voter = voter;
    }

    public PreInvocationAuthorizationAdviceVoter getVoter() {
        return voter;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return voter.supports(attribute);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return voter.supports(clazz);
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation object, Collection<ConfigAttribute> attributes) {
        int vote = voter.vote(authentication, object, attributes);
        if (vote == 1) {
            for (ConfigAttribute attribute : attributes) {
                if (attribute instanceof LightningInvocationAttribute preInvocationAttribute) {
                    if (preInvocationAttribute instanceof PreInvocationAttribute) {
                        // 设置当前资源方法安全holder
                        LightningResourceMethodSecurityHolder.setPreResourceMethodSecurity(preInvocationAttribute.getMethodIdentifierWithActionAndType());
                    }
                }
            }
        }
        return vote;
    }
}

class LightningPostInvocationAuthorizationProvider implements AfterInvocationProvider {
    private final AfterInvocationProvider provider;

    public LightningPostInvocationAuthorizationProvider(AfterInvocationProvider provider) {
        this.provider = provider;
    }

    public AfterInvocationProvider getProvider() {
        return provider;
    }

    @Override
    public Object decide(Authentication authentication, Object object, Collection<ConfigAttribute> attributes, Object returnedObject) throws AccessDeniedException {
        Object decide = provider.decide(authentication, object, attributes, returnedObject);
        for (ConfigAttribute attribute : attributes) {
            if (attribute instanceof LightningInvocationAttribute invocationAttribute) {
                if (attribute instanceof PostInvocationAttribute) {
                    LightningResourceMethodSecurityHolder.setPostResourceMethodSecurity(
                            invocationAttribute.getMethodIdentifierWithActionAndType()
                    );
                }
            }
        }
        return decide;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return provider.supports(attribute);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return provider.supports(clazz);
    }
}
