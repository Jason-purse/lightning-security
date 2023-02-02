package com.generatera.oauth2.resource.server.config.token;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Audience;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.*;
/**
 * @author FLJ
 * @date 2023/1/12
 * @time 14:47
 * @Description Nimbus 本地 opaque token 校验器 ...
 *
 * // TODO: 2023/1/12 直接使用 token 检测端点检测 ...
 */
public class NimbusLocalOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private static final String AUTHORITY_PREFIX = "SCOPE_";
    private final Log logger = LogFactory.getLog(this.getClass());
    private final RestOperations restOperations;
    private Converter<String, RequestEntity<?>> requestEntityConverter;

    public NimbusLocalOpaqueTokenIntrospector(String clientId, String clientSecret) {
        Assert.notNull(clientId, "clientId cannot be null");
        Assert.notNull(clientSecret, "clientSecret cannot be null");
        //this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
        this.restOperations = restTemplate;
    }

    public NimbusLocalOpaqueTokenIntrospector(RestOperations restOperations) {
        Assert.notNull(restOperations, "restOperations cannot be null");
        //this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
        this.restOperations = restOperations;
    }

    private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(URI introspectionUri) {
        return (token) -> {
            HttpHeaders headers = this.requestHeaders();
            MultiValueMap<String, String> body = this.requestBody(token);
            return new RequestEntity(body, headers, HttpMethod.POST, introspectionUri);
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

    public OAuth2AuthenticatedPrincipal introspect(String token) {
        RequestEntity<?> requestEntity = this.requestEntityConverter.convert(token);
        if (requestEntity == null) {
            throw new OAuth2IntrospectionException("requestEntityConverter returned a null entity");
        } else {
            ResponseEntity<String> responseEntity = this.makeRequest(requestEntity);
            HTTPResponse httpResponse = this.adaptToNimbusResponse(responseEntity);
            TokenIntrospectionResponse introspectionResponse = this.parseNimbusResponse(httpResponse);
            TokenIntrospectionSuccessResponse introspectionSuccessResponse = this.castToNimbusSuccess(introspectionResponse);
            if (!introspectionSuccessResponse.isActive()) {
                this.logger.trace("Did not validate token since it is inactive");
                throw new BadOpaqueTokenException("Provided token isn't active");
            } else {
                return this.convertClaimsSet(introspectionSuccessResponse);
            }
        }
    }

    public void setRequestEntityConverter(Converter<String, RequestEntity<?>> requestEntityConverter) {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }

    private ResponseEntity<String> makeRequest(RequestEntity<?> requestEntity) {
        try {
            return this.restOperations.exchange(requestEntity, String.class);
        } catch (Exception var3) {
            throw new OAuth2IntrospectionException(var3.getMessage(), var3);
        }
    }

    private HTTPResponse adaptToNimbusResponse(ResponseEntity<String> responseEntity) {
        MediaType contentType = responseEntity.getHeaders().getContentType();
        if (contentType == null) {
            this.logger.trace("Did not receive Content-Type from introspection endpoint in response");
            throw new OAuth2IntrospectionException("Introspection endpoint response was invalid, as no Content-Type header was provided");
        } else if (!contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            this.logger.trace("Did not receive JSON-compatible Content-Type from introspection endpoint in response");
            throw new OAuth2IntrospectionException("Introspection endpoint response was invalid, as content type '" + contentType + "' is not compatible with JSON");
        } else {
            HTTPResponse response = new HTTPResponse(responseEntity.getStatusCodeValue());
            response.setHeader("Content-Type", contentType.toString());
            response.setContent((String)responseEntity.getBody());
            if (response.getStatusCode() != 200) {
                this.logger.trace("Introspection endpoint returned non-OK status code");
                throw new OAuth2IntrospectionException("Introspection endpoint responded with HTTP status code " + response.getStatusCode());
            } else {
                return response;
            }
        }
    }

    private TokenIntrospectionResponse parseNimbusResponse(HTTPResponse response) {
        try {
            return TokenIntrospectionResponse.parse(response);
        } catch (Exception var3) {
            throw new OAuth2IntrospectionException(var3.getMessage(), var3);
        }
    }

    private TokenIntrospectionSuccessResponse castToNimbusSuccess(TokenIntrospectionResponse introspectionResponse) {
        if (!introspectionResponse.indicatesSuccess()) {
            ErrorObject errorObject = introspectionResponse.toErrorResponse().getErrorObject();
            String message = "Token introspection failed with response " + errorObject.toJSONObject().toJSONString();
            this.logger.trace(message);
            throw new OAuth2IntrospectionException(message);
        } else {
            return (TokenIntrospectionSuccessResponse)introspectionResponse;
        }
    }

    private OAuth2AuthenticatedPrincipal convertClaimsSet(TokenIntrospectionSuccessResponse response) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Map<String, Object> claims = response.toJSONObject();
        Iterator var5;
        if (response.getAudience() != null) {
            List<String> audiences = new ArrayList<>();
            var5 = response.getAudience().iterator();

            while(var5.hasNext()) {
                Audience audience = (Audience)var5.next();
                audiences.add(audience.getValue());
            }

            claims.put("aud", Collections.unmodifiableList(audiences));
        }

        if (response.getClientID() != null) {
            claims.put("client_id", response.getClientID().getValue());
        }

        Instant iat;
        if (response.getExpirationTime() != null) {
            iat = response.getExpirationTime().toInstant();
            claims.put("exp", iat);
        }

        if (response.getIssueTime() != null) {
            iat = response.getIssueTime().toInstant();
            claims.put("iat", iat);
        }

        if (response.getIssuer() != null) {
            claims.put("iss", response.getIssuer().getValue());
        }

        if (response.getNotBeforeTime() != null) {
            claims.put("nbf", response.getNotBeforeTime().toInstant());
        }

        if (response.getScope() != null) {
            List<String> scopes = Collections.unmodifiableList(response.getScope().toStringList());
            claims.put("scope", scopes);
            var5 = scopes.iterator();

            while(var5.hasNext()) {
                String scope = (String)var5.next();
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
            }
        }

        return new OAuth2IntrospectionAuthenticatedPrincipal(claims, authorities);
    }

}