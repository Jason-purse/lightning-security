package com.generatera.form.login.authorization.and.resource.server;

import com.generatera.authorization.application.server.config.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final static List<User> users = new LinkedList<>();

    static {
        users.add(new User("test","{noop}123456", List.of(
                new SimpleGrantedAuthority("admin")
        )));

    }


    @Override
    public LightningUserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return new DefaultLightningUserDetails(user);
            }
        }

        // 用户名或者密码错误 ...
        throw new BadCredentialsException("用户名或者密码错误...");
    }


}
