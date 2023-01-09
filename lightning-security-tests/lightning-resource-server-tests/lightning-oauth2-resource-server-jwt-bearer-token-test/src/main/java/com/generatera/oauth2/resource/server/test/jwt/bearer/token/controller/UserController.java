package com.generatera.oauth2.resource.server.test.jwt.bearer.token.controller;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserController {

    @GetMapping("api/user")
    public Object currentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        return context.getAuthentication().getPrincipal();
    }
}
