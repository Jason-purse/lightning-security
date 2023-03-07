package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.method.security.repository.JpaResourceMethodSecurityRepository;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import com.generatera.security.authorization.server.specification.util.LogUtil;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.Assert;

import java.util.Map;

import static com.generatera.resource.server.config.ResourceServerProperties.AuthorityConfig.JpaCacheConfig.DataSourceConfigPrefix;
import static com.generatera.resource.server.config.ResourceServerProperties.AuthorityConfig.JpaCacheConfigPrefix;
import static com.generatera.resource.server.config.ResourceServerProperties.AuthorityConfig.MongoCacheConfig.mongoClientPropertiesPrefix;

/**
 * 基于缓存实现的,  仓库管理器 ..
 *
 * 本质上它仅仅管理 {@link  org.springframework.stereotype.Repository} 但是为了方便
 * 最终它返回的是{@link  LightningExtMethodSecurityMetadataSource}
 */
public class MethodSecurityMetadataRepositoryManager implements DisposableBean {
    
    private final MethodSecurityRepositoryHandler handler;

    private final ApplicationContext applicationContext;

    private final ResourceServerProperties resourceServerProperties;


    public MethodSecurityMetadataRepositoryManager(ApplicationContext applicationContext, ResourceServerProperties resourceServerProperties) {
        HandlerFactory.HandlerProvider handlerProvider = HandlerFactory.getHandler(MethodSecurityMetadataRepositoryManager.class,
                resourceServerProperties);
        Assert.notNull(handlerProvider,"method security meta data repository handler provider must not be null !!!");
        this.handler = (MethodSecurityRepositoryHandler) handlerProvider.getHandler();
        this.applicationContext = applicationContext;
        this.resourceServerProperties = resourceServerProperties;
    }


    public LightningExtMethodSecurityMetadataSource getRepository(PrePostInvocationAttributeFactory attributeFactory) {
        return handler.getRepository(resourceServerProperties,applicationContext,attributeFactory);
    }


    public static String getModuleName(ResourceServerProperties properties,ApplicationContext applicationContext) {
        String moduleName = properties.getAuthorityConfig().getModuleName();
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        String value = ElvisUtil.stringElvis(moduleName, applicationName);
        Assert.hasText(value,"module name[look ResourceServerProperties for details description] must not be null,populate with module name or spring.application.name !!!");
        return value;
    }

    @Override
    public void destroy() throws Exception {
        handler.destroy();
    }


    interface MethodSecurityRepositoryHandlerProvider extends HandlerFactory.HandlerProvider {
        @Override
        default Object key() {
            return MethodSecurityMetadataRepositoryManager.class;
        }

        @Override
        default boolean support(Object predicate) {
            return support(((ResourceServerProperties) predicate));
        }
        
        boolean support(ResourceServerProperties resourceServerProperties);
    }
    
    interface MethodSecurityRepositoryHandler extends HandlerFactory.Handler {
        
        public LightningExtMethodSecurityMetadataSource getRepository(ResourceServerProperties resourceServerProperties,
                                   ApplicationContext applicationContext,
                                   PrePostInvocationAttributeFactory attributeFactory);

        default void destroy() {

        }
    }
    
    private static boolean useSelfConfigOrNewConfigJpa(ResourceServerProperties resourceServerProperties) {
        ResourceServerProperties.AuthorityConfig.CacheConfig cacheConfig = resourceServerProperties.getAuthorityConfig().getCacheConfig();
        ResourceServerProperties.AuthorityConfig.JpaCacheConfig jpaCacheConfig = cacheConfig.getJpaCacheConfig();
        return jpaCacheConfig.isEnable();
    }

    private static boolean useSelfConfigOrNewConfigMongo(ResourceServerProperties resourceServerProperties) {
        ResourceServerProperties.AuthorityConfig.MongoCacheConfig mongoCacheConfig = resourceServerProperties.getAuthorityConfig().getCacheConfig().getMongoCacheConfig();
        return mongoCacheConfig.isEnable();
    }

    static class NewApplicationContextJpaRepositoryHandler implements MethodSecurityMetadataRepositoryManager.MethodSecurityRepositoryHandler {

        private final AnnotationConfigApplicationContext repositoryApplicationContext = new AnnotationConfigApplicationContext();

        static class DelegatePropertySource extends PropertySource<Object> {
            private final Environment contextEnvironment;
            private final String jpaPropertiesPrefix;
            private final String dataSourcePropertiesPrefix;
            public DelegatePropertySource(String name,Environment contextEnvironment,
                                          String jpaPropertiesPrefix,
                                          String dataSourcePropertiesPrefix) {
                super(name);
                this.contextEnvironment = contextEnvironment;
                this.jpaPropertiesPrefix = jpaPropertiesPrefix;
                this.dataSourcePropertiesPrefix = dataSourcePropertiesPrefix;
            }

