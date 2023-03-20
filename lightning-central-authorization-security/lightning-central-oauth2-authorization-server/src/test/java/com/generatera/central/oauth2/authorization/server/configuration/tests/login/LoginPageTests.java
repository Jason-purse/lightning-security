package com.generatera.central.oauth2.authorization.server.configuration.tests.login;

import com.generatera.central.oauth2.authorization.server.configuration.OAuth2CentralAuthorizationServerConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;
import org.springframework.web.context.WebApplicationContext;

/**
 * 中央授权服务器,进行处理 ...
 *
 *  所需要的无非就是,提供者配置 ...
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringJUnitWebConfig
public class LoginPageTests {

    /**
     * 引入此配置 ...
     */
    @Import(OAuth2CentralAuthorizationServerConfiguration.class)
    @Configuration
    public static class CentralServerConfiguration {



    }

    private HtmlUnitDriver driver;

    @BeforeAll
    public void beforeConfig(WebApplicationContext applicationContext) {
        driver = MockMvcHtmlUnitDriverBuilder.webAppContextSetup(applicationContext).build();
    }


    @Test
    public void loginPageTest() {

    }
}
