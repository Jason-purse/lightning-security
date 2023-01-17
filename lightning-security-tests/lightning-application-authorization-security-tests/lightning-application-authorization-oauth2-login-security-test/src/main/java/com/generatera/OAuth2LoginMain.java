package com.generatera;

import com.generatera.authorization.application.server.config.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
public class OAuth2LoginMain {
    public static void main(String[] args) {
        SpringApplication.run(OAuth2LoginMain.class, args);
    }

    /**
     * 实现 userPrincipal 的序列化和反序列化动作 ...
     * @return
     */
    @Bean
    public LightningUserPrincipalConverter userPrincipalConverter() {

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        class SimpleUserPrincipal {

            private String userName;

            private String password;

            private List<String> authorities;
        }

        return new LightningUserPrincipalConverter() {
            @NotNull
            @Override
            public LightningUserPrincipal convert(@NotNull Object value) {
                SimpleUserPrincipal principal = (SimpleUserPrincipal) value;

                List<GrantedAuthority> authorityList = new LinkedList<>();
                if (principal.authorities != null) {
                    for (String authority : principal.authorities) {
                        authorityList.add(new SimpleGrantedAuthority(authority));
                    }
                }

                return new DefaultLightningUserDetails(
                        User.withUsername(principal.userName)
                                .password(principal.password)
                                .authorities(authorityList)
                                .build()
                );
            }

            @Override
            public Object serialize(LightningUserPrincipal userPrincipal) {

                // 例如这里,你必然知道存在 oidc token 令牌 ...
                // 如果你想保留,则自己写这个序列化动作 ..


                return new SimpleUserPrincipal(userPrincipal.getName(), userPrincipal.getPassword(), userPrincipal.getAuthorities() != null ?
                        userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList() :
                        Collections.emptyList());
            }
        };
    }
}