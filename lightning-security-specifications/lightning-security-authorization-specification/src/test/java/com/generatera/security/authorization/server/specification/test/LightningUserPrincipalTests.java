package com.generatera.security.authorization.server.specification.test;

import com.generatera.security.authorization.server.specification.DefaultLightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalInject;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalProperty;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalPropertyHandlerMethodArgumentEnhancer;
import com.jianyue.lightning.boot.autoconfigure.web.WebConfigAutoConfiguration;
import com.jianyue.lightning.framework.web.method.argument.context.MethodArgumentContext;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 测试LightningUserPrincipal {@link com.generatera.security.authorization.server.specification.LightningUserPrincipal}
 * 以及 {@link com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalProperty}
 * 注解的使用 ...
 *
 * 2. {@link UserPrincipalPropertyHandlerMethodArgumentEnhancer}
 * 绑定 {@link com.generatera.security.authorization.server.specification.LightningUserPrincipal} 的属性到 Param类对象上 ...
 */
public class LightningUserPrincipalTests {

    @Test
    public void propertyGet() {
        MyLightningUserPrincipal myLightningUserPrincipal = new MyLightningUserPrincipal(
                "张三",
                "里斯",
                Collections.emptyList(),
                false
        );

        Object username = myLightningUserPrincipal.getProperty("username", String.class);
        System.out.println(username);
        System.out.println(myLightningUserPrincipal.getProperty("password", String.class));
        System.out.println(myLightningUserPrincipal.getProperty("value", String.class));
        System.out.println(myLightningUserPrincipal.getProperty("password", Object.class));
        System.out.println(myLightningUserPrincipal.getProperty("password", Character.class));
        System.out.println(myLightningUserPrincipal.getProperty("password", CharSequence.class));
        System.out.println(myLightningUserPrincipal.getProperty("password", Integer.class));

        System.out.println(myLightningUserPrincipal.getProperty("username", Object.class));
        System.out.println(myLightningUserPrincipal.getProperty("username", Character.class));
        System.out.println(myLightningUserPrincipal.getProperty("username", CharSequence.class));
        System.out.println(myLightningUserPrincipal.getProperty("username", Integer.class));

    }

    static class MyLightningUserPrincipal extends DefaultLightningUserPrincipal {

        public MyLightningUserPrincipal(String username, String password,Collection<? extends GrantedAuthority> authorities,boolean authenticated) {
            super(username,password,
                    authenticated,authenticated,authenticated,authenticated,authorities);
        }

        public String getPassword() {
            return null;
        }


        public String valueS() {
            return "1231";
        }

        public String getValue() {
            return "1234";
        }

    }


    /**
     * 使用ConversionService 进行增强
     */
    @SpringJUnitConfig
    public static class SpringContainerTests {

        @Configuration
        @PropertySource("classpath:application.properties")
        public static class MyConfig {
            @Bean
            public DefaultConversionService conversionService() {
                return new DefaultConversionService();
            }
        }

        @Autowired
        private DefaultConversionService conversionService;

        @Test
        public void test() {

            MyLightningUserPrincipal userPrincipal = new MyLightningUserPrincipal("张三", "12345", null, false);
            System.out.println(userPrincipal.getProperty("password", Integer.class));

            Integer password = conversionService.convert(userPrincipal.getProperty("value"), Integer.class);
            System.out.println(password);
        }
    }


    @SpringJUnitConfig
    public static class SpringContainerForMethodResolveTests {

        @Test
        public void methodResolve() throws Exception {

            SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
            emptyContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                    new MyLightningUserPrincipal("张三","123456",null,true),
                    null,null
            ));

            SecurityContextHolder.setContext(emptyContext);

            UserPrincipalPropertyHandlerMethodArgumentEnhancer lightningUserPrincipalPropertyHandlerMethodArgumentResolver = new UserPrincipalPropertyHandlerMethodArgumentEnhancer();
            MockHttpServletRequest get = new MockHttpServletRequest("get", "/api/get/current/user");

            get.setParameter("password","123456");
            get.setParameter("value","8090");


            MethodParameter parameter = new MethodParameter(
                    SpringContainerForMethodResolveTests.class.getMethod("registerTest", MyData.class), 0
            );
            if (lightningUserPrincipalPropertyHandlerMethodArgumentResolver.supportsParameter(parameter)) {
                MethodArgumentContext methodArgumentContext = new MethodArgumentContext(
                        parameter, new ModelAndViewContainer(),
                        new ServletWebRequest(get), new DefaultDataBinderFactory(null),
                        new MyData()
                );
                lightningUserPrincipalPropertyHandlerMethodArgumentResolver.enhanceArgument(
                     methodArgumentContext
                       );

                System.out.println(methodArgumentContext.getTarget());
            }
        }

        @GetMapping
        public void registerTest(@UserPrincipalInject MyData myData) {

        }
    }


    @SpringJUnitWebConfig
    public static class SpringMvcForMethodResolveTests {
        @Import({WebConfigAutoConfiguration.class,WebMvcAutoConfiguration.class})
        @Configuration
        public static class Config {

            @Bean
            public UserPrincipalPropertyHandlerMethodArgumentEnhancer userPrincipalPropertyHandlerMethodArgumentEnhancer() {
                return new UserPrincipalPropertyHandlerMethodArgumentEnhancer();
            }

            @Bean
            public MyController myController() {
                return new MyController();
            }
        }

        @RestController
        @RequestMapping("/api")
        public static class MyController {
            @GetMapping
            public void registerTest(MyData myData) {
                System.out.println(myData);
            }
        }

        private MockMvc mockMvc;
        @BeforeEach
        public void each(WebApplicationContext context) {
            mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        }


        @Test
        public void test() throws Exception {
            SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
            emptyContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                    new MyLightningUserPrincipal("张三","123456",null,true),
                    null,null
            ));

            SecurityContextHolder.setContext(emptyContext);

            MockHttpServletRequest get = new MockHttpServletRequest("get", "/api");

            get.setParameter("password","123456");
            get.setParameter("value","8090");

                mockMvc.perform(MockMvcRequestBuilders.get("/api"))
                        .andReturn();

        }



    }


    @Test
    public void argumentConvertWithConversionService() {
        DefaultConversionService defaultConversionService = new DefaultConversionService();
        List<String> values = Arrays.asList("1","2");

        Object convert = defaultConversionService.convert(values, TypeDescriptor.valueOf(List.class));

        Assertions.assertNotNull(convert);

        String valuee = "123";

        List convert1 = defaultConversionService.convert(valuee, List.class);
        System.out.println(convert1);

        System.out.println(convert);
    }



    @Data
    @UserPrincipalInject
    static class MyData {

        @UserPrincipalProperty
        private String username;

        private String password;

        @UserPrincipalProperty
        private String value;

        @UserPrincipalInject
        private MyData2 myData2 = new MyData2();


    }

    @Data
    static class MyData2 {

        @UserPrincipalProperty
        private String username;

        private String password;

        @UserPrincipalProperty
        private String value;


        @UserPrincipalProperty("username")
        private List<String> listUsername;
    }




}
