package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import com.generatera.authorization.application.server.oauth2.login.config.authorization.OAuth2loginParameterNames;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


/**
 * 支持resource owner authorization grant type request ..
 */
public class DefaultOauth2AuthorizationExtRequestResolver implements LightningOAuth2AuthorizationExtRequestResolver {

    private final AntPathRequestMatcher authorizationRequestMatcher;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private Consumer<OAuth2AuthorizationExtRequest.Builder> authorizationExtRequestCustomizer = (customizer) -> {
    };

    private static final StringKeyGenerator DEFAULT_SECURE_KEY_GENERATOR = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

    private static final Consumer<OAuth2AuthorizationExtRequest.Builder> DEFAULT_PKCE_APPLIER = OAuth2AuthorizationExtRequestCustomizers.withPkce();

    public void setAuthorizationExtRequestCustomizer(Consumer<OAuth2AuthorizationExtRequest.Builder> authorizationExtRequestCustomizer) {
        Assert.notNull(authorizationExtRequestCustomizer,"authorizationExtRequestCustomizer must not be null !!!");
        this.authorizationExtRequestCustomizer = authorizationExtRequestCustomizer;
    }

    public DefaultOauth2AuthorizationExtRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                                        String authorizationRequestBaseUri) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestMatcher = new AntPathRequestMatcher(authorizationRequestBaseUri + "/{" + "registrationId" + "}");
    }

    @Override
    public OAuth2AuthorizationExtRequest resolve(HttpServletRequest request) {
        String registrationId = this.resolveRegistrationId(request);
        if (registrationId == null) {
            return null;
        } else {
            String redirectUriAction = this.getAction(request, "login");
            return this.resolve(request, registrationId, redirectUriAction);
        }
    }

    private String resolveRegistrationId(HttpServletRequest request) {
        return this.authorizationRequestMatcher.matches(request) ? this.authorizationRequestMatcher.matcher(request).getVariables().get("registrationId") : null;
    }

    private String getAction(HttpServletRequest request, String defaultAction) {
        String action = request.getParameter("action");
        return action == null ? defaultAction : action;
    }

    @Override
    public OAuth2AuthorizationExtRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        if (clientRegistrationId == null) {
            return null;
        } else {
            String redirectUriAction = this.getAction(request, "authorize");
            return this.resolve(request, clientRegistrationId, redirectUriAction);
        }
    }

    private OAuth2AuthorizationExtRequest resolve(HttpServletRequest request, @Nullable String registrationId, String redirectUriAction) {
        if (registrationId == null) {
            return null;
        } else {
            ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
            if (clientRegistration == null) {
                throw new IllegalArgumentException("Invalid Client Registration with Id: " + registrationId);
            } else if(AuthorizationGrantType.PASSWORD.equals(clientRegistration.getAuthorizationGrantType())) {
                OAuth2AuthorizationExtRequest.Builder builder = this.getBuilder(clientRegistration);
                String redirectUriStr = expandRedirectUri(request, clientRegistration, redirectUriAction);
                // 这里直接进行 token endpoint request ..
                // 用户密码需要授权中心 填充 ..
                builder
                        .clientId(clientRegistration.getClientId())
                        .clientSecret(clientRegistration.getClientSecret())
                        // 授权url
                        .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                        .redirectUri(redirectUriStr)
                        .scopes(clientRegistration.getScopes());

                this.authorizationExtRequestCustomizer.accept(builder);
                return builder.build();
            }

            // 返回 null
            return null;
        }
    }


    private OAuth2AuthorizationExtRequest.Builder getBuilder(ClientRegistration clientRegistration) {
        if (AuthorizationGrantType.PASSWORD.getValue().equals(clientRegistration.getAuthorizationGrantType().getValue())) {
            OAuth2AuthorizationExtRequest.Builder builder = OAuth2AuthorizationExtRequest.password();
            // 标识它是 PASSWORD
            builder.additionalParameters(parameters -> parameters.put(OAuth2loginParameterNames.OAUTH2_GRANT_TYPE,AuthorizationGrantType.PASSWORD.getValue()));
            if (!CollectionUtils.isEmpty(clientRegistration.getScopes()) && clientRegistration.getScopes().contains("openid")) {
                applyNonce(builder);
            }

            if (ClientAuthenticationMethod.NONE.equals(clientRegistration.getClientAuthenticationMethod())) {
                DEFAULT_PKCE_APPLIER.accept(builder);
            }

            return builder;
        } else {
            throw new IllegalArgumentException("Invalid Authorization Grant Type (" + clientRegistration.getAuthorizationGrantType().getValue() + ") for Client Registration with Id: " + clientRegistration.getRegistrationId());
        }
    }



    // 回来的请求地址 ...
    private static String expandRedirectUri(HttpServletRequest request, ClientRegistration clientRegistration, String action) {
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("registrationId", clientRegistration.getRegistrationId());
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .build();
        String scheme = uriComponents.getScheme();
        uriVariables.put("baseScheme", scheme != null ? scheme : "");
        String host = uriComponents.getHost();
        uriVariables.put("baseHost", host != null ? host : "");
        int port = uriComponents.getPort();
        uriVariables.put("basePort", port == -1 ? "" : ":" + port);
        String path = uriComponents.getPath();
        if (StringUtils.hasLength(path) && path.charAt(0) != '/') {
            path = '/' + path;
        }

        uriVariables.put("basePath", path != null ? path : "");
        uriVariables.put("baseUrl", uriComponents.toUriString());

        uriVariables.put("action", action != null ? action : "");
        return UriComponentsBuilder
                .fromUriString(clientRegistration.getRedirectUri())
                .buildAndExpand(uriVariables)
                .toUriString();
    }

    private static void applyNonce(OAuth2AuthorizationExtRequest.Builder builder) {
        try {
            String nonce = DEFAULT_SECURE_KEY_GENERATOR.generateKey();
            String nonceHash = createHash(nonce);
            builder.attributes((attrs) -> {
                attrs.put("nonce", nonce);
            });
            builder.additionalParameters((params) -> {
                params.put("nonce", nonceHash);
            });
        } catch (NoSuchAlgorithmException var3) {
        }

    }

    private static String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
