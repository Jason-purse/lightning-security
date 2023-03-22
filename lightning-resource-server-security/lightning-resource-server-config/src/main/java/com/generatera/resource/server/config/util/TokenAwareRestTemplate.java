package com.generatera.resource.server.config.util;

import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import com.jianyue.lightning.boot.starter.util.StreamUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.jianyue.lightning.boot.starter.util.SwitchUtil.switchMapFuncForTrue;

/**
 * @author FLJ
 * @date 2023/3/22
 * @time 9:05
 * @Description 感知token的 Template ..
 * <p>
 * 能够直接无感知token 请求对应的服务 ...
 */
public class TokenAwareRestTemplate {

    private static final String Authorization = "Authorization";

    private final RestTemplate restTemplate;

    public TokenAwareRestTemplate(@NotNull List<HttpMessageConverter<?>> messageConverters) {
        this.restTemplate = new RestTemplate(messageConverters);
        init();
    }

    public TokenAwareRestTemplate() {
        this.restTemplate = new RestTemplate();
        init();
    }

    private void init() {
        // 支持 APPLICATION_FORM_URLENCODED
        restTemplate.getMessageConverters().add(
                new HttpMessageConverter<Map<String, String>>() {
                    @Override
                    public boolean canRead(@NotNull Class<?> clazz, MediaType mediaType) {
                        return MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType);
                    }

                    @Override
                    public boolean canWrite(@NotNull Class<?> clazz, MediaType mediaType) {
                        return MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType);
                    }

                    @NotNull
                    @Override
                    public List<MediaType> getSupportedMediaTypes() {
                        return List.of(MediaType.APPLICATION_FORM_URLENCODED);
                    }

                    @NotNull
                    @Override
                    public Map<String, String> read(@NotNull Class<? extends Map<String, String>> clazz, @NotNull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                        InputStream body = inputMessage.getBody();
                        byte[] bytes = body.readAllBytes();
                        String value = new String(bytes);
                        if (StringUtils.hasText(value)) {
                            MultiValueMap<String, String> queryParams = UriComponentsBuilder.newInstance()
                                    .query(value)
                                    .build()
                                    .getQueryParams();

                            // 将它进行转换 ...
                            return JsonUtil.getDefaultJsonUtil().convertTo(to(queryParams), clazz);
                        }
                        // 空map
                        return Collections.emptyMap();
                    }

                    @Override
                    public void write(@NotNull Map<String, String> s, MediaType contentType, @NotNull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                        if (!ObjectUtils.isEmpty(s)) {
                            String json = JsonUtil.getDefaultJsonUtil().asJSON(s);
                            outputMessage.getBody().write(json.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
        );
    }

    /**
     * 修改rest template
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public <T> Result<T> getForResult(String url, @Nullable Map<String, String> params, @Nullable Map<String, String> headers, Class<T> clazz) {
        RequestEntity<Void> entity
                = RequestEntity.get(URI.create(url))
                .headers(new HttpHeaders(of(params)))
                .build();
        ResponseEntity<Result<T>> exchange = restTemplate.exchange(create(url, params), HttpMethod.GET, entity,
                ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(Result.getDefaultImplementClass(), clazz).getType())
        );

        return getResult(exchange);
    }

    public <T> Result<T> getForResult(String url, Class<T> clazz) {
        return getForResult(url, null, null, clazz);
    }

    public <T> Result<T> getForResult(String url, @NotNull Map<String, String> params, Class<T> clazz) {
        return getForResult(url, params, null, clazz);
    }


    @Nullable
    private <T> Result<T> getResult(ResponseEntity<Result<T>> exchange) {
        if (exchange.getStatusCode().is2xxSuccessful()) {
            return exchange.getBody();
        }

        throw new IllegalStateException("invoke request failure,because reason: " + exchange);
    }


    public <T> Result<T> postForResult(String url, @Nullable Map<String, String> params, @Nullable Map<String, String> headers, Class<T> clazz) {

        return postForResult(url, params, headers, MediaType.APPLICATION_FORM_URLENCODED, clazz);
    }


    public <T> Result<T> postForResult(String url, @NotNull Map<String, String> params, Class<T> clazz) {
        return postForResult(url, params, null, clazz);
    }

    public <T> Result<T> postForResult(String url,Class<T> clazz) {
        return postForResult(url,null,null,clazz);
    }

    public <T> Result<T> postJsonForResult(String url, @Nullable Map<String, String> params, @Nullable Map<String, String> headers, Class<T> clazz) {
        return postForResult(url, params, headers, MediaType.APPLICATION_JSON, clazz);
    }

    public <T> Result<T> postJsonForResult(String url,Class<T> clazz) {
        return postJsonForResult(url,null,null,clazz);
    }

    public <T> Result<T> postJsonForResult(String url, @NotNull Map<String, String> formData, Class<T> clazz) {
        return postForResult(url, formData, null, MediaType.APPLICATION_JSON, clazz);
    }


    public <T> Result<T> postForResult(String url, @Nullable Map<String, String> params, @Nullable Map<String, String> headers, @NotNull MediaType mediaType, Class<T> clazz) {

        RequestEntity.BodyBuilder post = RequestEntity.post(URI.create(url))
                .header(HttpHeaders.CONTENT_TYPE, mediaType.toString());
        RequestEntity<?> entity;
        if (params != null) {
            entity = post
                    .headers(new HttpHeaders(of(headers)))
                    .body(params, ResolvableType.forClassWithGenerics(Map.class, String.class, String.class).getType());
        } else {
            entity = post
                    .headers(new HttpHeaders(of(headers)))
                    .build();
        }

        ResponseEntity<Result<T>> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(Result.getDefaultImplementClass(), clazz).getType()));
        return getResult(exchange);
    }




    public <T> Result<T> putForResult(String url, Object json, @Nullable Map<String, String> headers, Class<T> clazz) {
        RequestEntity<String> body = RequestEntity.put(URI.create(url))
                .headers(new HttpHeaders(of(headers)))
                .body(JsonUtil.getDefaultJsonUtil().asJSON(json));

        ResponseEntity<Result<T>> exchange = restTemplate.exchange(url, HttpMethod.PUT, body, ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(Result.getDefaultImplementClass(), clazz).getType()));
        return getResult(exchange);
    }

    public <T> Result<T> putForResult(String url, Object json, Class<T> clazz) {
        return putForResult(url, json, null, clazz);
    }



    public <T> Result<T> deleteForResult(String url, @Nullable Map<String, String> params, @Nullable Map<String, String> headers, Class<T> clazz) {
        RequestEntity<Void> entity = RequestEntity.delete(URI.create(url))
                .headers(new HttpHeaders(of(headers)))
                .build();

        ResponseEntity<Result<T>> exchange = restTemplate.exchange(create(url, params), HttpMethod.DELETE, entity, ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(Result.getDefaultImplementClass(), clazz).getType()));
        return getResult(exchange);
    }

    public <T> Result<T> deleteForResult(String url, Class<T> clazz) {
        return deleteForResult(url, null, null, clazz);
    }

    public <T> Result<T> deleteForResult(String url, @NotNull Map<String, String> params, Class<T> clazz) {
        return deleteForResult(url, params, null, clazz);
    }


    private MultiValueMap<String, String> of(Map<String, String> params) {

        // token 值
        String tokenValue = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getHeader(Authorization);

        return OptionalFlux.of(params)
                .map(Map::entrySet)
                .map(StreamUtil.setToMap(Map.Entry::getKey, value -> List.of(value.getValue())))
                .map(LinkedMultiValueMap::new)
                // 否则 设置一个空map
                .orElse(new LinkedMultiValueMap<>())
                .consume(map -> {
                    if (StringUtils.hasText(tokenValue)) {
                        map.put(Authorization, List.of(tokenValue));
                    }
                })
                .getResult();
    }

    private Map<String, String> to(MultiValueMap<String, String> params) {
        return OptionalFlux.of(params)
                .map(Map::entrySet)
                .map(
                        StreamUtil.setToMap(
                                Map.Entry::getKey,
                                value ->
                                        OptionalFlux.collection(value.getValue())
                                                .map(switchMapFuncForTrue(CollectionUtils::isNotEmpty, list -> list.iterator().next()))
                                                .getResult()
                        )
                )
                .orElse(Collections.emptyMap())
                .getResult();
    }

    private URI create(String url, Map<String, String> params) {
        return UriComponentsBuilder.fromUriString(url)
                .queryParams(of(params))
                .encode()
                .build()
                .toUri();
    }

}
