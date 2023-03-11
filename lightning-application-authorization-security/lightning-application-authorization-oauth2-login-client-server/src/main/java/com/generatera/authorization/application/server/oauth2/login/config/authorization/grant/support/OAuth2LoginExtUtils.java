package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import com.generatera.authorization.application.server.oauth2.login.config.OAuth2LoginUtils;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2GrantedAuthoritiesMapper;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.DefaultOAuth2AuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.DelegateOAuthorizedClientService;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuth2AuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuthorizedClientService;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.DelegateOAuth2AccessTokenResponseClient;
import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultPasswordTokenResponseClient;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 扩展工具类,例如增强原有功能,提供 pass grant 客户端 请求授权服务端授权的一些组件 ..
 */
public class OAuth2LoginExtUtils {
    private OAuth2LoginExtUtils() {
    }

    /**
     * authorized client repository 的默认配置 ..
     */
    public static <B extends HttpSecurityBuilder<B>> LightningOAuth2AuthorizedClientRepository getAuthorizedClientRepository(B builder) {
        LightningOAuth2AuthorizedClientRepository authorizedClientRepository = builder.getSharedObject(LightningOAuth2AuthorizedClientRepository.class);
        if (authorizedClientRepository == null) {
            authorizedClientRepository = getAuthorizedClientRepositoryBean(builder);
            if (authorizedClientRepository == null) {
                authorizedClientRepository = new DefaultOAuth2AuthorizedClientRepository(
                        new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(
                                /**
                                 * @see com.generatera.authorization.application.server.oauth2.login.config.ApplicationAuthorizedClientConfiguration
                                 */
                                HttpSecurityBuilderUtils.getBean(builder, LightningOAuthorizedClientService.class, () -> {
                                    return new DelegateOAuthorizedClientService(
                                            new InMemoryOAuth2AuthorizedClientService(
                                                    OAuth2LoginUtils.getClientRegistrationRepository(builder)
                                            )
                                    );
                                })
                        ));
            }

            builder.setSharedObject(LightningOAuth2AuthorizedClientRepository.class, authorizedClientRepository);
        }

        return authorizedClientRepository;
    }

    private static <B extends HttpSecurityBuilder<B>> LightningOAuth2AuthorizedClientRepository getAuthorizedClientRepositoryBean(B builder) {
        Map<String, LightningOAuth2AuthorizedClientRepository> authorizedClientRepositoryMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(builder.getSharedObject(ApplicationContext.class), LightningOAuth2AuthorizedClientRepository.class);
        if (authorizedClientRepositoryMap.size() > 1) {
            throw new NoUniqueBeanDefinitionException(OAuth2AuthorizedClientRepository.class, authorizedClientRepositoryMap.size(), "Expected single matching bean of type '" + OAuth2AuthorizedClientRepository.class.getName() + "' but found " + authorizedClientRepositoryMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(authorizedClientRepositoryMap.keySet()));
        } else {
            return !authorizedClientRepositoryMap.isEmpty() ? authorizedClientRepositoryMap.values().iterator().next() : null;
        }
    }

    public static <B extends HttpSecurityBuilder<B>> LightningPasswordGrantAuthenticationRequestConverter getPasswordGrantAuthenticationRequestConverter(B builder) {
        return HttpSecurityBuilderUtils.getBean(builder, LightningPasswordGrantAuthenticationRequestConverter.class, () -> {
            return new PasswordGrantAccessTokenAuthenticationConverter(OAuth2LoginUtils.getClientRegistrationRepository(builder));
        });
    }

    /**
     * 提供password grant 支持的 dao authentication provider
     * 提供最基本的,没必要提供全部 ..
     */
    public static <B extends HttpSecurityBuilder<B>> LightningOAuth2PasswordGrantDaoAuthenticationProvider getOAuth2ExtAuthenticationDaoProvider(B builder) {

        return HttpSecurityBuilderUtils.getBean(builder, LightningOAuth2PasswordGrantDaoAuthenticationProvider.class, () -> {
            LightningOAuth2UserService bean = HttpSecurityBuilderUtils.getOptionalBean(builder, LightningOAuth2UserService.class);
            LightningOAuth2GrantedAuthoritiesMapper authoritiesMapper = HttpSecurityBuilderUtils.getBean(builder, LightningOAuth2GrantedAuthoritiesMapper.class);
            if (bean == null) {
                LightningOidcUserService oidcUserService = HttpSecurityBuilderUtils.getBean(builder, LightningOidcUserService.class);
                OidcPasswordGrantAccessTokenDaoAuthenticationProvider authenticationProvider = new OidcPasswordGrantAccessTokenDaoAuthenticationProvider(
                        new DelegateOAuth2AccessTokenResponseClient<>(
                                new DefaultPasswordTokenResponseClient()::getTokenResponse
                        ),
                        oidcUserService,
                        getAuthorizedClientRepository(builder)
                );
                authenticationProvider.setAuthoritiesMapper(authoritiesMapper);
                return authenticationProvider;
            } else {
                // oauth2 service
                DefaultPasswordGrantAccessTokenDaoAuthenticationProvider authenticationProvider = new DefaultPasswordGrantAccessTokenDaoAuthenticationProvider(
                        new DelegateOAuth2AccessTokenResponseClient<>(
                                new DefaultPasswordTokenResponseClient()::getTokenResponse
                        ),
                        bean,
                        getAuthorizedClientRepository(builder)
                );
                authenticationProvider.setAuthoritiesMapper(authoritiesMapper);
                return authenticationProvider;
            }
        });
    }
}
