package com.generatera.form.login.resource.test.controller;

import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class OrderController {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_role1')")
    public LightningUserPrincipal currentUser() {
        return LightningUserContext.get().getUserPrincipal().get();
    }
}
