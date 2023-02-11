package com.generatera.resource.server.config.method.security;

import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.security.authorization.server.specification.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.LinkedList;
import java.util.List;

/**
 * 数据库操作的配置
 *
 * 分为直接依赖资源服务器配置的数据库连接方式,还是 连接到对应的其他缓存资源权限的数据库 ..
 * {@link  ResourceServerProperties#getAuthorityConfig()} 根据它的存储分类进行决定
 *
 * 如果是jpa,没有额外通过{@link ResourceServerProperties.AuthorityConfig.CacheConfig#getJpaCacheConfig()}
 * 的相关数据库配置 以及 启用标志设置,那么默认使用当前应用关联的 数据库配置 .
 * 否则,通过{@link  MethodSecurityMetadataRepositoryManager} 进行仓库的创建 ...
 *
 * 其他存储分类,同理 .. 只要{@link  MethodSecurityMetadataRepositoryManager} 支持 ..
 */
@AutoConfiguration
@Configuration
public class MethodSecurityMetadataRepositoryConfiguration extends PropertiesBindImportSelector<ResourceServerProperties> {

    public MethodSecurityMetadataRepositoryConfiguration(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {

        List<String> candidates = new LinkedList<>();
        ResourceServerProperties properties = getProperties();
        if (properties.getAuthorityConfig().getResourceAuthoritySaveKind() == ResourceServerProperties.StoreKind.JPA) {

            // 有一个问题 ...
            // 是否提供了自定义的 数据库地址 ..
            ResourceServerProperties.AuthorityConfig.JpaCacheConfig jpaCacheConfig = properties.getAuthorityConfig().getCacheConfig()
                    .getJpaCacheConfig();

            if (!jpaCacheConfig.isEnable()) {
                candidates.add(JpaDataBaseOperationsConfiguration.class.getName());
            }
        }
        return  candidates.toArray(String[]::new);
    }


    /**
     * 通过新的应用上下文来 处理 ..
     */
    @EnableJpaRepositories("com.generatera.resource.server.config.method.security.repository")
    @EntityScan("com.generatera.resource.server.config.method.security.entity")
    static class JpaDataBaseOperationsConfiguration {

    }


    /**
     * 添加此配置是,为了能够方便的添加某些注解,而无需查看 RepositoryManager ..的相关代码 ..
     *
     * 例如添加事务支持,直接加入即可 ..
     */
    static class MongoDataBaseOperationsConfiguration {

    }

}
