package com.generatera.central.oauth2.authorization.server.configuration.components.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
@AutoConfiguration
public class RegisteredClientConfiguration {
    /**
     * 也可以自己 实现 client repository ..
     *
     * 由于基于 generic-crud-service, AppService
     *
     * 使用的底层数据库是什么,那么就自动切换 ...
     */
    @ConditionalOnMissingBean(RegisteredClientRepository.class)
    public static class ClientRepositoryConfig {



        @Bean
        public RegisteredClientRepository registeredClientRepository(AppService appService) {
            return new LightningRegisteredClientRepository(appService);
        }


        @Bean
        public AppService appService() {
            return new DefaultAppServiceImpl();
        }

        // for appService ...

        @Bean
        public DefaultAppQueryHandler defaultAppQueryHandler() {
            return new DefaultAppQueryHandler();
        }

        @Bean
        public AppParamConverter appParamConverter() {
            return new AppParamConverter();
        }
    }
}
