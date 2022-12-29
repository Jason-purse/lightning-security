package com.generatera.authorization;


import com.generatera.authorization.server.configure.client.LightningRegisteredClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = LightningAuthorizationApplication.class)
public class ClientRegisteredRepositoryTests {

    @Autowired
    private LightningRegisteredClientRepository lightningRegisteredClientRepository;


    @Test
    public void clientRegisteredRepository() {
        System.out.println(lightningRegisteredClientRepository.findById("3"));
    }
}
