package com.generatera.security.authorization.server.specification.test;

import com.generatera.security.authorization.server.specification.DefaultLightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.annotations.LightningUserPrincipalPropertyHandlerMethodArgumentResolver;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalInject;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalProperty;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collection;
import java.util.Collections;

/**
 * 测试LightningUserPrincipal {@link com.generatera.security.authorization.server.specification.LightningUserPrincipal}
 * 以及 {@link com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalProperty}
 * 注解的使用 ...
 *
 * 2. {@link com.generatera.security.authorization.server.specification.components.annotations.LightningUserPrincipalPropertyHandlerMethodArgumentResolver}
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

            LightningUserPrincipalPropertyHandlerMethodArgumentResolver lightningUserPrincipalPropertyHandlerMethodArgumentResolver = new LightningUserPrincipalPropertyHandlerMethodArgumentResolver();
            MockHttpServletRequest get = new MockHttpServletRequest("get", "/api/get/current/user");

            get.setParameter("password","123456");
            get.setParameter("value","8090");

            MethodParameter parameter = new MethodParameter(
                    SpringContainerForMethodResolveTests.class.getMethod("registerTest", MyData.class), 0
            );
            if (lightningUserPrincipalPropertyHandlerMethodArgumentResolver.supportsParameter(parameter)) {
                Object o = lightningUserPrincipalPropertyHandlerMethodArgumentResolver.resolveArgument(parameter, new ModelAndViewContainer(),
                        new ServletWebRequest(get), new DefaultDataBinderFactory(null));

                System.out.println(o);
            }
        }

        public void registerTest(MyData myData) {

        }
    }



    @Data
    @UserPrincipalInject
    static class MyData {

        @UserPrincipalProperty
        private String username;

        private String password;

        @UserPrincipalProperty
        private String value;

        private MyData2 myData2;
    }

    static class MyData2 {

        @UserPrincipalProperty
        private String username;

        private String password;

        @UserPrincipalProperty
        private String value;
    }
}
