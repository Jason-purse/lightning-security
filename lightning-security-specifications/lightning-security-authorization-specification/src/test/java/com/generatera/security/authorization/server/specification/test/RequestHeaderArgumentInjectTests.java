package com.generatera.security.authorization.server.specification.test;

import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderArgument;
import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderHandlerMethodArgumentEnhancer;
import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderInject;
import com.jianyue.lightning.framework.web.method.argument.context.MethodArgumentContext;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;

/**
 * @author FLJ
 * @date 2023/3/3
 * @time 13:51
 * @Description 请求头参数
 */
@SpringJUnitWebConfig
public class RequestHeaderArgumentInjectTests {

    private WebApplicationContext webApplicationContext;
    private  MockMvc mockMvc;

    @Configuration
    public static class Config {

        @Bean
        public RequestHeaderHandlerMethodArgumentEnhancer requestHeaderHandlerMethodArgumentResolver() {
            return new RequestHeaderHandlerMethodArgumentEnhancer();
        }

    }
    @BeforeTestMethod()
    public  void beforeAll() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Autowired
    public RequestHeaderHandlerMethodArgumentEnhancer requestHeaderHandlerMethodArgumentResolver;

    public void test(@RequestHeaderArgument("clientId") String value) {

    }

    public void test2(MyData myData) {

    }

    @Test
    public void requestHeaderArgumentTest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/api/v1/test");
        request.addHeader("clientId","invalid clientId");
        request.addParameter("string","1020");

        LightningUserContext.set(new LightningUserPrincipalTests.MyLightningUserPrincipal("zs","123456", Collections.emptyList(),true));

        MethodArgumentContext test = new MethodArgumentContext(
                MethodParameter.forExecutable(this.getClass().getMethod("test", String.class), 0),
                new ModelAndViewContainer(),
                new ServletWebRequest(request),
                new DefaultDataBinderFactory(null),
                null
        );
        requestHeaderHandlerMethodArgumentResolver.enhanceArgument(
               test
        );

        System.out.println(test.getTarget());

        test.setMethodParameter(
                MethodParameter.forExecutable(this.getClass().getMethod("test2", MyData.class),0)
        );
        test.setTarget(new MyData());

        requestHeaderHandlerMethodArgumentResolver.enhanceArgument(test);

        System.out.println(test.getTarget());
    }
    @Data
    @RequestHeaderInject
    public static class MyData {

        @RequestHeaderArgument("clientId")
        private String clientId;
    }
}
