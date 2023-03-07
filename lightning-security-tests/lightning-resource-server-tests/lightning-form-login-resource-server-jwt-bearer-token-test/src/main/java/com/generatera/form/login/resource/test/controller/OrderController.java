package com.generatera.form.login.resource.test.controller;

import com.generatera.resource.server.config.method.security.LightningPreAuthorize;
import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class OrderController {
    /**
     * 查看文档- 仅限fergin调用
     * @return
     */
    @GetMapping
    @LightningPreAuthorize("物流模块-查询文件")
    public LightningUserPrincipal currentUser() {
        return LightningUserContext.get().getUserPrincipal().get();
    }

    @GetMapping("1")
    @LightningPreAuthorize(roles = {"role4"})
    public LightningUserPrincipal currentUser1() {
        return currentUser();
    }

    @GetMapping("2")
    @LightningPreAuthorize(roles = {"role10"})
    public LightningUserPrincipal currentUser2() {
        return currentUser();
    }
}
