package com.generatera.app.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author FLJ
 * @date 2022/12/27
 * @time 11:13
 * @Description app 授权应用服务器,本身作为 授权服务器的资源服务器 消费授权服务器的派发的token ..
 *
 * 它需要作为 资源服务器 ...
 */
@SpringBootApplication
public class LightningAppAuthorizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(LightningAppAuthorizationApplication.class,args);
    }
}
