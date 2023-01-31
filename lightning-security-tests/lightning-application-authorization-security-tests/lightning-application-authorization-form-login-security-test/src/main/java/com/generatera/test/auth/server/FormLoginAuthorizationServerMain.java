package com.generatera.test.auth.server;

import com.generatera.security.authorization.server.specification.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.jianyue.lightning.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class FormLoginAuthorizationServerMain {
    public static void main(String[] args) {
        SpringApplication.run(FormLoginAuthorizationServerMain.class,args);
    }


    @Bean
    public LightningUserPrincipalConverter forDbUserPrincipalConverter() {
        return new LightningUserPrincipalConverter() {
            @NotNull
            @Override
            public LightningUserPrincipal convert(@NotNull Object value) {
                Map<String, Object> valueMap = ((Map<String,Object>) value);

                Object authorities = valueMap.get("authorities");
                Collection<GrantedAuthority> grantedAuthorities = Collections.emptyList();
                if(authorities instanceof String) {
                    String[] values = ((String) authorities).split("");
                    grantedAuthorities = Arrays.stream(values).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                }
                else {
                    Collection<String> authorities1 = (Collection<String>) authorities;
                    grantedAuthorities = new LinkedList<>();
                    for (String s : authorities1) {
                        grantedAuthorities.add(new SimpleGrantedAuthority(s));
                    }
                }


                return new DefaultLightningUserDetails(
                        new User(valueMap.get("username").toString(),
                                "",grantedAuthorities)
                );
            }

            @Override
            public Object serialize(LightningUserPrincipal userPrincipal) {
                User user = new User(userPrincipal.getName(), "", userPrincipal.getAuthorities());
                Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
                Collection<String> authoritiesString = Collections.emptyList();
                if(authorities != null && !authorities.isEmpty()) {
                    authoritiesString = authorities.stream().map(GrantedAuthority::getAuthority).toList();
                }
                Collection<String> finalAuthoritiesString = authoritiesString;
                return JsonUtil.getDefaultJsonUtil().asJSON(
                        new LinkedHashMap<>() {{
                            put("username",userPrincipal.getUsername());
                            put("authorities", finalAuthoritiesString);
                        }}
                );
            }
        };
    }
}