package com.generatera.resource.server.config;

import com.generatera.resource.server.config.method.security.LightningPostAuthorize;
import com.generatera.resource.server.config.method.security.LightningPreAuthorize;
import com.generatera.security.authorization.server.specification.components.token.MacAlgorithm;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.generatera.resource.server.config.ResourceServerProperties.AuthorityConfiguration.CacheConfig.JpaCacheConfigPrefix;
import static com.generatera.resource.server.config.ResourceServerProperties.AuthorityConfiguration.CacheConfig.MongoCacheConfigPrefix;

/**
 * 授权架构:
 * 1. 权限
 * Authentication 保留了当前主体的所被授予的权限, 最终会被 AccessDecisionManager 实例使用用来做出授权决定...
 * 那多数管理器能轻松的获取GrantedAuthority 中的字符串,如果无法标识为一个字符串(他很复杂),那么这个Authority应该考虑为复杂的,并且
 * getAuthority()必须返回 null ...
 * 1.1 “复杂”GrantedAuthority 的一个示例是存储适用于不同客户帐号的操作和权限阈值列表的实现,由于这种形式下,表现为一个字符串很困难,所以返回Null即可,
 * 这指示 AccessDecisionManager 需要支持特定的GrantedAuthority实现来理解它的内容 ...
 * <p>
 * 1.2 Spring security 包括了一个具体的GrantAuthority实现,具体的是 SimpleGrantedAuthority,这个实现让特定于用户的内容能够转换为GrantedAuthority,
 * 所有的AuthenticationProvider 实例使用了SimpleGrantedAuthority 的安全架构来填充 Authentication 对象 ..
 * <p>
 * 2. 预执行处理
 * SpringSecurity 提供了拦截器去访问安全对象,例如一个方法执行或者web 请求,一个预执行决定是否允许继续处理是通过 AccessDecisionManager ...
 * 3. AuthorizationManager
 * 这替代了 AccessDecisionManager 以及 AccessDecisionVoter ...
 * 应用能够自定义一个 AccessDecisionManager 或者 AccessDecisionVoter 鼓励去改变使用 AuthorizationManager ...
 * AuthorizationManager 是被AuthorizationFilter 调用的并且负责做出最终的访问控制决定..
 * AuthorizationManager的check 方法将传递所有相关的信息来做出授权决定,尤其是,传递一个安全的对象,针对实际安全对象调度中的参数也能够被检测 ..
 * 例如,假设安全对象是一个MethodInvocation,它能够容易的查询MethodInvocation - 为了Customer 参数,并且在AuthorizationManager中实现某些
 * 种类的安全逻辑去确保主体(principal)在这些customizer上进行操作 .. 实现期待返回一个 正向的AuthorizationDecision(如果访问成功授予),
 * 否则期待一个负AuthorizationDecision,并且如果弃权,则返回 null ...
 * verify 调用check 并且在访问拒绝的情况下抛出AccessDeniedException ...
 * <p>
 * 4. 基于代理的 AuthorizationManager 实现
 * 虽然用户可以实现自己的 AuthorizationManager 来控制授权的所有方面，但 Spring Security 附带了一个可以与各个 AuthorizationManager 协作的委托 AuthorizationManager。
 * RequestMatcherDelegatingAuthorizationManager 将使用最合适的代理AuthorizationManager来匹配请求,对于方法安全,你能够使用 AuthorizationManagerBeforeMethodInterceptor
 * 以及 AuthorizationManagerAfterMethodInterceptor ...
 * <a target="_blank" href="https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html#authz-authorization-manager-implementations">
 * AuthorizationManager 实现</a>
 * <p>
 * 5. AuthorityAuthorizationManager
 * 大多数 SpringSecurity提供的 AuthorizationManager  是 AuthorityAuthorizationManager, 它根据当前Authentication中的 authorities进行配置,并且它将返回 AuthorizationDecision(如果允许访问),
 * 否则返回一个 拒绝 ..
 * 6. AuthenticatedAuthorizationManager
 * AuthenticatedAuthorizationManager 能够用来区分 匿名和完全认证 以及记住我认证的用户,许多站点允许在记住我的认证情况下访问受限资源,但是需要用户去配置它们的身份(当完全访问的时候) ...
 * 7. Custom Authorization Managers
 * 当然,我们可以自定义授权管理器并且 增加自己的访问控制逻辑,你能够特定于应用(与业务逻辑相关),或者实现某些安全管理逻辑,例如你可以创建一个查询 Open Policy Agent 或者 自己的授权数据库的实现 ...
 * 你能够在Spring web 站点上发现一个<a href="https://spring.io/blog/2009/01/03/spring-security-customization-part-2-adjusting-secured-session-in-real-time" target="_blank">blog article</a>
 * 去学习如何使用遗留的AccessDecisionVoter实时拒绝帐户已被暂停的用户访问。 相反你可以通过实现 AuthorizationManager 实现相同的结果 ..
 * <p>
 * 8. 适配AccessDecisionManager  和 AccessDecisionVoters 。。
 * 在AuthorizationManager 之前,spring security发布了 AccessDecisionManager and AccessDecisionVoter...
 * 某些情况下,例如迁移老的应用,可能想要使用 AuthorizationManager  然后代理到 AccessDecisionManager  以及 AccessDecisionVoter ..
 * 那么为了这么做,使用装饰的AuthorizationManager ..
 * <pre>
 *          @Component
 * public class AccessDecisionManagerAuthorizationManagerAdapter implements AuthorizationManager {
 *     private final AccessDecisionManager accessDecisionManager;
 *     private final SecurityMetadataSource securityMetadataSource;
 *
 *     @Override
 *     public AuthorizationDecision check(Supplier<Authentication> authentication, Object object) {
 *         try {
 *             Collection<ConfigAttribute> attributes = this.securityMetadataSource.getAttributes(object);
 *             this.accessDecisionManager.decide(authentication.get(), object, attributes);
 *             return new AuthorizationDecision(true);
 *         } catch (AccessDeniedException ex) {
 *             return new AuthorizationDecision(false);
 *         }
 *     }
 *
 *     @Override
 *     public void verify(Supplier<Authentication> authentication, Object object) {
 *         Collection<ConfigAttribute> attributes = this.securityMetadataSource.getAttributes(object);
 *         this.accessDecisionManager.decide(authentication.get(), object, attributes);
 *     }
 * }
 *      </pre>
 * <p>
 * 然后关联到 SecurityFilterChain中的过滤器上 ..
 * 或者仅仅调用 AccessDecisionVoter,你能够:
 * <pre>
 *     @Component
 * public class AccessDecisionVoterAuthorizationManagerAdapter implements AuthorizationManager {
 *     private final AccessDecisionVoter accessDecisionVoter;
 *     private final SecurityMetadataSource securityMetadataSource;
 *
 *     @Override
 *     public AuthorizationDecision check(Supplier<Authentication> authentication, Object object) {
 *         Collection<ConfigAttribute> attributes = this.securityMetadataSource.getAttributes(object);
 *         int decision = this.accessDecisionVoter.vote(authentication.get(), object, attributes);
 *         switch (decision) {
 *         case ACCESS_GRANTED:
 *             return new AuthorizationDecision(true);
 *         case ACCESS_DENIED:
 *             return new AuthorizationDecision(false);
 *         }
 *         return null;
 *     }
 * }
 * </pre>
 * <p>
 * 9. 体系化角色
 * 推荐参与到应用中的角色应该自动包括其他角色,例如在应用中可能存在admin和用户角色的概念,你可能想要admin能够像普通用户那样做任何事情 ..
 * 为了实现这,你能够确保所有admin 用户关联了 user 角色,除此之外,你能够修改每一个访问约束(当需要"user"角色时,同样包含 admin角色),
 * 当系统中存在大量的角色时,这将非常的复杂 ...
 * 角色体系的使用允许你配置那些角色(或者那些authorities)应该包含其他, Spring Security的 RoleVoter的扩展版本,RoleHierarchyVoter,
 * 它使用RoleHierarchy进行配置,这样它能够获取这个用户被赋予了那些所有可达的authorities, 例如示例配置:
 * <pre>
 *          @Bean
 * AccessDecisionVoter hierarchyVoter() {
 *     RoleHierarchy hierarchy = new RoleHierarchyImpl();
 *     hierarchy.setHierarchy("ROLE_ADMIN > ROLE_STAFF\n" +
 *             "ROLE_STAFF > ROLE_USER\n" +
 *             "ROLE_USER > ROLE_GUEST");
 *     return new RoleHierarchyVoter(hierarchy);
 * }
 *      </pre>
 * <p>
 * 这里有四种角色,ROLE_ADMIN ⇒ ROLE_STAFF ⇒ ROLE_USER ⇒ ROLE_GUEST,当用户使用admin 认证,那么它将包含所有4中角色,当被AuthorizationManager
 * 评估安全约束时,将会适配去调用 RoleHierarchyVoter, ">" 符号表示包含的意思 ..
 * <p>
 * Role 体系提供了简化访问控制配置数据的便捷语义并且减少了 authorities的数量(你需要分配给用户的),
 * 对于更复杂的需求，您可能希望在应用程序所需的特定访问权限与分配给用户的角色之间定义逻辑映射，并在加载用户信息时在两者之间进行转换。
 * <p>
 * 除了角色体系,你可以使用权限点和角色关联,这样 所有角色校验的是最终的权限点 ....
 * <p>
 * 10. 遗留的授权组件
 * Spring Security包含了某些遗留的组件,因为它们尚未删除..
 * 10.1 AccessDecisionManager
 * 它被 AbstractSecurityInterceptor  调用并做出最终的访问控制决定 ...
 * decide方法 将传递所有的相关信息来做出一个决定,AuthorizationManager.supports(ConfigAttribute)方法将会被 AbstractSecurityInterceptor  在
 * 启动时进行调用来决定一个 AccessDecisionManager  是否能够处理 ConfigAttribute .
 * supports(Class) 方法被安全连接器实现调用去确保 配置 AccessDecisionManager  支持 在安全拦截器中出现的安全对象的类型 ...
 * 10.2 基于 Voting 的 AccessDecisionManager 实现
 * 当用户实现自己的 AccessDecisionManager  时去控制授权的所有方面,Spring security提供了各种 基于voting的 AccessDecisionManager ..
 * 也就是各种AccessDecisionVoter  实现用于 授权决定投票,AccessDecisionManager  决定是否抛出 AccessDeniedException  ...
 * <p>
 * AccessDecisionVoter  有三个方法,vote() 会返回int值,是AccessDecisionVoter 的静态字段ACCESS_ABSTAIN, ACCESS_DENIED and ACCESS_GRANTED,
 * 一个voting 实现如果返回 ACCESS_ABSTAIN  则表示弃权(如果它有意见,则返回拒绝或者授权) ..
 * 当然Spring security为我们提供了三种实现 ,ConsensusBased 基于非弃权的投票来一致决定授权还是拒绝 ...
 * 提供的属性来控制在事件中的投票的质量,或者当所有投票都弃权时的 最终控制行为 ...
 * AffirmativeBased  实现是 只要有一个授权访问同意(换句话说,拒绝将会被忽略),像ConsensusBased  ,有一个参数同样能够通知最终行为(
 * 当所有投票都弃权时) ...
 * UnanimousBased 表示必须全体一致访问授权(忽略弃权),否则拒绝(和AffirmativeBased  相反), 同样有一个参数可以控制最终行为(当所有投票弃权) ..
 * <p>
 * 当然我们可以存在自定义的实现( 以不同的方式 投票计算)
 * <p>
 * 10.3 RoleVoter
 * 基于角色进行投票, 如果任何 ConfigAttribute  以ROLE_前缀开始,它将投票授权访问(如果authority 能够呈现为一个字符串)并且完全等于一个或者多个
 * 以ROLE_前缀开始的ConfigAttributes, 如果不匹配,则RoleVoter 投票拒绝, 如果 没有ConfigAttribute 以ROLE_开始,它投票弃权 ...
 * 因为vote传递的是 ConfigAttributes ...
 * <p>
 * 10.4 AuthenticatedVoter
 * 能够区分 各种用户, 许多站点包含了记住我认证下能够访问的受限资源,但是全部访问需要完全登录 ...(确认身份) ..
 * 当你有一个 IS_AUTHENTICATED_ANONYMOUSLY  属性来授权匿名访问,那么这个属性将会被 AuthenticatedVoter ...
 * <p>
 * 10.5 Custom Voters
 * 自定义的访问逻辑控制 ...
 * <a href="https://spring.io/blog/2009/01/03/spring-security-customization-part-2-adjusting-secured-session-in-real-time" target="_blank">blog article</a>
 * 去了解如何使用voter 去实时拒绝被暂停账户的拒绝访问 。。。
 * 像Spring Security其他部分,AfterInvocationManager 有单个具体实现,AfterInvocationProviderManager, 它轮询 AfterInvocationProviders 列表,
 * 每一个 AfterInvocationProvider 允许修改返回对象或者抛出 AccessDeniedException, 由于修改,前面修改对象的结果将传递到列表中的下一个 ...
 * <p>
 * 请知道,如果你使用 AfterInvocationManager, 你将仍然需要配置属性(configuration attributes 去允许 MethodSecurityInterceptor's 的 AccessDecisionManager  去允许操作)
 * 因为当你不允许执行,那么执行之后是不可能执行的, 如果你使用的AccessDecisionManager 没有定义相关的特殊secure 方法执行的配置属性,则将会导致AccessDecisionVoter  投票访问拒绝 ..
 * 最终,如果 AccessDecisionManager 的属性allowIfAllAbstainDecisions = false,AccessDeniedException  将会抛出, 你可以设置它(尽管这不是推荐方式),或者简单确定
 * 只要有一个配置属性允许操作即可让 AccessDecisionVoter  投票授权 访问,后者推荐的方式是通过ROLE_USER或者 ROLE_AUTHENTICATED 配置属性 ...
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
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
 * <p>
 * 2. 启用全局方法安全
 * 通过在@Configuration类上使用@EnableGlobalMethodSecurity 来启用基于注解的安全 ...
 * 例如启用@Secured 注解 ..
 * 并且它是通过访问决定管理器来做出实际的处理,和启用方法安全不同 ..
 * 2.1 如果想起用 jsr-250
 * 则设置@EnableGlobalMethodSecurity(jsr250Enabled = true) 即可 .
 * 这能够让你基于标准并使用简单的基于角色的约束来提供方法安全，但是没有spring security本身注解的能力,为了使用新的基于表达式的语法
 * 2.2 使用基于表达式的语法
 *
 * @EnableGlobalMethodSecurity(prePostEnabled = true)
 * 当然，基于表达式的注解能够在处理检查用户的授权列表之后还可以检查简单的角色信息 ...
 * <p>
 * 也就是说,method security 优化了全局方法安全,它仅仅只需要针对特定的方法进行配置即可,无需继承配置并实现繁琐的覆盖 ...
 * 3. 全局方法安全配置
 * 某些时候,需要执行相比于@EnableGlobalMethodSecurity注解更加复杂的操作,对此,你能够继承GlobalMethodSecurityConfiguration,
 * 并确保@EnableGlobalMethodSecurity注解呈现在你的子类上,例如你可以提供自定义的MethodSecurityExpressionHandler ...
 * <p>
 * 4.注意事项
 * 4.1 默认 是基于spring-aop的，也就是仅对bean 进行方法安全处理,如果需要对域对象进行处理(例如通过 new Operator)创建的,那么你需要使用
 * AspectJ ...
 * 4.2 同一个方法应该尽量启用一种类型的注解,如果出现多个将会出现未知行为,并且仅仅只有一种注解将会被应用 ...
 * 4.3 通过切入点 实现方法安全
 * <protect-pointcut/> 是一个启用方法安全的元素,通过声明就可以实现方法安全 ..
 * <pre>
 *          <global-method-security>
 *          <protect-pointcut expression="execution(* com.mycompany.*Service.*(..))"
 * 	            access="ROLE_USER"/>
 *          </global-method-security>
 *      </pre>
 */
