package com.generatera.test.auth.server.config.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {

    @GetMapping
    public Object getCurrentUser(@CurrentSecurityContext SecurityContext securityContext) {
        Authentication authentication = securityContext.getAuthentication();
        return authentication;
    }
}
