package com.generatera.security.authorization.server.specification.test;

import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderArgument;
import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderHandlerMethodArgumentResolver;
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
        public RequestHeaderHandlerMethodArgumentResolver requestHeaderHandlerMethodArgumentResolver() {
            return new RequestHeaderHandlerMethodArgumentResolver();
        }

    }
    @BeforeTestMethod()
    public  void beforeAll() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Autowired
    public RequestHeaderHandlerMethodArgumentResolver requestHeaderHandlerMethodArgumentResolver;

    public void test(@RequestHeaderArgument("clientId") String value) {

    }

    @Test
    public void requestHeaderArgumentTest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/api/v1/test");
        //request.addHeader("clientId","invalid clientId");
        request.addParameter("string","1020");

        Object test = requestHeaderHandlerMethodArgumentResolver.resolveArgument(
                MethodParameter.forExecutable(this.getClass().getMethod("test", String.class), 0),
                new ModelAndViewContainer(),
                new ServletWebRequest(request),
                new DefaultDataBinderFactory(null)
        );

        System.out.println(test);
    }
}