@Data
@ConfigurationProperties(prefix = ResourceServerProperties.RESOURCE_SERVER_PREFIX)
public class ResourceServerProperties {

    public static final String RESOURCE_SERVER_PREFIX = "lightning.security.resource.server";

    public final static String AUTHORITY_CONFIG_PREFIX = RESOURCE_SERVER_PREFIX + ".authorityConfig";


    private final TokenVerificationConfig tokenVerificationConfig = new TokenVerificationConfig();


    private final AuthorityConfiguration authorityConfig = new AuthorityConfiguration();


    @Data
    public static class TokenVerificationConfig {
        /**
         * 默认使用Bearer jwt Token
         */
        private TokenType tokenType = TokenType.JWT;

        /**
         * 从Url上进一步配置
         *
         * 其他类型必须提供jwk 的值 ..
         */
        private JwkSourceCategory jwkSourceCategory = JwkSourceCategory.JWK_OR_ISSUER_URL;

        private final Rsa256Jwk rsa256Jwk = new Rsa256Jwk();

        private final SecretJwk secretJwk = new SecretJwk();



        private final BearerTokenConfig bearerTokenConfig = new BearerTokenConfig();


        public enum TokenType {
            JWT,
            Opaque
        }

        @Data
        public static class Rsa256Jwk {
            private String value;
        }

