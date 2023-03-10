package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.provider.metadata.HttpMessageConverters;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
/**
 * @author FLJ
 * @date 2023/1/29
 * @time 15:49
 * @Description 默认支持 {@link ApplicationLevelAuthorizationToken}的正确序列化 ...
 *
 * 但是它的工作被{@link com.generatera.authorization.application.server.config.authentication.DefaultLightningAbstractAuthenticationEntryPoint} 替代了,
 * 留下它仅仅是为了保留{@link AuthTokenEndpointConfigurer}的完整性 ..
 *
 * 目前它的反序列化动作并没有进行测试和使用{@link #readInternal(Class, HttpInputMessage)}
 *
 * // TODO: 2023/1/29 后续修改它的反序列化动作
 */
public class ApplicationLevelAuthorizationTokenHttpMessageConverter extends AbstractHttpMessageConverter<ApplicationLevelAuthorizationToken> {
    private static final Charset DEFAULT_CHARSET;
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP;
    private final GenericHttpMessageConverter<Object> jsonMessageConverter = Objects.requireNonNull(HttpMessageConverters.getJsonMessageConverter());

    private Converter<Map<String, Object>, ApplicationLevelAuthorizationToken> accessTokenResponseConverter = new DefaultMapAuthAccessTokenResponseConverter();

    private Converter<ApplicationLevelAuthorizationToken, Map<String, Object>> accessTokenResponseParametersConverter = new DefaultAuthAppLevelAuthorizationTokenResponseMapConverter();

    public ApplicationLevelAuthorizationTokenHttpMessageConverter() {
        super(DEFAULT_CHARSET, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    protected boolean supports(@NotNull Class<?> clazz) {
        return ApplicationLevelAuthorizationToken.class.isAssignableFrom(clazz);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    protected ApplicationLevelAuthorizationToken readInternal(@NotNull Class<? extends ApplicationLevelAuthorizationToken> clazz, @NotNull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        try {
            Map<String, Object> tokenResponseParameters = (Map<String, Object>) this.jsonMessageConverter.read(STRING_OBJECT_MAP.getType(), null, inputMessage);
            ApplicationLevelAuthorizationToken convert = this.accessTokenResponseConverter.convert(tokenResponseParameters);
            assert convert != null;
            return convert;
        } catch (Exception var5) {
            throw new HttpMessageNotReadableException("An error occurred reading the OAuth 2.0 Access Token Response: " + var5.getMessage(), var5, inputMessage);
        }
    }

    protected void writeInternal(@NotNull ApplicationLevelAuthorizationToken tokenResponse, @NotNull HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        try {
            Object tokenResponseParameters = this.accessTokenResponseParametersConverter.convert(tokenResponse);
            assert tokenResponseParameters != null;
            this.jsonMessageConverter.write(Result.success(200, "LOGIN_SUCCESS", tokenResponseParameters), STRING_OBJECT_MAP.getType(), MediaType.APPLICATION_JSON, outputMessage);
        } catch (Exception var4) {
            throw new HttpMessageNotWritableException("An error occurred writing the OAuth 2.0 Access Token Response: " + var4.getMessage(), var4);
        }
    }


    public final void setAccessTokenResponseConverter(Converter<Map<String, Object>, ApplicationLevelAuthorizationToken> accessTokenResponseConverter) {
        Assert.notNull(accessTokenResponseConverter, "accessTokenResponseConverter cannot be null");
        this.accessTokenResponseConverter = accessTokenResponseConverter;
    }


    public final void setAccessTokenResponseParametersConverter(Converter<ApplicationLevelAuthorizationToken, Map<String, Object>> accessTokenResponseParametersConverter) {
        Assert.notNull(accessTokenResponseParametersConverter, "accessTokenResponseParametersConverter cannot be null");
        this.accessTokenResponseParametersConverter = accessTokenResponseParametersConverter;
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
        STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
        };
    }
}
