package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
/**
 * @author FLJ
 * @date 2022/12/29
 * @time 14:19
 * @Description Mongo OAuth2 Auhtorization service
 */
@AllArgsConstructor
public class MongoOAuth2AuthorizationService implements OAuth2AuthorizationService, LightningAuthorizationService<DefaultOAuth2Authorization> {

    private final  MongoTemplate mongoTemplate;

    @Override
    public void save(OAuth2Authorization authorization) {

    }

    @Override
    public void remove(OAuth2Authorization authorization) {

    }

    @Override
    public OAuth2Authorization findById(String id) {
        return null;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return null;
    }

    @Override
    public void save(DefaultOAuth2Authorization authorization) {

    }

    @Override
    public void remove(DefaultOAuth2Authorization authorization) {

    }

    @Override
    public DefaultOAuth2Authorization findAuthorizationById(String id) {
        return null;
    }

    @Override
    public DefaultOAuth2Authorization findByToken(String token, LightningTokenType.LightningAuthenticationTokenType tokenType) {
        return null;
    }
}
