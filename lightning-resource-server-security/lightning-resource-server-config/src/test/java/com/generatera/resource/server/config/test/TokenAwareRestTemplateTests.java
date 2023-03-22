package com.generatera.resource.server.config.test;

import com.generatera.resource.server.config.util.TokenAwareRestTemplate;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;

import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class TokenAwareRestTemplateTests {

    @Test
    public void test() {

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        TokenAwareRestTemplate tokenAwareRestTemplate = new TokenAwareRestTemplate();
        RestTemplate restTemplate = tokenAwareRestTemplate.getRestTemplate();
        // 需要支持 ..
        restTemplate.getMessageConverters().add(
                new MappingJackson2HttpMessageConverter()
        );
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate)
                .ignoreExpectOrder(true).build();

        mockServer.expect(times(1), requestTo("/post_encode_url")).andRespond(withSuccess(
                JsonUtil.getDefaultJsonUtil().asJSON(new MyData("zs", "lis4")),
                MediaType.APPLICATION_JSON
        ));


        mockServer.expect(times(1), requestTo("/post_json_empty"))
                .andRespond(withSuccess(JsonUtil.getDefaultJsonUtil().asJSON(Result.success(null)), MediaType.APPLICATION_JSON));

        mockServer.expect(times(1), request -> {
                    System.out.println(((MockClientHttpRequest) request).getBodyAsString());
                    AssertionErrors.assertTrue("requestUrl", request.getURI().toString().equals("/post_json"));
                })
                .andRespond(withSuccess(
                        JsonUtil.getDefaultJsonUtil().asJSON(Result.success(new MyData("zs", "lis4"))),
                        MediaType.APPLICATION_JSON
                ));

        mockServer.expect(times(1), requestTo("/post_encode_url_empty"))
                .andRespond(withSuccess(JsonUtil.getDefaultJsonUtil().asJSON(Result.success(null)), MediaType.APPLICATION_JSON));


        mockServer.expect(times(1), requestTo("/get"))
                .andRespond(withSuccess(
                        JsonUtil.getDefaultJsonUtil().asJSON(Result.success(new MyData("zs", "lis4"))),
                        MediaType.APPLICATION_JSON
                ));


        mockServer.expect(times(1), requestTo("/get_empty"))
                .andRespond(withSuccess(JsonUtil.getDefaultJsonUtil().asJSON(Result.success(null)), MediaType.APPLICATION_JSON));


        Result<Void> forResult = tokenAwareRestTemplate.getForResult("/get_empty", Void.class);
        Assertions.assertNull(forResult.getResult());
        Assertions.assertEquals(forResult.getCode(), 200);

        Result<MyData> forResult1 = tokenAwareRestTemplate.getForResult("/get", MyData.class);
        Assertions.assertNotNull(forResult1.getResult());
        Assertions.assertEquals(new MyData("zs", "lis4"), forResult1.getResult());


        Result<Void> post_encode_url_empty = tokenAwareRestTemplate.postForResult("post_encode_url_empty", Collections.emptyMap(), Void.class);
        Assertions.assertNull(post_encode_url_empty.getResult());
        Assertions.assertEquals(post_encode_url_empty.getCode(), 200);


        Result<Void> result = tokenAwareRestTemplate.postJsonForResult("post_json_empty", Map.of("username", "123123"), Void.class);
        Assertions.assertNull(result.getResult());
        Assertions.assertEquals(result.getCode(), 200);
        Result<MyData> result1 = tokenAwareRestTemplate.postJsonForResult("/post_json", Map.of("username", "123123"), MyData.class);
        Assertions.assertNotNull(result1.getResult());
        Assertions.assertEquals(result1.getCode(), 200);

    }


    @Test
    public void errorTest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        TokenAwareRestTemplate tokenAwareRestTemplate = new TokenAwareRestTemplate();
        RestTemplate restTemplate = tokenAwareRestTemplate.getRestTemplate();
        // 需要支持 ..
        restTemplate.getMessageConverters().add(
                new MappingJackson2HttpMessageConverter()
        );
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate)
                .ignoreExpectOrder(true).build();

        Result<String> stringResult = tokenAwareRestTemplate.postForResult("/api", null, String.class);
        Assertions.assertNotNull(stringResult);
        Assertions.assertNotNull(stringResult.getResult());
        Assertions.assertEquals(200,stringResult.getResult());

    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    @NoArgsConstructor
    static class MyData {

        private String username;

        private String password;


    }
}
