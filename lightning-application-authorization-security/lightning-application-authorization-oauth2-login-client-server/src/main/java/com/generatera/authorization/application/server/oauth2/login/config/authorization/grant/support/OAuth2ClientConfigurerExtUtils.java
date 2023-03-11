package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.DefaultOAuth2AuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuth2AuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuthorizedClientService;
import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 不建议使用,工具类存在混淆 ..
 */
public class OAuth2ClientConfigurerExtUtils {
    private OAuth2ClientConfigurerExtUtils() {
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
                                HttpSecurityBuilderUtils.getBean(builder, LightningOAuthorizedClientService.class)
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

}
