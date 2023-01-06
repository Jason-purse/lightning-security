package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * @author FLJ
 * @date 2023/1/6
 * @time 11:13
 * @Description  覆盖了 spring resource server的默认配置,你可以提供 issuer 进行处理 ...
 *
 * 如果你不提供这个配置,它自动和 spring resource server的配置兼容 ..
 */
@Data
@ConfigurationProperties(prefix = "lightning.resource.server.oauth2.jwt.bearer.token")
public class OAuth2JwtBearerTokenResourceServerProperties {

    private final AuthorizationServerInfoSettings authorizationServerInfoSettings =
            new AuthorizationServerInfoSettings();

    @Data
    public static class AuthorizationServerInfoSettings {

        private String issuer;

    }

}