            @Override
            public Object getProperty(String name) {
                if(name.startsWith(jpaPropertiesPrefix)) {
                    return contextEnvironment.getProperty(
                            JpaCacheConfigPrefix.concat(".") + name.substring(jpaPropertiesPrefix.length() - 1)
                    );
                }
                if(name.startsWith(dataSourcePropertiesPrefix)) {
                    return contextEnvironment.getProperty(
                            DataSourceConfigPrefix.concat(".") + name.substring(dataSourcePropertiesPrefix.length() - 1)
                    );
                }

                // 其他的返回null
                return null;
            }
        }

        @Override
        public LightningExtMethodSecurityMetadataSource getRepository(ResourceServerProperties resourceServerProperties,
                                                                      ApplicationContext applicationContext,
                                                                      PrePostInvocationAttributeFactory attributeFactory) {

            LogUtil.prettyLog("application enable additional config for method security meta data store, so enable an new application context for repository !!!");
            // 配置系统环境属性
            AnnotationMetadata annotationMetadata = AnnotationMetadata.introspect(JpaProperties.class);
            Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(ConfigurationProperties.class.getName());
            assert annotationAttributes != null;
            String jpaPropertiesPrefix = annotationAttributes.get("value").toString();
            AnnotationMetadata introspect = AnnotationMetadata.introspect(DataSourceProperties.class);
            Map<String, Object> attributes = introspect.getAnnotationAttributes(ConfigurationProperties.class.getName());
            assert attributes != null;
            String dataSourcePropertiesPrefix = attributes.get("value").toString();

            // 需要添加属性 ..
            MutablePropertySources propertySources = repositoryApplicationContext.getEnvironment().getPropertySources();

            // 代理 ..
            propertySources.addFirst(
                    new DelegatePropertySource("repositoryPropertySource", applicationContext.getEnvironment(),
                            jpaPropertiesPrefix, dataSourcePropertiesPrefix)
            );
            // 开启自动配置
            repositoryApplicationContext.register(MethodSecurityMetadataRepositoryConfiguration.JpaDataBaseOperationsConfiguration.class);
            repositoryApplicationContext.register(DataSourceAutoConfiguration.class);
            repositoryApplicationContext.register(HibernateJpaAutoConfiguration.class);
            repositoryApplicationContext.register(TaskExecutionAutoConfiguration.class);
            repositoryApplicationContext.register(JpaRepositoriesAutoConfiguration.class);
            repositoryApplicationContext.register(DataSourceTransactionManagerAutoConfiguration.class);

            // 刷新即可 ...
            repositoryApplicationContext.refresh();
            LogUtil.prettyLog("repository application context was flushed !!!!");

            JpaResourceMethodSecurityRepository bean = repositoryApplicationContext.getBean(JpaResourceMethodSecurityRepository.class);
            return new JpaPrePostMethodSecurityMetadataSource(
                    attributeFactory,
                    bean,
                    getModuleName(resourceServerProperties,applicationContext)
            );
        }

        @Override
        public void destroy() {
            LogUtil.prettyLog("close repository application context !!!");
            repositoryApplicationContext.close();
        }
    }

    static class NewApplicationContextMongoRepositoryHandler implements MethodSecurityRepositoryHandler {
        private final AnnotationConfigApplicationContext repositoryApplicationContext = new AnnotationConfigApplicationContext();

        static class DelegatePropertySource extends PropertySource<Object> {
            private final Environment contextEnvironment;
            private final String dataSourcePropertiesPrefix;
            public DelegatePropertySource(String name,Environment contextEnvironment,
                                          String dataSourcePropertiesPrefix) {
                super(name);
                this.contextEnvironment = contextEnvironment;
                this.dataSourcePropertiesPrefix = dataSourcePropertiesPrefix;
            }

            @Override
            public Object getProperty(String name) {
                if(name.startsWith(dataSourcePropertiesPrefix)) {
                    return contextEnvironment.getProperty(
                            mongoClientPropertiesPrefix.concat(".") + name.substring(dataSourcePropertiesPrefix.length() - 1)
                    );
                }

                // 其他的返回null
                return null;
            }
        }

