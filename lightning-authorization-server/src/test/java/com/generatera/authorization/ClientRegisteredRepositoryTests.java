package com.generatera.authorization;

import com.generatera.authorization.oauth2.entity.OAuth2ClientEntity;
import com.generatera.authorization.oauth2.repository.LightningOAuth2ClientRepository;
import com.generatera.authorization.oauth2.service.LightningRegisteredClientRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

@SpringBootTest
@ContextConfiguration(classes = LightningAuthorizationApplication.class)
public class ClientRegisteredRepositoryTests {

    @Autowired
    private LightningOAuth2ClientRepository lightningOAuth2ClientRepository;

    @Autowired
    private LightningRegisteredClientRepository lightningRegisteredClientRepository;


    @Test
    public void test() {
        Optional<OAuth2ClientEntity> byId = lightningOAuth2ClientRepository.findById(3L);
        Assertions.assertTrue(byId.isPresent());

        System.out.println(byId.get());

    }


    @Test
    public void clientRegisteredRepository() {
        System.out.println(lightningRegisteredClientRepository.findById("3"));
    }
}
