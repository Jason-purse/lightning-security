package com.generatera.central.oauth2.authorization.server.configuration.model.entity.authorization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
/**
 * @author FLJ
 * @date 2023/1/16
 * @time 11:14
 * @Description oauth2 authorization request entity
 *
 * todo 需要不断维护 {@link org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest} 和这个实体的关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2AuthorizationRequestEntity implements Serializable {

    private String authorizationUri;
    private String authorizationGrantType;
    private String responseType;
    private String clientId;
    private String redirectUri;
    private Set<String> scopes;
    private String state;
    private Map<String, Object> additionalParameters;
    private String authorizationRequestUri;
    private Map<String, Object> attributes;

}
