package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.provider.AuthorizationServerNimbusJwkSetEndpointFilter;
import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationServerMetadataEndpointFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

import java.util.List;

/**
 * 实现 授权服务器的启动 ...
 *
 * 1. 根据 {@link LightningAuthServerConfigurer} 进行授权服务器的配置自动配置 ...
 *      对于资源服务器,在检测到 存在{@link LightningAuthServerConfigurer}的时候,将使用 {@link LightningAuthServerConfigurer}
 *      进行资源服务器配置 ...
 * 2. 当不存在oauth2 授权服务器的情况下,填充公共的约定(例如 Provider元数据提供, JWk公钥等获取方式配置) ..
 *
 * @see LightningAuthServerConfigurer
 * @see OAuth2AuthorizationServer
 * @see AuthorizationServerMetadataEndpointFilter
 * @see AuthorizationServerNimbusJwkSetEndpointFilter
 */
@Slf4j
public class AuthExtSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>  {

    private final List<LightningAuthServerConfigurer> configurers;


    public AuthExtSecurityConfigurer(List<LightningAuthServerConfigurer> configurerList) {
        this.configurers = configurerList;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {

        ApplicationAuthExtConfigurerUtils.getJwkSourceProvider(builder);
        for (LightningAuthServerConfigurer configurer : configurers) {
            configurer.configure(builder);
        }

    }



}
