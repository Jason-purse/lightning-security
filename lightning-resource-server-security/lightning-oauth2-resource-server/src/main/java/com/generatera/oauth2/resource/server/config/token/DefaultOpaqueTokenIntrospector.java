package com.generatera.oauth2.resource.server.config.token;

import com.generatera.oauth2.resource.server.config.LightningOpaqueOAuth2UserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.*;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 15:56
 * @Description 自省token 检查器 ...
 * <p>
 * 支持 client_secret_basic 以及 client_secret_post 客户端校验方式 ..
 *
 * 并且默认丢弃 scopes(使用authorities 作为它的 替代品) ...
 * 一般来说默认用不上 ....
 *
 * 你可以选择覆盖 DefaultOpaqueTokenIntrospector 继承实现,或者使用 JwtClaimsToUserPrincipalMapper 进行 {@link LightningUserPrincipal}
 * 自定义 ...
 *
 * @see DefaultOpaqueTokenIntrospector#convertClaimsSet
 * @see JwtClaimsToUserPrincipalMapper
 */
public class DefaultOpaqueTokenIntrospector implements LightningOAuth2OpaqueTokenIntrospector {
    private  String authorityPrefix = "SCOPE_";
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private final Log logger = LogFactory.getLog(this.getClass());
    private final RestOperations restOperations;
    private Converter<String, RequestEntity<?>> requestEntityConverter;

    private List<String> authoritiesName = Arrays.asList(JwtExtClaimNames.SCOPE_CLAIM,JwtExtClaimNames.SCOPE_SHORT_CLAIM);

    /**
     * 用来将 jwt claims 转换为 userPrincipal的 映射器 ..
     */
    private JwtClaimsToUserPrincipalMapper jwtClaimsToUserPrincipalMapper;

    public DefaultOpaqueTokenIntrospector(String introspectionUri, String clientId, String clientSecret) {
        Assert.notNull(introspectionUri, "introspectionUri cannot be null");
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(clientSecret, "clientSecret cannot be null");
        this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
        this.restOperations = restTemplate;
    }


    public DefaultOpaqueTokenIntrospector(String introspectionUri, RestOperations restOperations) {
        Assert.notNull(introspectionUri, "introspectionUri cannot be null");
        Assert.notNull(restOperations, "restOperations cannot be null");
        this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
        this.restOperations = restOperations;
    }

    public void setAuthorityPrefix(String authorityPrefix) {
        Assert.hasText(authorityPrefix,"authorityPrefix must not be null !!!");
        this.authorityPrefix = authorityPrefix;
    }

    public void setAuthoritiesName(List<String> authoritiesName) {
        Assert.notEmpty(authoritiesName,"authoritiesName must not be null !!!");
        this.authoritiesName = authoritiesName;
    }

    public void setJwtClaimsToUserPrincipalMapper(JwtClaimsToUserPrincipalMapper jwtClaimsToUserPrincipalMapper) {
        Assert.notNull(jwtClaimsToUserPrincipalMapper,"jwtClaimsToUserPrincipalMapper must not be null !!!");
        this.jwtClaimsToUserPrincipalMapper = jwtClaimsToUserPrincipalMapper;
    }

    public static DefaultOpaqueTokenIntrospector clientSecretPostOf(String introspectionUri, String clientId, String clientSecret) {
        UriComponents url = UriComponentsBuilder.fromUriString(introspectionUri)
                .queryParam("username", clientId)
                .queryParam("password", clientSecret)
                .build();

        return new DefaultOpaqueTokenIntrospector(url.toUriString(), new RestTemplate());
    }

    public static DefaultOpaqueTokenIntrospector clientSecretBasicOf(String introspectionUri, String clientId, String clientSecret) {
        return new DefaultOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    }

    private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(URI introspectionUri) {
        return (token) -> {
            HttpHeaders headers = this.requestHeaders();
            MultiValueMap<String, String> body = this.requestBody(token);
            return new RequestEntity<>(body, headers, HttpMethod.POST, introspectionUri);
        };
    }

    private HttpHeaders requestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private MultiValueMap<String, String> requestBody(String token) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", token);
        return body;
    }

    @Override
    public LightningUserPrincipal introspect(String token) {
        RequestEntity<?> requestEntity = this.requestEntityConverter.convert(token);
        if (requestEntity == null) {
            throw new OAuth2IntrospectionException("requestEntityConverter returned a null entity");
        } else {
            ResponseEntity<Map<String, Object>> responseEntity = this.makeRequest(requestEntity);
            Map<String, Object> claims = this.adaptToNimbusResponse(responseEntity);

            // 由于是 JWT ,所以默认就是 jwt 包装一下
            return this.convertClaimsSet(claims,token);
        }
    }

    public void setRequestEntityConverter(Converter<String, RequestEntity<?>> requestEntityConverter) {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }

    private ResponseEntity<Map<String, Object>> makeRequest(RequestEntity<?> requestEntity) {
        try {
            return this.restOperations.exchange(requestEntity, STRING_OBJECT_MAP);
        } catch (Exception var3) {
            throw new OAuth2IntrospectionException(var3.getMessage(), var3);
        }
    }

    private Map<String, Object> adaptToNimbusResponse(ResponseEntity<Map<String, Object>> responseEntity) {
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new OAuth2IntrospectionException("Introspection endpoint responded with " + responseEntity.getStatusCode());
        } else {
            Map<String, Object> claims = responseEntity.getBody();
            if (claims == null) {
                return Collections.emptyMap();
            } else {
                boolean active = (Boolean) claims.compute("active", (k, v) -> {
                    if (v instanceof String) {
                        return Boolean.parseBoolean((String) v);
                    } else {
                        return v instanceof Boolean ? v : false;
                    }
                });
                if (!active) {
                    this.logger.trace("Did not validate token since it is inactive");
                    throw new BadOpaqueTokenException("Provided token isn't active");
                } else {
                    return claims;
                }
            }
        }
    }

    protected LightningUserPrincipal convertClaimsSet(Map<String, Object> claims, String token) {
        claims.computeIfPresent("aud", (k, v) -> {
            return v instanceof String ? Collections.singletonList(v) : v;
        });
        claims.computeIfPresent("client_id", (k, v) -> {
            return v.toString();
        });
        claims.computeIfPresent("exp", (k, v) -> {
            return Instant.ofEpochSecond(((Number) v).longValue());
        });
        claims.computeIfPresent("iat", (k, v) -> {
            return Instant.ofEpochSecond(((Number) v).longValue());
        });
        claims.computeIfPresent("iss", (k, v) -> {
            return v.toString();
        });
        claims.computeIfPresent("nbf", (k, v) -> Instant.ofEpochSecond(((Number) v).longValue()));
        Collection<GrantedAuthority> authorities = new LinkedList<>();
        for (String authorityName : authoritiesName) {
            claims.computeIfPresent(authorityName, (k, v) -> {
                if (!(v instanceof String)) {
                    return v;
                } else {
                    Collection<String> scopes = Arrays.asList(((String)v).split(" "));

                    for (String scope : scopes) {
                        authorities.add(new SimpleGrantedAuthority(authorityPrefix + scope));
                    }

                    return scopes;
                }
            });
        }


        if(jwtClaimsToUserPrincipalMapper != null) {
            return jwtClaimsToUserPrincipalMapper.convert(claims);
        }

        for (String authorityName : authoritiesName) {
            claims.remove(authorityName);
        }


        return new LightningOpaqueOAuth2UserPrincipal(
                new OAuth2IntrospectionAuthenticatedPrincipal(
                        claims,authorities)
        );
    }
}
