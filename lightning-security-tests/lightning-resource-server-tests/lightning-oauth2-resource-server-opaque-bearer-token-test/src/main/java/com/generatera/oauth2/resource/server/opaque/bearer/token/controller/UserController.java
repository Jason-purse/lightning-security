package com.generatera.oauth2.resource.server.opaque.bearer.token.controller;

import com.generatera.oauth2.resource.server.opaque.bearer.token.SimpleUserPrincipal;
import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping
public class UserController {

    @GetMapping("current/user")
    public LightningUserPrincipal currentUser() throws IllegalAccessException {
        LightningUserContext lightningUserContext = LightningUserContext.get();
        Optional<LightningUserPrincipal> userPrincipal = lightningUserContext.getUserPrincipal();

        userPrincipal.map(ele -> ((SimpleUserPrincipal) ele))
                .ifPresent(ele -> {
                    System.out.println("simpleUserPrincipal info" + ele.toString());
                });

        return userPrincipal.orElseThrow(() -> new IllegalAccessException("无效访问 !!!"));
    }
}
