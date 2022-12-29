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

    private Redis redis = new Redis();

    @Data
    public static class Redis {
        /**
         * key 前缀
         */
        private final  String DEFAULT_KEY_PREFIX = "lightning-auth-server-authorization-";

        private String keyPrefix = DEFAULT_KEY_PREFIX;
    }
}
