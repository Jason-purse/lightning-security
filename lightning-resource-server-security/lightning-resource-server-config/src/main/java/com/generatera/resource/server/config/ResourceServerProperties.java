package com.generatera.resource.server.config;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 从 spring-security 2.0开始, spring security 已经基本上提供了对服务层方法的安全支持, 提供了 jsr-250 注解 以及 spring 原生的
 * {@link org.springframework.security.access.annotation.Secured} 注解 来提供支持.
 * 从 spring-security3.0开始,我们可以使用基于表达式的注解,你能够在单个bean上应用 security 注解,或者基于xml的配置下,使用 xml的
 * intercept-methods element 来装饰 bean 声明, 或者你能够基于aspect 风格的切入点跨越多个bean 形成统一风格的 安全支持 ..
 * <p>
 * <p>
 * 1. 启用方法安全
 * 在spring5.6中,我们能够通过@EnableMethodSecurity注解去启用基于注解的security 配置支持 ...
 * 也就是说,使用了这个配置,注解才会生效 ..
 * 它在许多方面上改进了 @EnableGlobalMethodSecurity ..
 * 1.1 使用 简化的 AuthorizationManager  api 来替代 元数据 源,配置属性,决定管理器,以及 voter(投票人),这简化重用以及自定义 ..
 * 1.2 偏好直接基于bean 配置,而不是继承 GlobalMethodSecurityConfiguration  去定制 bean ...
 * 1.3 内置使用spring aop 构建,移除了抽象并允许使用spring aop 构建块进行自定义
 * 1.4 对冲突注解进行检查 确保明确的安全配置
 * 1.5 兼容jsr-250
 * 1.6  默认启用了 @PreAuthorize, @PostAuthorize, @PreFilter, and @PostFilter ..
 */
@Data
@ConfigurationProperties(prefix = ResourceServerProperties.RESOURCE_SERVER_PREFIX)
public class ResourceServerProperties {
    public static final String RESOURCE_SERVER_PREFIX = "lightning.security.resource.server";

    private final TokenVerificationConfig tokenVerificationConfig = new TokenVerificationConfig();

    /**
     * resource url 白名单(如果连同 auth server,那么可以使用authorization server对应的白名单列表控制)
     * {@link AuthorizationServerComponentProperties#getPermission()}
     */
    private final Permission permission = new Permission();

    @Data
    public static class TokenVerificationConfig {
        /**
         * 默认使用Bearer jwt Token
         */
        private TokenType tokenType = TokenType.JWT;

        private final BearerTokenConfig bearerTokenConfig = new BearerTokenConfig();

        public enum TokenType {
            JWT,
            Opaque
        }

        @Data
        public static class BearerTokenConfig {
            /**
             * 需不需要 bearer token 前缀 ..
             */
            private boolean useHeader = Boolean.FALSE;
        }
    }

    @Data
    public static class Permission {

        private List<String> urlWhiteList;

    }
}
