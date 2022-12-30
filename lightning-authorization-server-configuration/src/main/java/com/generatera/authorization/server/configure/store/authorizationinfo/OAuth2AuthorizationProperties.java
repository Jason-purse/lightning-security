package com.generatera.authorization.server.configure.store.authorizationinfo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author FLJ
 * @date 2022/12/29
 * @time 15:59
 * @Description 有关存储的属性
 */
@Data
@ConfigurationProperties(prefix = "lightning.auth.server.authorization.store")
public class OAuth2AuthorizationProperties {

    /**
     * 默认使用redis
     */
    private StoreKind kind = StoreKind.REDIS;


    private Redis redis = new Redis();

    @Data
    public static class Redis {
        /**
         * key 前缀
         */
        private final  String DEFAULT_KEY_PREFIX = "lightning-auth-server-authorization-";

        private String keyPrefix = DEFAULT_KEY_PREFIX;

        /**
         *  value 保留 30分钟(但是不能够使用恰好30分钟,需要比正常时间少10 - 30秒)
         */
        private final Long DEFAULT_EXPIRED_TIME = 30 * 1000 * 60L;


        private Long expiredTimeDuration = DEFAULT_EXPIRED_TIME;

        /**
         * 需要比正常时间少10 - 30秒
         * @return expired_time
         */
        public Long getExpiredTimeDuration() {
            return expiredTimeDuration - 10 * 1000L;
        }
    }


    public enum  StoreKind {
       REDIS,
       JPA,
       MONGO
    }
}
