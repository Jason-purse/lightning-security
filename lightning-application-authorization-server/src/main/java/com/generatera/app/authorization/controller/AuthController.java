package com.generatera.app.authorization.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


/**
 * 授权执行token的颁发 ..
 * 1. 授权码授予
 */
@RestController
@RequestMapping("/auth/v1/login/oauth2")
public class AuthController {


    @PostMapping("client/resource/owner")
    public void loginByClient(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response
    ) {
        // 模拟授权请求 ...


    }
}
