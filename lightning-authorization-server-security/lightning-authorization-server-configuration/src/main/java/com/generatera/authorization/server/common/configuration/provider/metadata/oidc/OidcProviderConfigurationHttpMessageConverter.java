package com.generatera.authorization.server.common.configuration.provider.metadata.oidc;

import com.generatera.authorization.server.common.configuration.provider.metadata.AbstractAuthorizationServerMetadata;
import com.generatera.authorization.server.common.configuration.provider.metadata.ClaimTypeConverter;
import com.generatera.authorization.server.common.configuration.provider.metadata.HttpMessageConverters;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.converter.ClaimConversionService;
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

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/11
 * @time 15:23
 * @Description OidcProviderConfiguraion Http Message Converter
 */
public class OidcProviderConfigurationHttpMessageConverter extends AbstractHttpMessageConverter<OidcProviderConfiguration> {
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private final GenericHttpMessageConverter<Object> jsonMessageConverter = HttpMessageConverters.getJsonMessageConverter();
    private Converter<Map<String, Object>, OidcProviderConfiguration> providerConfigurationConverter = new OidcProviderConfigurationHttpMessageConverter.OidcProviderConfigurationConverter();
    private Converter<OidcProviderConfiguration, Map<String, Object>> providerConfigurationParametersConverter = AbstractAuthorizationServerMetadata::getClaims;

    public OidcProviderConfigurationHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        Assert.notNull(jsonMessageConverter,"jsonMessageConverter must not be null !!!");
    }

    protected boolean supports(@NotNull Class<?> clazz) {
        return OidcProviderConfiguration.class.isAssignableFrom(clazz);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    protected OidcProviderConfiguration readInternal(@NotNull Class<? extends OidcProviderConfiguration> clazz, @NotNull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        try {
            assert jsonMessageConverter != null;
            Map<String, Object> providerConfigurationParameters = (Map<String,Object>)this.jsonMessageConverter.read(STRING_OBJECT_MAP.getType(), null, inputMessage);
            OidcProviderConfiguration providerConfiguration = this.providerConfigurationConverter.convert(providerConfigurationParameters);
            assert providerConfiguration != null;
            return providerConfiguration;
        } catch (Exception var4) {
            throw new HttpMessageNotReadableException("An error occurred reading the OpenID Provider Configuration: " + var4.getMessage(), var4, inputMessage);
        }
    }

    protected void writeInternal(@NotNull OidcProviderConfiguration providerConfiguration, @NotNull HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        try {
            Map<String, Object> providerConfigurationResponseParameters = this.providerConfigurationParametersConverter.convert(providerConfiguration);
            assert jsonMessageConverter != null;
            assert providerConfigurationResponseParameters != null;
            this.jsonMessageConverter.write(providerConfigurationResponseParameters, STRING_OBJECT_MAP.getType(), MediaType.APPLICATION_JSON, outputMessage);
        } catch (Exception var4) {
            throw new HttpMessageNotWritableException("An error occurred writing the OpenID Provider Configuration: " + var4.getMessage(), var4);
        }
    }

    public final void setProviderConfigurationConverter(Converter<Map<String, Object>, OidcProviderConfiguration> providerConfigurationConverter) {
        Assert.notNull(providerConfigurationConverter, "providerConfigurationConverter cannot be null");
        this.providerConfigurationConverter = providerConfigurationConverter;
    }

    public final void setProviderConfigurationParametersConverter(Converter<OidcProviderConfiguration, Map<String, Object>> providerConfigurationParametersConverter) {
        Assert.notNull(providerConfigurationParametersConverter, "providerConfigurationParametersConverter cannot be null");
        this.providerConfigurationParametersConverter = providerConfigurationParametersConverter;
    }

    private static final class OidcProviderConfigurationConverter implements Converter<Map<String, Object>, OidcProviderConfiguration> {
        private static final ClaimConversionService CLAIM_CONVERSION_SERVICE = ClaimConversionService.getSharedInstance();
        private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);
        private static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);
        private static final TypeDescriptor URL_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(URL.class);
        private final ClaimTypeConverter claimTypeConverter;

        private OidcProviderConfigurationConverter() {
            Converter<Object, ?> collectionStringConverter = getConverter(TypeDescriptor.collection(Collection.class, STRING_TYPE_DESCRIPTOR));
            Converter<Object, ?> urlConverter = getConverter(URL_TYPE_DESCRIPTOR);
            Map<String, Converter<Object, ?>> claimConverters = new HashMap();
            claimConverters.put("issuer", urlConverter);
            //claimConverters.put("authorization_endpoint", urlConverter);
            //claimConverters.put("token_endpoint", urlConverter);
            //claimConverters.put("token_endpoint_auth_methods_supported", collectionStringConverter);
            claimConverters.put("jwks_uri", urlConverter);
            //claimConverters.put("userinfo_endpoint", urlConverter);
            claimConverters.put("response_types_supported", collectionStringConverter);
            //claimConverters.put("grant_types_supported", collectionStringConverter);
            claimConverters.put("subject_types_supported", collectionStringConverter);
            claimConverters.put("id_token_signing_alg_values_supported", collectionStringConverter);
            claimConverters.put("scopes_supported", collectionStringConverter);
            this.claimTypeConverter = new ClaimTypeConverter(claimConverters);
        }

        public OidcProviderConfiguration convert(@NotNull Map<String, Object> source) {
            Map<String, Object> parsedClaims = this.claimTypeConverter.convert(source);
            return OidcProviderConfiguration.withClaims(parsedClaims).build();
        }

        private static Converter<Object, ?> getConverter(TypeDescriptor targetDescriptor) {
            return (source) -> CLAIM_CONVERSION_SERVICE.convert(source, OBJECT_TYPE_DESCRIPTOR, targetDescriptor);
        }
    }
}