package com.generatera.form.login.resource.test;

import com.generatera.oauth2.resource.server.config.token.LightningJwtGrantAuthorityMapper;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class FormLoginResourceServerTest {

    public static void main(String[] args) {
        SpringApplication.run(FormLoginResourceServerTest.class, args);
    }

    /**
     * todo 应该 基于授权服务器给出提示,进行进一步配置 ...
     * @return
     */
    @Bean
    public LightningJwtGrantAuthorityMapper jwtListConverter() {
        return new LightningJwtGrantAuthorityMapper() {
            @Override
            public Collection<GrantedAuthority> convert(LightningJwt source) {
                List<String> authorities = source.getClaimAsStringList("authorities");
                return authorities != null ?
                        authorities.stream().map(ele -> {
                            return (GrantedAuthority) new SimpleGrantedAuthority(ele);
                        }).toList()
                        : Collections.emptyList();
            }
        };
    }
}