        @Data
        public static class SecretJwk {

            private String value;

            private String algorithm = MacAlgorithm.HS256.getName();
        }




        @Data
        public static class BearerTokenConfig {
            /**
             * 需不需要 bearer token 前缀 ..
             */
            private boolean useHeader = Boolean.FALSE;
        }

        public static enum JwkSourceCategory {
            RSA256,
            SECRET,
            JWK_OR_ISSUER_URL
        }
    }



    public enum StoreKind {
        REDIS,
        JPA,
        MONGO,
        MEMORY
    }


    @Data
    public static class AuthorityConfiguration {

        public static final String CACHE_CONFIG_PREFIX = AUTHORITY_CONFIG_PREFIX + ".cacheConfig";

        /**
         * 默认使用{@link JwtExtClaimNames#SCOPE_CLAIM}
         * 可以设定 ...
         */
        private String authorityName;

        /**
         * 资源服务器 上的资源存储方式
         * <p>
         * 专门针对{@link LightningPreAuthorize} 和{@link LightningPostAuthorize} 处理
         */
        private StoreKind resourceAuthoritySaveKind = StoreKind.MEMORY;


        /**
         * 模块名称,可以使用 spring.application.name 作为兜底策略 ..
         */
        private String moduleName;


        /**
         * 支持缓存配置 ...
         */
        private final CacheConfig cacheConfig = new CacheConfig();


