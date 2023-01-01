package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.util.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 仅仅是 OAuth2AuthorizationRequest的复制版,支持 resource owner authorization grant type ..
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2AuthorizationExtRequest {
    private String authorizationUri;
    private AuthorizationGrantType authorizationGrantType;
    private OAuth2AuthorizationResponseType responseType;
    private String clientId;
    private String redirectUri;
    private Set<String> scopes;
    private String state;
    private Map<String, Object> additionalParameters;
    private String authorizationRequestUri;
    private Map<String, Object> attributes;

    public static OAuth2AuthorizationExtRequest.Builder password() {
        return new Builder(AuthorizationGrantType.PASSWORD);
    }


    public static final class Builder {
        private String authorizationUri;
        private AuthorizationGrantType authorizationGrantType;
        private OAuth2AuthorizationResponseType responseType;
        private String clientId;
        /**
         * 客户端 密码
         */
        private String clientSecret;

        private String redirectUri;
        private Set<String> scopes;
        private String state;
        private Map<String, Object> additionalParameters;
        private Consumer<Map<String, Object>> parametersConsumer;
        private Map<String, Object> attributes;
        private String authorizationRequestUri;
        private Function<UriBuilder, URI> authorizationRequestUriFunction;
        private final DefaultUriBuilderFactory uriBuilderFactory;

        protected Builder(AuthorizationGrantType authorizationGrantType) {
            this.additionalParameters = new LinkedHashMap();
            this.parametersConsumer = (params) -> {
            };
            this.attributes = new LinkedHashMap();
            this.authorizationRequestUriFunction = (builder) -> {
                return builder.build(new Object[0]);
            };
            Assert.notNull(authorizationGrantType, "authorizationGrantType cannot be null");
            this.authorizationGrantType = authorizationGrantType;
            if (AuthorizationGrantType.PASSWORD.equals(authorizationGrantType)) {
                this.responseType = OAuth2AuthorizationResponseType.TOKEN;
            }


            this.uriBuilderFactory = new DefaultUriBuilderFactory();
            this.uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        }

        public Builder authorizationUri(String authorizationUri) {
            this.authorizationUri = authorizationUri;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder scope(String... scope) {
            return scope != null && scope.length > 0 ? this.scopes(new LinkedHashSet(Arrays.asList(scope))) : this;
        }

        public Builder scopes(Set<String> scopes) {
            this.scopes = scopes;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder additionalParameters(Map<String, Object> additionalParameters) {
            if (!CollectionUtils.isEmpty(additionalParameters)) {
                this.additionalParameters.putAll(additionalParameters);
            }

            return this;
        }

        public Builder additionalParameters(Consumer<Map<String, Object>> additionalParametersConsumer) {
            if (additionalParametersConsumer != null) {
                additionalParametersConsumer.accept(this.additionalParameters);
            }

            return this;
        }

        public Builder parameters(Consumer<Map<String, Object>> parametersConsumer) {
            if (parametersConsumer != null) {
                this.parametersConsumer = parametersConsumer;
            }

            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            if (!CollectionUtils.isEmpty(attributes)) {
                this.attributes.putAll(attributes);
            }

            return this;
        }

        public Builder attributes(Consumer<Map<String, Object>> attributesConsumer) {
            if (attributesConsumer != null) {
                attributesConsumer.accept(this.attributes);
            }

            return this;
        }

        public Builder authorizationRequestUri(String authorizationRequestUri) {
            this.authorizationRequestUri = authorizationRequestUri;
            return this;
        }

        public Builder authorizationRequestUri(Function<UriBuilder, URI> authorizationRequestUriFunction) {
            if (authorizationRequestUriFunction != null) {
                this.authorizationRequestUriFunction = authorizationRequestUriFunction;
            }

            return this;
        }

        public OAuth2AuthorizationExtRequest build() {
            Assert.hasText(this.authorizationUri, "authorizationUri cannot be empty");
            Assert.hasText(this.clientId, "clientId cannot be empty");
            if (AuthorizationGrantType.IMPLICIT.equals(this.authorizationGrantType)) {
                Assert.hasText(this.redirectUri, "redirectUri cannot be empty");
            }

            OAuth2AuthorizationExtRequest authorizationRequest = new OAuth2AuthorizationExtRequest();
            authorizationRequest.authorizationUri = this.authorizationUri;
            authorizationRequest.authorizationGrantType = this.authorizationGrantType;
            authorizationRequest.responseType = this.responseType;
            authorizationRequest.clientId = this.clientId;
            authorizationRequest.redirectUri = this.redirectUri;
            authorizationRequest.state = this.state;
            authorizationRequest.scopes = Collections.unmodifiableSet((Set)(CollectionUtils.isEmpty(this.scopes) ? Collections.emptySet() : new LinkedHashSet(this.scopes)));
            authorizationRequest.additionalParameters = Collections.unmodifiableMap(this.additionalParameters);
            authorizationRequest.attributes = Collections.unmodifiableMap(this.attributes);
            authorizationRequest.authorizationRequestUri = StringUtils.hasText(this.authorizationRequestUri) ? this.authorizationRequestUri : this.buildAuthorizationRequestUri();
            return authorizationRequest;
        }

        private String buildAuthorizationRequestUri() {
            Map<String, Object> parameters = this.getParameters();
            this.parametersConsumer.accept(parameters);
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap();
            parameters.forEach((k, v) -> {
                queryParams.set(encodeQueryParam(k), encodeQueryParam(String.valueOf(v)));
            });
            UriBuilder uriBuilder = this.uriBuilderFactory.uriString(this.authorizationUri).queryParams(queryParams);
            return ((URI)this.authorizationRequestUriFunction.apply(uriBuilder)).toString();
        }

        private Map<String, Object> getParameters() {
            Map<String, Object> parameters = new LinkedHashMap();
            parameters.put("response_type", this.responseType.getValue());
            parameters.put("client_id", this.clientId);
            if (!CollectionUtils.isEmpty(this.scopes)) {
                parameters.put("scope", StringUtils.collectionToDelimitedString(this.scopes, " "));
            }

            if (this.state != null) {
                parameters.put("state", this.state);
            }

            if (this.redirectUri != null) {
                parameters.put("redirect_uri", this.redirectUri);
            }

            parameters.putAll(this.additionalParameters);
            return parameters;
        }

        private static String encodeQueryParam(String value) {
            return UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
        }
    }
}
