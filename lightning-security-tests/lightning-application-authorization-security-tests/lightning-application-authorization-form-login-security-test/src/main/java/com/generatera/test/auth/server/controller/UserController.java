package com.generatera.test.auth.server.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserController {

    /**
     * {@link com.jianyue.lightning.boot.starter.generic.crud.service.config.GenericCRUDModelAttributeMethodProcessor 导致 这个注解的真正处理器无法处理}
     * @param securityContext
     * @return
     */
    //@GetMapping("current/user")
    //public Object currentUser(@CurrentSecurityContext SecurityContext securityContext) {
    //    Authentication authentication = securityContext.getAuthentication();
    //    return authentication.getPrincipal();
    //}

    @GetMapping("current/user")
    public Object currentUser(@AuthenticationPrincipal Object authentication) {
        SecurityContext context = SecurityContextHolder.getContext();
        return context.getAuthentication().getPrincipal();
    }
}