        private String invalidTokenErrorMessage;

        private String filterAccessDeniedErrorMessage;

        /**
         * 暂时不用 ...
         */
        private String methodSecurityAccessDeniedErrorMessage;

        /**
         * 默认在启动的时候 进行 pre / psot 扫描 ..
         */
        private boolean enableMethodPrePostAuthorityScan = true;

        @Data
        public static class CacheConfig {


            public static final String MongoCacheConfigPrefix = CACHE_CONFIG_PREFIX + ".mongoCacheConfig";

            public static final String JpaCacheConfigPrefix = CACHE_CONFIG_PREFIX + ".jpaCacheConfig";


            public static final long DEFAULT_EXPIRED_DURATION = 5 * 60 * 1000;

            /**
             * 是否支持强制更新
             * 例如,提供 webhook 给其他 服务器(进行 触发强制更新) ...
             */
            private boolean supportForceSupport = false;


            /**
             * 默认5分钟 ..
             */
            private long expiredDuration = DEFAULT_EXPIRED_DURATION;


            /**
             * jpa 缓存配置 ..
             */
            private final JpaCacheConfig jpaCacheConfig = new JpaCacheConfig();

            private final MongoCacheConfig mongoCacheConfig = new MongoCacheConfig();
        }



        @Data
        public static class JpaCacheConfig  {

            public static final String DataSourceConfigPrefix = JpaCacheConfigPrefix + ".dataSourceProperties";


            /**
             * 配置数据库信息
             */
            private final DataSourceProperties dataSourceProperties = new DataSourceProperties();

            /**
             * 配置jpa 属性信息
             */
            private final JpaProperties jpaProperties = new JpaProperties();


            /**
             * 默认false, 需要显式启动 ..
             */
            private boolean enable;
        }


        @Data
        public static class MongoCacheConfig {

            public static final String mongoClientPropertiesPrefix = MongoCacheConfigPrefix + ".mongoProperties";

            private final MongoProperties mongoProperties = new MongoProperties();

            /**
             * 是否连接其他数据库,而不是当前应用的数据库
             */
            private boolean enable;

        }

    }
}
