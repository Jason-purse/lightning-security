package com.generatera.authorization.application.server.config.specification;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.converter.ClaimConversionService;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.converter.ClaimTypeConverter;
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

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OAuth2AuthorizationServerMetadataHttpMessageConverter extends AbstractHttpMessageConverter<OAuth2AuthorizationServerMetadata> {
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private final GenericHttpMessageConverter<Object> jsonMessageConverter = HttpMessageConverters.getJsonMessageConverter();
    private Converter<Map<String, Object>, OAuth2AuthorizationServerMetadata> authorizationServerMetadataConverter = new OAuth2AuthorizationServerMetadataHttpMessageConverter.OAuth2AuthorizationServerMetadataConverter();
    private Converter<OAuth2AuthorizationServerMetadata, Map<String, Object>> authorizationServerMetadataParametersConverter = source -> source.getClaims();

    public OAuth2AuthorizationServerMetadataHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    protected boolean supports(Class<?> clazz) {
        return OAuth2AuthorizationServerMetadata.class.isAssignableFrom(clazz);
    }

    protected OAuth2AuthorizationServerMetadata readInternal(Class<? extends OAuth2AuthorizationServerMetadata> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        try {
            Map<String, Object> authorizationServerMetadataParameters = (Map)this.jsonMessageConverter.read(STRING_OBJECT_MAP.getType(), (Class)null, inputMessage);
            return (OAuth2AuthorizationServerMetadata)this.authorizationServerMetadataConverter.convert(authorizationServerMetadataParameters);
        } catch (Exception var4) {
            throw new HttpMessageNotReadableException("An error occurred reading the OAuth 2.0 Authorization Server Metadata: " + var4.getMessage(), var4, inputMessage);
        }
    }

    protected void writeInternal(OAuth2AuthorizationServerMetadata authorizationServerMetadata, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        try {
            Map<String, Object> authorizationServerMetadataResponseParameters = (Map)this.authorizationServerMetadataParametersConverter.convert(authorizationServerMetadata);
            this.jsonMessageConverter.write(authorizationServerMetadataResponseParameters, STRING_OBJECT_MAP.getType(), MediaType.APPLICATION_JSON, outputMessage);
        } catch (Exception var4) {
            throw new HttpMessageNotWritableException("An error occurred writing the OAuth 2.0 Authorization Server Metadata: " + var4.getMessage(), var4);
        }
    }

    public final void setAuthorizationServerMetadataConverter(Converter<Map<String, Object>, OAuth2AuthorizationServerMetadata> authorizationServerMetadataConverter) {
        Assert.notNull(authorizationServerMetadataConverter, "authorizationServerMetadataConverter cannot be null");
        this.authorizationServerMetadataConverter = authorizationServerMetadataConverter;
    }

    public final void setAuthorizationServerMetadataParametersConverter(Converter<OAuth2AuthorizationServerMetadata, Map<String, Object>> authorizationServerMetadataParametersConverter) {
        Assert.notNull(authorizationServerMetadataParametersConverter, "authorizationServerMetadataParametersConverter cannot be null");
        this.authorizationServerMetadataParametersConverter = authorizationServerMetadataParametersConverter;
    }

    private static final class OAuth2AuthorizationServerMetadataConverter implements Converter<Map<String, Object>, OAuth2AuthorizationServerMetadata> {
        private static final ClaimConversionService CLAIM_CONVERSION_SERVICE = ClaimConversionService.getSharedInstance();
        private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);
        private static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);
        private static final TypeDescriptor URL_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(URL.class);
        private final ClaimTypeConverter claimTypeConverter;

        private OAuth2AuthorizationServerMetadataConverter() {
            Converter<Object, ?> collectionStringConverter = getConverter(TypeDescriptor.collection(Collection.class, STRING_TYPE_DESCRIPTOR));
            Converter<Object, ?> urlConverter = getConverter(URL_TYPE_DESCRIPTOR);
            Map<String, Converter<Object, ?>> claimConverters = new HashMap();
            claimConverters.put("issuer", urlConverter);
            claimConverters.put("authorization_endpoint", urlConverter);
            claimConverters.put("token_endpoint", urlConverter);
            claimConverters.put("token_endpoint_auth_methods_supported", collectionStringConverter);
            claimConverters.put("jwks_uri", urlConverter);
            claimConverters.put("scopes_supported", collectionStringConverter);
            claimConverters.put("response_types_supported", collectionStringConverter);
            claimConverters.put("grant_types_supported", collectionStringConverter);
            claimConverters.put("revocation_endpoint", urlConverter);
            claimConverters.put("revocation_endpoint_auth_methods_supported", collectionStringConverter);
            claimConverters.put("introspection_endpoint", urlConverter);
            claimConverters.put("introspection_endpoint_auth_methods_supported", collectionStringConverter);
            claimConverters.put("code_challenge_methods_supported", collectionStringConverter);
            this.claimTypeConverter = new ClaimTypeConverter(claimConverters);
        }

        public OAuth2AuthorizationServerMetadata convert(Map<String, Object> source) {
            Map<String, Object> parsedClaims = this.claimTypeConverter.convert(source);
            return OAuth2AuthorizationServerMetadata.withClaims(parsedClaims).build();
        }

        private static Converter<Object, ?> getConverter(TypeDescriptor targetDescriptor) {
            return (source) -> {
                return CLAIM_CONVERSION_SERVICE.convert(source, OBJECT_TYPE_DESCRIPTOR, targetDescriptor);
            };
        }
    }
}