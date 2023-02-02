package com.generatera.test.auth.server.controller;

import com.generatera.security.authorization.server.specification.LightningUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public Object currentUser() {
        return LightningUserContext.get()
                .getUserPrincipal()
                .map(Object::toString)
                .orElse("no current user");
    }

    @GetMapping("forward")
    public void forward(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("../login").forward(request,response);
    }
}