        @Override
        public LightningExtMethodSecurityMetadataSource getRepository(ResourceServerProperties resourceServerProperties,
                                                                      ApplicationContext applicationContext,
                                                                      PrePostInvocationAttributeFactory attributeFactory) {

            LogUtil.prettyLog("application enable additional config for method security meta data store, so enable an new application context for repository !!!");
            // 配置系统环境属性
            AnnotationMetadata annotationMetadata = AnnotationMetadata.introspect(MongoProperties.class);
            Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(ConfigurationProperties.class.getName());
            assert annotationAttributes != null;
            String dataSourcePropertiesPrefix = annotationAttributes.get("value").toString();

            // 需要添加属性 ..
            MutablePropertySources propertySources = repositoryApplicationContext.getEnvironment().getPropertySources();

            // 代理 ..
            propertySources.addFirst(
                    new DelegatePropertySource("repositoryPropertySource", applicationContext.getEnvironment(), dataSourcePropertiesPrefix)
            );

            // 开启自动配置
            repositoryApplicationContext.register(MethodSecurityMetadataRepositoryConfiguration.MongoDataBaseOperationsConfiguration.class);
            repositoryApplicationContext.register(MongoAutoConfiguration.class);
            repositoryApplicationContext.register(TaskExecutionAutoConfiguration.class);
            repositoryApplicationContext.register(MongoDataAutoConfiguration.class);
            repositoryApplicationContext.register(MongoRepositoriesAutoConfiguration.class);

            // 刷新即可 ...
            repositoryApplicationContext.refresh();
            LogUtil.prettyLog("repository application context was flushed !!!!");

            MongoTemplate bean = repositoryApplicationContext.getBean(MongoTemplate.class);
            return new MongoPrePostMethodSecurityMetadataSource(
                    attributeFactory,
                    bean,
                    getModuleName(resourceServerProperties,applicationContext)
            );
        }

        @Override
        public void destroy() {
            LogUtil.prettyLog("close repository application context !!!");
            repositoryApplicationContext.close();
        }
    }
    static {
        HandlerFactory.registerHandler(
                new MethodSecurityRepositoryHandlerProvider() {
                    @Override
                    public boolean support(ResourceServerProperties resourceServerProperties) {
                       return resourceServerProperties.getAuthorityConfig().getResourceAuthoritySaveKind() == ResourceServerProperties.StoreKind.JPA &&
                               useSelfConfigOrNewConfigJpa(resourceServerProperties) ;}

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new NewApplicationContextJpaRepositoryHandler();
                    }
                }
        );

        // jpa fallback
        HandlerFactory.registerHandler(
                new MethodSecurityRepositoryHandlerProvider() {
                    @Override
                    public boolean support(ResourceServerProperties resourceServerProperties) {
                        return resourceServerProperties.getAuthorityConfig().getResourceAuthoritySaveKind() == ResourceServerProperties.StoreKind.JPA &&
                                !useSelfConfigOrNewConfigJpa(resourceServerProperties);
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new MethodSecurityRepositoryHandler() {
                            @Override
                            public LightningExtMethodSecurityMetadataSource getRepository(ResourceServerProperties resourceServerProperties, ApplicationContext applicationContext,
                                                       PrePostInvocationAttributeFactory attributeFactory) {
                                return new JpaPrePostMethodSecurityMetadataSource(attributeFactory,
                                        applicationContext.getBean(JpaResourceMethodSecurityRepository.class),
                                        getModuleName(resourceServerProperties,applicationContext));
                            }
                        };
                    }
                }
        );


        // mongo
        HandlerFactory.registerHandler(new MethodSecurityRepositoryHandlerProvider() {
            @Override
            public boolean support(ResourceServerProperties resourceServerProperties) {
                return resourceServerProperties.getAuthorityConfig().getResourceAuthoritySaveKind() ==
                                ResourceServerProperties.StoreKind.MONGO &&
                        useSelfConfigOrNewConfigMongo(resourceServerProperties);
            }

            @NotNull
            @Override
            public HandlerFactory.Handler getHandler() {
                return new NewApplicationContextMongoRepositoryHandler();
            }
        });

        // mongo fallback
        HandlerFactory.registerHandler(
                new MethodSecurityRepositoryHandlerProvider() {
                    @Override
                    public boolean support(ResourceServerProperties resourceServerProperties) {
                        return resourceServerProperties.getAuthorityConfig().getResourceAuthoritySaveKind() == ResourceServerProperties.StoreKind.MONGO
                                && !useSelfConfigOrNewConfigMongo(resourceServerProperties);
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new MethodSecurityRepositoryHandler() {
                            @Override
                            public LightningExtMethodSecurityMetadataSource getRepository(ResourceServerProperties resourceServerProperties, ApplicationContext applicationContext, PrePostInvocationAttributeFactory attributeFactory) {
                                return new MongoPrePostMethodSecurityMetadataSource(
                                        attributeFactory,
                                        applicationContext.getBean(MongoTemplate.class),
                                        getModuleName(resourceServerProperties,applicationContext)
                                );
                            }
                        };
                    }
                }
        );


        // fallback
        HandlerFactory.registerHandler(
                new MethodSecurityRepositoryHandlerProvider() {
                    @Override
                    public boolean support(ResourceServerProperties resourceServerProperties) {
                        return true;
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return new MethodSecurityRepositoryHandler() {
                            @Override
                            public LightningExtMethodSecurityMetadataSource getRepository(ResourceServerProperties resourceServerProperties, ApplicationContext applicationContext, PrePostInvocationAttributeFactory attributeFactory) {
                                return new LightningPrePostMethodSecurityMetadataSource(attributeFactory,getModuleName(resourceServerProperties,applicationContext));
                            }
                        };
                    }
                }
        );


    }



}


