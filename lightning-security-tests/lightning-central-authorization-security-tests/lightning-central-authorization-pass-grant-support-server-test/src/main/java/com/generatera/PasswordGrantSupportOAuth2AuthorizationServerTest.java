package com.generatera;

import com.fasterxml.jackson.databind.Module;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.jackson.mixin.AuditDeletedDateMixin;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.jackson.mixin.LongMixin;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.jackson.mixin.UserAuthorityMixin;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.jackson.mixin.UserPrincipalMixin;
import com.generatera.central.oauth2.authorization.server.configuration.model.entity.AuditDeletedDate;
import com.generatera.central.oauth2.authorization.server.configuration.model.entity.UserAuthority;
import com.generatera.central.oauth2.authorization.server.configuration.model.entity.UserPrincipal;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningUserPrincipalConverter;
import com.generatera.model.entity.LightningOAuth2UserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.jianyue.lightning.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

import java.util.List;
/**
 * @author FLJ
 * @date 2023/1/16
 * @time 16:42
 * @Description 同样需要  LightningUserPrincipalConverter 进行 LightningUserPrincipal 处理 ...
 * 修改了 spring-oauth2-authorization-server的默认配置 ...
 */
@SpringBootApplication
public class PasswordGrantSupportOAuth2AuthorizationServerTest {
    public static void main(String[] args) {
        SpringApplication.run(PasswordGrantSupportOAuth2AuthorizationServerTest.class, args);
    }

    @Bean
    public LightningUserPrincipalConverter userPrincipalConverter() {

        return new LightningUserPrincipalConverter() {
            // You will need to write the Mixin for your class so Jackson can marshall it.
            private final JsonUtil jsonUtil;

            {
                ClassLoader classLoader = PasswordGrantSupportOAuth2AuthorizationServerTest.class.getClassLoader();
                List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
                jsonUtil = JsonUtil.of()
                        .registerModules(securityModules)
                        .registerModule(new OAuth2AuthorizationServerJackson2Module())
                        .configureObjectMapper(objectMapper -> {
                            objectMapper.addMixIn(UserAuthority.class, UserAuthorityMixin.class);
                            objectMapper.addMixIn(UserPrincipal.class, UserPrincipalMixin.class);
                            objectMapper.addMixIn(AuditDeletedDate.class, AuditDeletedDateMixin.class);
                            objectMapper.addMixIn(Long.class, LongMixin.class);
                        });

            }

            @NotNull
            @Override
            public  LightningUserPrincipal convert(@NotNull Object value) {
                return jsonUtil.fromJson(value.toString(), LightningOAuth2UserDetails.class);
            }

            @Override
            public  Object serialize(LightningUserPrincipal userPrincipal) {
                return jsonUtil.asJSON(userPrincipal);
            }
        };
    }
}