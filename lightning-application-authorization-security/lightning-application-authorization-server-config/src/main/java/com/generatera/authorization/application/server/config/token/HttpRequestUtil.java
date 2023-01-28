package com.generatera.authorization.application.server.config.token;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

final class HttpRequestUtil {
    public static MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
        parameterMap.forEach((key, values) -> {
            if (values.length > 0) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });
        return parameters;
    }
}
