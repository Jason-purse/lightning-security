package com.generatera.authorization.server.common.configuration;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * @author FLJ
 * @date 2023/1/9
 * @time 11:03
 * @Description 主要是 作为授权服务器的一些可配置属性 ...
 */
@Data
@ConfigurationProperties(prefix = "lightning.auth.server.common.component")
public class AuthorizationServerComponentProperties {
    /**
     * token 生成器的名称(暂时没有使用)
     */
    public static final String TOKEN_GENERATOR_NAME = "lightning.authentication.token.generator";

    /**
     * 提供者配置属性
     * 为元数据端点提供属性 为资源服务器 交换公钥 提供机会 ...
     */
    private final ProviderSettingProperties providerSettingProperties = new ProviderSettingProperties();

    /**
     * token 配置属性
     *
     * 授权中心颁发应用级别的 token 的配置处理 ..
     *
     * 此配置因授权服务器类型而不同,例如 oauth2-central-authorization-server 根本不会颁发应用级别的token ...
     * 就算它颁发token,也是根据请求client的token配置来进行token 颁发的相关配置,所以在某些授权服务器下,这个配置不会生效 ...
     * 当然当没有特殊处理的情况下,例如 oauth2-login-client-server,它本身就是一个普通的授权服务器,那么它需要此配置来配置
     * 应用级别的Token配置,从而使得token生成具有可参照的依据 ...
     */
    private final TokenSettings tokenSettings = new TokenSettings();


    private final AuthorizationStoreConfig authorizationStoreConfig = new AuthorizationStoreConfig();



    @Data
    public static class TokenSettings {

        /**
         * 访问 token 默认存储时常
         */
        public static Long DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE = 30 * 60 * 1000L;

        /**
         * 刷新 token 默认存储时常
         */
        public static Long DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE = 7 * 24 * 60 * 60 * 1000L;

        /**
         * 默认受众
         */
        public static String[] DEFAULT_AUDIENCES = new String[]{"*"};


        private Long accessTokenTimeToLive = DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE;

        /**
         * 生成的 token 类型
         */
        private LightningTokenType.LightningTokenValueType tokenValueType = LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE;

        private Boolean reuseRefreshToken = Boolean.TRUE;

        private Long refreshTokenTimeToLive = DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE;

        private String[] audiences = DEFAULT_AUDIENCES;





        public Long getAccessTokenTimeToLive() {
            return accessTokenTimeToLive == null ? DEFAULT_ACCESS_TOKEN_TIME_TO_LIVE : accessTokenTimeToLive - 5 * 1000L;
        }

        public Long getRefreshTokenTimeToLive() {
            return refreshTokenTimeToLive == null ? DEFAULT_REFRESH_TOKEN_TIME_TO_LIVE : refreshTokenTimeToLive - 5 * 1000L;
        }

        public Boolean getReuseRefreshToken() {
            return reuseRefreshToken != null ? reuseRefreshToken : Boolean.FALSE;
        }

    }




    @Data
    public static class ProviderSettingProperties {

        // 可以为空(自己自动生成)
        private String issuer;

        private String authorizationEndpoint = "/auth/v1/oauth2/authorize";

        private String tokenEndpoint = "/auth/v1/oauth2/token";

        private String jwkSetEndpoint = "/auth/v1/oauth2/jwks";

        private String tokenRevocationEndpoint = "/auth/v1/oauth2/revoke";

        private String tokenIntrospectionEndpoint = "/auth/v1/oauth2/introspect";

        private String oidcClientRegistrationEndpoint = "/auth/v1/connect/register";

        private String oidcUserInfoEndpoint = "/auth/v1/userinfo";
    }

    @Data
    public static class AuthorizationStoreConfig {
        /**
         * 授权存储方式
         */
        private final StoreKind storeKind = StoreKind.MEMORY;
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
