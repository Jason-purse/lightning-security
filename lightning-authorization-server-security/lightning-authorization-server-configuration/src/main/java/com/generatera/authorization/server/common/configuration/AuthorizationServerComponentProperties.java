package com.generatera.authorization.server.common.configuration;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueFormat;
import lombok.Data;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.AUTH_SERVER_COMPONENT_PREFIX;

/**
 * @author FLJ
 * @date 2023/1/9
 * @time 11:03
 * @Description 主要是 作为授权服务器的一些可配置属性 ...
 * <p>
 * <p>
 * 我们主要是想要集成token生成规范(借鉴oauth2的token生成) ...
 * 本质上其实和oauth2 类似,保留Oauth2的token 审查端点,token 撤销端点,包括 自己作为第三方提供商的一些提供者配置 ...
 * 能够有效的和 对应的资源服务器进行 token 工作协同,例如opaque token 省查 / jwt token 自解析 ...
 * <p>
 *
 * 本质上,token 版本控制 基于设置不同版本的filter 或者修改filter 代码实现 ...
 *
 * 那么如果框架库本身升级,也可以通过设置{@link  ServerProperties#getServlet()} 设置context-path 实现路径区分不同应用,
 * 例如通过nginx 实现不同版本的 应用转发 ..
 */
@Data
@ConfigurationProperties(prefix = AUTH_SERVER_COMPONENT_PREFIX)
public class AuthorizationServerComponentProperties {

    public static final String AUTH_SERVER_COMPONENT_PREFIX = "lightning.security.auth.server.common.component";
    /**
     * token 生成器的名称(暂时没有使用)
     */
    public static final String TOKEN_GENERATOR_NAME = "lightning.authentication.token.generator";


    private final AuthorizationStoreConfig authorizationStoreConfig = new AuthorizationStoreConfig();



    private final Permission permission = new Permission();

    /**
     * token 配置属性
     * <p>
     * 授权中心颁发应用级别的 token 的配置处理 ..
     * <p>
     * 此配置因授权服务器类型而不同,例如 oauth2-central-authorization-server 根本不会颁发应用级别的token ...
     * 就算它颁发token,也是根据请求client的token配置来进行token 颁发的相关配置,所以在某些授权服务器下,这个配置不会生效 ...
     * 当然当没有特殊处理的情况下,例如 oauth2-login-client-server,它本身就是一个普通的授权服务器,那么它需要此配置来配置
     * 应用级别的Token配置,从而使得token生成具有可参照的依据 ...
     */
    private TokenSettings tokenSettings = new TokenSettings();


    @Data
    public static class TokenSettings {
        /**
         * 默认受众
         */
        public static String[] DEFAULT_AUDIENCES = new String[]{"*"};


        private String[] audiences = DEFAULT_AUDIENCES;

        /**
         * 授权类型列表 ..
         */
        private List<LightningTokenType.LightningAuthenticationTokenType> grantTypes =
                Arrays.asList(
                        LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE,
                        LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE
                );

        private final AccessToken accessToken = new AccessToken();

        private final RefreshToken refreshToken = new RefreshToken();

        @Data
        public static class AccessToken {

            /**
             * 访问 token 默认存储时常
             */
            public static Long DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE = 30 * 60 * 1000L;

            /**
             * 生成的 token 类型
             */
            private LightningTokenType.LightningTokenValueType tokenValueType = LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE;

            /**
             * 生成的 token 的值的格式是 (JWT)
             * 这里的jwt 只是一个形式的代称(不等价于 JSON Web Tokens ), 因为所有的token 都是jwt生成的 ..
             */
            private LightningTokenValueFormat tokenValueFormat = LightningTokenValueFormat.JWT;

            private Long tokenTimeToLive = DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE;


            public Long getTokenTimeToLive() {
                return tokenTimeToLive == null ? DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE : tokenTimeToLive - 5 * 1000L;
            }


        }

        @Data
        public static class RefreshToken {


            /**
             * 刷新 token 默认存储时常
             */
            public static Long DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE = 7 * 24 * 60 * 60 * 1000L;

            /**
             * 生成的 token 类型
             */
            private LightningTokenType.LightningTokenValueType tokenValueType = LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE;

            /**
             * 生成的 token 的值的格式是 (JWT)
             * 这里的jwt 只是一个形式的代称(不等价于 JSON Web Tokens ), 因为所有的token 都是jwt生成的 ..
             */
            private LightningTokenValueFormat tokenValueFormat = LightningTokenValueFormat.JWT;

            private Boolean reuseRefreshToken = Boolean.TRUE;

            private Long tokenTimeToLive = DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE;

            public Long getTokenTimeToLive() {
                return tokenTimeToLive == null ? DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE : tokenTimeToLive - 5 * 1000L;
            }
        }
    }

    @Data
    public static class Permission {

        /**
         * url 白名单 - 放行,不需要token 校验
         */
        private List<String> urlWhiteList = Collections.emptyList();
    }


    @Data
    public static class AuthorizationStoreConfig {
        /**
         * 授权存储方式
         */
        private StoreKind storeKind = StoreKind.MEMORY;
        /**
         * lightning.auth.server.authorization.store.redis
         */
        private final Redis redis = new Redis("l.a.s.a.store.redis");
    }

    @Data
    public static class Redis {

        /**
         * key 前缀
         */
        private String keyPrefix;

        /**
         * value 保留 30分钟(但是不能够使用恰好30分钟,需要比正常时间少10 - 30秒)
         */
        private final Long DEFAULT_EXPIRED_TIME = 30 * 1000 * 60L;


        private Long expiredTimeDuration = DEFAULT_EXPIRED_TIME;

        public Redis(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        /**
         * 需要比正常时间少10 - 30秒
         *
         * @return expired_time
         */
        public Long getExpiredTimeDuration() {
            return expiredTimeDuration != null ? expiredTimeDuration : DEFAULT_EXPIRED_TIME - 10 * 1000L;
        }

        public static Redis of(String keyPrefix) {
            return new Redis(keyPrefix);
        }
    }


    public enum StoreKind {
        REDIS,
        JPA,
        MONGO,
        MEMORY
    }
}
