package com.generatera.authorization.server.configure.store.authorizationinfo;

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
public class MongoOAuth2AuthorizationService implements OAuth2AuthorizationService {

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
}
