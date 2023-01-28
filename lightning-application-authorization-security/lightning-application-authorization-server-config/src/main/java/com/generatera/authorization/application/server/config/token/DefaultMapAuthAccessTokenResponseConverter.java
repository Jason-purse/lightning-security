package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.LightningAccessTokenGenerator;
import com.generatera.security.authorization.server.specification.components.token.LightningRefreshTokenGenerator;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.plain.DefaultPlainToken;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

// TODO: 2023/1/28
public final class DefaultMapAuthAccessTokenResponseConverter implements Converter<Map<String, Object>, ApplicationLevelAuthorizationToken> {
    private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = new HashSet<>(Arrays.asList("access_token", "expires_in", "refresh_token", "scope", JwtExtClaimNames.AUTHORITIES_CLAIM, "access_token_type", "refresh_token"));

    public DefaultMapAuthAccessTokenResponseConverter() {
    }

    public ApplicationLevelAuthorizationToken convert(@NotNull Map<String, Object> source) {
        String accessToken = getParameterValue(source, "access_token");
        long expiresIn = getExpiresIn(source);
        long issuesIn = getIssuesIn(source);
        LightningTokenValueType accessTokenType = getAccessTokenType(source);
        LightningTokenType.LightningTokenValueFormat accessTokenFormat = getAccessTokenValueFormat(source);
        LightningTokenValueType refreshTokenType = getRefreshTokenType(source);
        String refreshToken = getParameterValue(source, "refresh_token");

        return ApplicationLevelAuthorizationToken.of(
                new LightningAccessTokenGenerator.LightningAuthenticationAccessToken(
                        new DefaultPlainToken(
                                accessToken,
                                Instant.ofEpochMilli(issuesIn),
                                Instant.ofEpochMilli(expiresIn)
                        ),
                        accessTokenType,
                            accessTokenFormat
                        ),
                new LightningRefreshTokenGenerator.LightningAuthenticationRefreshToken(
                        new DefaultPlainToken(refreshToken)
                )
        );
    }

    private LightningTokenType.LightningTokenValueFormat getAccessTokenValueFormat(Map<String, Object> tokenResponseParameters) {
        String access_token_type = getParameterValue(tokenResponseParameters, "access_token_type");
        return LightningTokenType.LightningTokenValueFormat.JWT.value().equalsIgnoreCase(access_token_type) ? LightningTokenType.LightningTokenValueFormat.JWT :
                LightningTokenType.LightningTokenValueFormat.OPAQUE.value().equalsIgnoreCase(access_token_type) ? LightningTokenType.LightningTokenValueFormat.OPAQUE :
                        null;

    }

    private static LightningTokenValueType getAccessTokenType(Map<String, Object> tokenResponseParameters) {
        return LightningTokenValueType.BEARER_TOKEN_TYPE.value().equalsIgnoreCase(getParameterValue(tokenResponseParameters, "access_token_type")) ? LightningTokenValueType.BEARER_TOKEN_TYPE : null;
    }

    private static LightningTokenValueType getRefreshTokenType(Map<String, Object> tokenResponseParameters) {
        return LightningTokenValueType.BEARER_TOKEN_TYPE.value().equalsIgnoreCase(getParameterValue(tokenResponseParameters, "refresh_token_type")) ? LightningTokenValueType.BEARER_TOKEN_TYPE : null;
    }


    private static long getExpiresIn(Map<String, Object> tokenResponseParameters) {
        return getParameterValue(tokenResponseParameters, "expires_in", 0L);
    }

    private static long getIssuesIn(Map<String, Object> tokenResponseParameters) {
        return getParameterValue(tokenResponseParameters, "issues_in", 0L);
    }

    private static Set<String> getScopes(Map<String, Object> tokenResponseParameters) {
        if (tokenResponseParameters.containsKey("scope")) {
            String scope = getParameterValue(tokenResponseParameters, "scope");
            return new HashSet(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        } else {
            return Collections.emptySet();
        }
    }

    private static String getParameterValue(Map<String, Object> tokenResponseParameters, String parameterName) {
        Object obj = tokenResponseParameters.get(parameterName);
        return obj != null ? obj.toString() : null;
    }

    private static long getParameterValue(Map<String, Object> tokenResponseParameters, String parameterName, long defaultValue) {
        long parameterValue = defaultValue;
        Object obj = tokenResponseParameters.get(parameterName);
        if (obj != null) {
            if (obj.getClass() == Long.class) {
                parameterValue = (Long) obj;
            } else if (obj.getClass() == Integer.class) {
                parameterValue = (long) (Integer) obj;
            } else {
                try {
                    parameterValue = Long.parseLong(obj.toString());
                } catch (NumberFormatException var8) {
                }
            }
        }

        return parameterValue;
    }

}