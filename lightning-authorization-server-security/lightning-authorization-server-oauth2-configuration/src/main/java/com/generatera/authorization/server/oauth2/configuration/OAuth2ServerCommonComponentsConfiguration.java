package com.generatera.authorization.server.oauth2.configuration;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.LightningOAuth2ServerConfigurer;
import com.generatera.authorization.application.server.config.token.LightningOAuth2ServerTokenGenerator;
import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * oauth2 server configuration
 * 必须出现在 xxx-login configuration 之前 ...
 *
 * 同样 oauth2 server配置 需要配置在 它的通用组件配置之前,因为需要做出一些策略判断 ..
 *
 * 这个项目主要是插入一些 oauth2 server 特有的组件(需要扩展的) ... 但是不属于中央授权中心的一些组件 ..
 * 也就是虽然我们遵循oauth2 规范,但是我们不希望全面引入 oauth2 server 项目的全部内容..
 *
 * authorization-server-configuration 已经定义了 任何授权中心符合oauth2部分规范的公共组件 ..
 * 但是对于oauth2 server 它本身还需要一些token 生成相关的东西或者其他东西,这属于oauth2 server 规范特有的(不属于我们所抽象的公共组件部分）
 *
 * 因为我们抽象的是token 生成规范(用户(access token / refresh token)的规范) ..
 *
 * 而这里属于 服务端对 oauth2 client的token 生成规范,所以不属于我们所抽象的一部分 ...
 *
 *
 * todo
 * 但是当仅作为一个 oauth2 server(并包含了表单登陆或者简单登陆方式),那么我希望表单登陆返回的token 能够和 oauth2 access token 互通 ..
 * 这是目前正在完善的部分
 *
 * 也就是oauth2 server token generator 有可能在与其他登陆方式组合时作为最终生成 token的手段 ...
 * 有可能覆盖oauth2 部分规范的公共token 生成组件的配置 ...
 */
@Configuration
@AutoConfigureBefore({ApplicationAuthServerConfig.class,AuthorizationServerCommonComponentsConfiguration.class})
@EnableConfigurationProperties(AuthorizationServerOAuth2CommonComponentsProperties.class)
@Import(OAuth2ServerSwitchImportSelector.class)
public class OAuth2ServerCommonComponentsConfiguration {

    public static class DefaultOAuth2ServerCommonComponentsConfiguration {
        // 它需要解码器 ..
        @Bean
        public LightningOAuth2ServerConfigurer oAuth2ServerConfigurer(
                @Autowired(required = false)
                LightningOAuth2ServerTokenGenerator serverTokenGenerator

        ) {
            return new LightningOAuth2ServerConfigurer() {
                @Override
                public void configure(OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer) {

                    // 配置 token 生成器
                    if(serverTokenGenerator != null) {
                        authorizationServerConfigurer.tokenGenerator(serverTokenGenerator);
                    }
                    else {
                        // 手动生成 ..
                        // 也就是用户如果不遵循 LightningOAuth2ServerTokenGenerator
                        // 会自动注入一个 LightningOAuth2ServerTokenGenerator ...

                        // 这样,能够让表单登陆 和 oauth2 server 共存的时候,使用oauth2 的 token生成器 ..
                        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator
                                = OAuth2ConfigurerExtUtils.getTokenGenerator(authorizationServerConfigurer.and());
                        authorizationServerConfigurer.tokenGenerator(tokenGenerator);
                    }
                }
            };
        }
    }


}
