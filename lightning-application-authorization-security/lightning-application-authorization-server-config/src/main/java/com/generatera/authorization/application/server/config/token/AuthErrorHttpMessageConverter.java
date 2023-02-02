package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.ApplicationAuthException;
import com.generatera.authorization.server.common.configuration.provider.metadata.HttpMessageConverters;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.jianyue.lightning.result.Result;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 16:37
 * @Description 认证错误的http 消息转换器 ...
 *
 * 主要为了转换{@link com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException}
 * 但是它的工作主要被{@link com.generatera.authorization.application.server.config.authentication.DefaultLightningAbstractAuthenticationEntryPoint}
 * 代替了 ...
 *
 * 保留它仅仅是为了{@link AuthTokenEndpointFilter}的完整性
 *
 * 其次它的反序列化动作,并没有得到验证和使用
 *
 * // TODO: 2023/1/29  后续修改反序列化动作
 */
public class AuthErrorHttpMessageConverter extends AbstractHttpMessageConverter<LightningAuthError> {
    private static final Charset DEFAULT_CHARSET;
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP;
    private final GenericHttpMessageConverter<Object> jsonMessageConverter = Objects.requireNonNull(HttpMessageConverters.getJsonMessageConverter());
    protected Converter<Map<String, String>, LightningAuthError> errorConverter = new AuthErrorConverter();
    protected Converter<LightningAuthError, Map<String, String>> errorParametersConverter = new AuthErrorParametersConverter();

    public AuthErrorHttpMessageConverter() {
        super(DEFAULT_CHARSET, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    protected boolean supports(@NotNull Class<?> clazz) {
        return LightningAuthError.class.isAssignableFrom(clazz);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    protected LightningAuthError readInternal(@NotNull Class<? extends LightningAuthError> clazz, @NotNull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        try {
            Map<String, Object> errorParameters = (Map<String, Object>) this.jsonMessageConverter.read(STRING_OBJECT_MAP.getType(), null, inputMessage);
            LightningAuthError convert = this.errorConverter.convert(errorParameters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> String.valueOf(entry.getValue()))));
            assert convert != null;
            return convert;
        } catch (Exception var4) {
            throw new HttpMessageNotReadableException("An error occurred reading the Auth Error: " + var4.getMessage(), var4, inputMessage);
        }
    }

    protected void writeInternal(@NotNull LightningAuthError authError, @NotNull HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        try {
            this.jsonMessageConverter.write(Result.error(ApplicationAuthException.authenticationFailureException().getCode(),
                    authError.getDescription()), STRING_OBJECT_MAP.getType(), MediaType.APPLICATION_JSON, outputMessage);
        } catch (Exception var4) {
            throw new HttpMessageNotWritableException("An error occurred writing the Auth  Error: " + var4.getMessage(), var4);
        }
    }

    public final void setErrorConverter(Converter<Map<String, String>, LightningAuthError> errorConverter) {
        Assert.notNull(errorConverter, "errorConverter cannot be null");
        this.errorConverter = errorConverter;
    }

    public final void setErrorParametersConverter(Converter<LightningAuthError, Map<String, String>> errorParametersConverter) {
        Assert.notNull(errorParametersConverter, "errorParametersConverter cannot be null");
        this.errorParametersConverter = errorParametersConverter;
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
        STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
        };
    }

    private static class AuthErrorParametersConverter implements Converter<LightningAuthError, Map<String, String>> {
        private AuthErrorParametersConverter() {
        }

        public Map<String, String> convert(LightningAuthError authError) {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("error", authError.getErrorCode());
            if (StringUtils.hasText(authError.getDescription())) {
                parameters.put("error_description", authError.getDescription());
            }

            if (StringUtils.hasText(authError.getUri())) {
                parameters.put("error_uri", authError.getUri());
            }

            return parameters;
        }
    }

    private static class AuthErrorConverter implements Converter<Map<String, String>, LightningAuthError> {
        private AuthErrorConverter() {
        }

        public LightningAuthError convert(Map<String, String> parameters) {
            String errorCode = parameters.get("error");
            String errorDescription = parameters.get("error_description");
            String errorUri = parameters.get("error_uri");
            return new LightningAuthError(errorCode, errorDescription, errorUri);
        }
    }
}