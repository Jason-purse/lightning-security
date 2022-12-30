package com.generatera.authorization.server.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.generatera.authorization.server.configure.store.authorizationinfo.OAuth2AuthorizationEntity;
import com.jianyue.lightning.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;

public class jacksonTests {
    @Test
    public void test() throws JsonProcessingException {

        String json = "{\"id\":\"913a0d78-ced1-456e-b6a7-f99ad4b288d5\",\"registeredClientId\":\"4\",\"principalName\":\"user1\",\"authorizationGrantType\":{\"value\":\"password\"},\"accessToken\":{\"token\":{\"tokenValue\":\"eyJraWQiOiI3ZmZhNjFlZC00ZmQwLTQwNDQtOWQ5Zi1hN2RjYmVmN2M2MzYiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImF1ZCI6InBhc3N3b3JkLWNsaWVudC1pZCIsIm5iZiI6MTY3MjM3MTU4NCwic2NvcGUiOlsibWVzc2FnZV93cml0ZSIsIm1lc3NhZ2VfcmVhZCJdLCJpc3MiOiJodHRwOlwvXC9sb2NhbGhvc3Q6OTAwMCIsImV4cCI6MTY3MjM3MzM4NCwiaWF0IjoxNjcyMzcxNTg0LCJ1c2VySWQiOjEsImF1dGhvcml0aWVzIjpbIkFETUlOIiwiVVNFUiJdfQ.oOPON_h1raD28MvqBSSMKifU0cECevvL02rFnVpE3xJNBNGzag9OkcuFYGAohlUp2SQOhtqlzVNYpyjGpoxC0xD2c-9fn8U12f63g-i1Nup1sZtmYtdb7WX2g8k4cmpHHdgBPRMIS5rV0lXMPcBYD4BJzBhxmh4ktgcpaFpRCL1z5yODm8FLmMN6EDaCr7WxZTxXdUOrQqjAsc7f2QriyFtm-TcPgB8jzUQLx66bHdrjqSESUH6xIYSaxKqroHZWHUM5rmxHxPpoMJKYuKud7NyEZe73a_8zDucPU2KaTrNvAb69nKHclqpc380m_gVIFEjJyr2jUcPmYwCAl2Uc6g\",\"issuedAt\":1672371584691,\"expiresAt\":1672373384691,\"tokenType\":{\"value\":\"Bearer\"},\"scopes\":[\"message_write\",\"message_read\"]},\"metadata\":{\"metadata.token.claims\":{\"sub\":\"user1\",\"aud\":[\"password-client-id\"],\"nbf\":1672371584691,\"scope\":[\"message_write\",\"message_read\"],\"iss\":\"http://localhost:9000\",\"exp\":1672373384691,\"iat\":1672371584691,\"userId\":1,\"authorities\":[\"ADMIN\",\"USER\"]},\"metadata.token.invalidated\":false},\"active\":true,\"claims\":{\"sub\":\"user1\",\"aud\":[\"password-client-id\"],\"nbf\":1672371584691,\"scope\":[\"message_write\",\"message_read\"],\"iss\":\"http://localhost:9000\",\"exp\":1672373384691,\"iat\":1672371584691,\"userId\":1,\"authorities\":[\"ADMIN\",\"USER\"]},\"expired\":false,\"invalidated\":false,\"beforeUse\":false},\"refreshToken\":{\"token\":{\"tokenValue\":\"bK2HDv51v9kVJ1dd013r_766dWo-sG1Ldamp05WTw1vl3IHVp7Fnqzi7-eCSL1DeoMpCHSHV_Cb8u5RWadBqLSheuq3xayJpLbpvZ5ylJ82wHnlc8DZqTMwSuIaVTW6l\",\"issuedAt\":1672371584726,\"expiresAt\":1672457984726},\"metadata\":{\"metadata.token.invalidated\":false},\"active\":true,\"expired\":false,\"invalidated\":false,\"beforeUse\":false},\"attributes\":{\"java.security.Principal\":{\"authorities\":[{\"authority\":\"ADMIN\"},{\"authority\":\"USER\"}],\"authenticated\":true,\"principal\":{\"id\":1,\"username\":\"user1\",\"enabled\":true,\"accountNonExpired\":true,\"credentialsNonExpired\":true,\"accountNonLocked\":true,\"authorities\":[{\"authority\":\"ADMIN\"},{\"authority\":\"USER\"}],\"audit\":{\"createdBy\":0,\"createdDate\":1670845320000,\"lastModifiedBy\":0,\"lastModifiedDate\":1670845320000}},\"name\":\"user1\"}}}";
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JSR310Module());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,true);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,false);

        OAuth2AuthorizationEntity oAuth2AuthorizationEntity = objectMapper.readValue(json, OAuth2AuthorizationEntity.class);

        System.out.println(oAuth2AuthorizationEntity);
    }

    @Test
    public void instantTest() throws JsonProcessingException {
        Instant now = Instant.now();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        simpleModule.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,true);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,false);
        objectMapper.registerModule(simpleModule);

        // 不应该直接对objectMapper 直接设置 dateformat
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        System.out.println(objectMapper.writeValueAsString(now));

        System.out.println(objectMapper.writeValueAsString(LocalDateTime.now()));
    }

    @Test
    public void instant2Tests() {
        System.out.println(JsonUtil.getDefaultJsonUtil().asJSON(Instant.now()));
        System.out.println(JsonUtil.getDefaultJsonUtil().asJSON(LocalDateTime.now()));

    }
}
