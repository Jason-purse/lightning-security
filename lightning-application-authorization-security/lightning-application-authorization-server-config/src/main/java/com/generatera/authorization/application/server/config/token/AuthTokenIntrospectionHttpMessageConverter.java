package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.provider.metadata.ClaimTypeConverter;
import com.generatera.authorization.server.common.configuration.provider.metadata.HttpMessageConverters;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.converter.ClaimConversionService;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.time.Instant;
import java.util.*;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 12:46
 * @Description 将认证省查的结果写出 ...
 *
 * spring oauth2 copy
 */
public class AuthTokenIntrospectionHttpMessageConverter extends AbstractHttpMessageConverter<AuthTokenIntrospection> {
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
    };
    @NotNull
    private final GenericHttpMessageConverter<Object> jsonMessageConverter = Objects.requireNonNull(HttpMessageConverters.getJsonMessageConverter());
    private Converter<Map<String, Object>, AuthTokenIntrospection> tokenIntrospectionConverter = new AuthTokenIntrospectionHttpMessageConverter.MapAuthTokenIntrospectionConverter();
    private Converter<AuthTokenIntrospection, Map<String, Object>> tokenIntrospectionParametersConverter = new AuthTokenIntrospectionHttpMessageConverter.AuthTokenIntrospectionMapConverter();

    public AuthTokenIntrospectionHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    protected boolean supports(@NotNull Class<?> clazz) {
        return AuthTokenIntrospection.class.isAssignableFrom(clazz);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    protected AuthTokenIntrospection readInternal(@NotNull Class<? extends AuthTokenIntrospection> clazz, @NotNull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        try {
            Map<String, Object> tokenIntrospectionParameters = (Map<String,Object>)this.jsonMessageConverter.read(STRING_OBJECT_MAP.getType(), null, inputMessage);
            AuthTokenIntrospection introspection = this.tokenIntrospectionConverter.convert(tokenIntrospectionParameters);
            assert introspection != null;
            return introspection;
        } catch (Exception var4) {
            throw new HttpMessageNotReadableException("An error occurred reading the Token Introspection Response: " + var4.getMessage(), var4, inputMessage);
        }
    }

    protected void writeInternal(@NotNull AuthTokenIntrospection tokenIntrospection, @NotNull HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        try {
            Map<String, Object> tokenIntrospectionResponseParameters = this.tokenIntrospectionParametersConverter.convert(tokenIntrospection);
            assert tokenIntrospectionResponseParameters != null;
            this.jsonMessageConverter.write(tokenIntrospectionResponseParameters, STRING_OBJECT_MAP.getType(), MediaType.APPLICATION_JSON, outputMessage);
        } catch (Exception var4) {
            throw new HttpMessageNotWritableException("An error occurred writing the Token Introspection Response: " + var4.getMessage(), var4);
        }
    }

    public final void setTokenIntrospectionConverter(Converter<Map<String, Object>, AuthTokenIntrospection> tokenIntrospectionConverter) {
        Assert.notNull(tokenIntrospectionConverter, "tokenIntrospectionConverter cannot be null");
        this.tokenIntrospectionConverter = tokenIntrospectionConverter;
    }

    public final void setTokenIntrospectionParametersConverter(Converter<AuthTokenIntrospection, Map<String, Object>> tokenIntrospectionParametersConverter) {
        Assert.notNull(tokenIntrospectionParametersConverter, "tokenIntrospectionParametersConverter cannot be null");
        this.tokenIntrospectionParametersConverter = tokenIntrospectionParametersConverter;
    }

    private static final class AuthTokenIntrospectionMapConverter implements Converter<AuthTokenIntrospection, Map<String, Object>> {
        private AuthTokenIntrospectionMapConverter() {
        }

        public Map<String, Object> convert(AuthTokenIntrospection source) {
            Map<String, Object> responseClaims = new LinkedHashMap<>(source.getClaims());

            if (!CollectionUtils.isEmpty(source.getScopes())) {
                responseClaims.put(JwtExtClaimNames.SCOPE_CLAIM, StringUtils.collectionToDelimitedString(source.getScopes(), " "));
            }

            if(!CollectionUtils.isNotEmpty(source.getAuthorities())) {
                responseClaims.put(JwtExtClaimNames.AUTHORITIES_CLAIM,source.getAuthorities());
            }

            if (source.getExpiresAt() != null) {
                responseClaims.put("exp", source.getExpiresAt().getEpochSecond());
            }

            if (source.getIssuedAt() != null) {
                responseClaims.put("iat", source.getIssuedAt().getEpochSecond());
            }

            if (source.getNotBefore() != null) {
                responseClaims.put("nbf", source.getNotBefore().getEpochSecond());
            }

            return responseClaims;
        }
    }

    private static final class MapAuthTokenIntrospectionConverter implements Converter<Map<String, Object>, AuthTokenIntrospection> {
        private static final ClaimConversionService CLAIM_CONVERSION_SERVICE = ClaimConversionService.getSharedInstance();
        private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);
        private static final TypeDescriptor BOOLEAN_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Boolean.class);
        private static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);
        private static final TypeDescriptor INSTANT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Instant.class);
        private static final TypeDescriptor URL_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(URL.class);
        private final ClaimTypeConverter claimTypeConverter;

        private MapAuthTokenIntrospectionConverter() {
            Converter<Object, ?> booleanConverter = getConverter(BOOLEAN_TYPE_DESCRIPTOR);
            Converter<Object, ?> stringConverter = getConverter(STRING_TYPE_DESCRIPTOR);
            Converter<Object, ?> instantConverter = getConverter(INSTANT_TYPE_DESCRIPTOR);
            Converter<Object, ?> collectionStringConverter = getConverter(TypeDescriptor.collection(Collection.class, STRING_TYPE_DESCRIPTOR));
            Converter<Object, ?> urlConverter = getConverter(URL_TYPE_DESCRIPTOR);
            Map<String, Converter<Object, ?>> claimConverters = new HashMap<>();
            claimConverters.put("active", booleanConverter);
            claimConverters.put(JwtExtClaimNames.SCOPE_CLAIM, AuthTokenIntrospectionHttpMessageConverter.MapAuthTokenIntrospectionConverter::convertScope);
            claimConverters.put(JwtExtClaimNames.SCOPE_SHORT_CLAIM, AuthTokenIntrospectionHttpMessageConverter.MapAuthTokenIntrospectionConverter::convertScope);
            claimConverters.put(JwtExtClaimNames.AUTHORITIES_CLAIM, AuthTokenIntrospectionHttpMessageConverter.MapAuthTokenIntrospectionConverter::convertScope);
            claimConverters.put("username", stringConverter);
            claimConverters.put("token_type", stringConverter);
            claimConverters.put("exp", instantConverter);
            claimConverters.put("iat", instantConverter);
            claimConverters.put("nbf", instantConverter);
            claimConverters.put("sub", stringConverter);
            claimConverters.put("aud", collectionStringConverter);
            claimConverters.put("iss", urlConverter);
            claimConverters.put("jti", stringConverter);
            this.claimTypeConverter = new ClaimTypeConverter(claimConverters);
        }

        public AuthTokenIntrospection convert(@NotNull Map<String, Object> source) {
            Map<String, Object> parsedClaims = this.claimTypeConverter.convert(source);
            return AuthTokenIntrospection.withClaims(parsedClaims).build();
        }

        private static Converter<Object, ?> getConverter(TypeDescriptor targetDescriptor) {
            return (source) -> {
                return CLAIM_CONVERSION_SERVICE.convert(source, OBJECT_TYPE_DESCRIPTOR, targetDescriptor);
            };
        }

        private static List<String> convertScope(Object scope) {
            return scope == null ? Collections.emptyList() : Arrays.asList(StringUtils.delimitedListToStringArray(scope.toString(), " "));
        }
    }
}