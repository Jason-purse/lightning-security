package com.generatera.authorization.application.server.oauth2.login.config.model.entity.client.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "oauth2_client_registration")
@Entity
@Document("oauth2_client_registration")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientRegistrationEntity implements Serializable {
    @Id
    @MongoId
    private String id;

    @Column(name = "registration_id")
    @Field(name = "registration_id")
    private String registrationId;

    @Column(name = "client_id")
    @Field(name = "client_id")
    private String clientId;
    @Column(name = "client_secret")
    @Field(name = "client_secret")
    private String clientSecret;

    @Column(name = "client_authentication_method")
    @Field(name = "client_authentication_method")
    private String clientAuthenticationMethod;
    @Column(name = "authorization_grant_type")
    @Field(name = "authorization_grant_type")
    private String authorizationGrantType;
    @Column(name = "redirect_uri")
    @Field(name = "redirect_uri")
    private String redirectUri;

    @Column(name = "scopes",columnDefinition = "text")
    @Field(name = "scopes")
    private String scopes;


    @Column(name = "client_name")
    @Field(name = "client_name")
    private String clientName;

    /**
     * 此参数,对于相同provider下的多个client registration 有很好的标注效果 ..
     * 当 registrationId 不得已 和其他client registrationId 重复时, 肯定需要先
     * 修改 registrationId,那么 此时就需要providerName 来标识provider 配置 ..
     *
     * 配置流程如下:
     *  1. 首先这会尝试从 当前项已经提供的完整配置尝试处理 ...
     *      1.1 其中包括使用issuer uri 进行完整性配置校验
     *      1.2 否则尝试构建,构建失败继续进行
     *  2. 当自身的完整配置无法处理时,则通过此providerName(如果存在,否则使用 registrationId) 和 常用提供商的信息进行比对 ..
     *
     *
     *
     * 这个providerName 目的 仅仅是为了了标准{@link org.springframework.security.config.oauth2.client.CommonOAuth2Provider}的
     * 相关的提供者名称,例如 GOOGLE / FACEBOOK ...
     *
     * 当自身的完整配置无法处理时,则通过此providerName 和 常用提供商的信息进行比对 ..
     * 如果存在,则创建,否则报错 ...
     */
    @Column(name = "provider_name")
    @Field(name = "provider_name")
    private String providerName;


    // ------- provider settings

    @Column(name = "authorization_uri")
    @Field(name = "authorization_uri")
    private String authorizationUri;
    @Column(name = "token_uri")
    @Field(name = "token_uri")
    private String tokenUri;
    @Column(name = "userInfo_uri")
    @Field(name = "userInfo_uri")
    private String userInfoUri;
    @Column(name = "user_info_authentication_method")
    @Field(name = "user_info_authentication_method")
    private String userInfoAuthenticationMethod;
    @Column(name = "user_name_attribute_name")
    @Field(name = "user_name_attribute_name")
    private String userNameAttributeName;
    @Column(name = "jwk_set_uri")
    @Field(name = "jwk_set_uri")
    private String jwkSetUri;
    @Column(name = "issuer_url")
    @Field(name = "issuer_url")
    private String issuerUri;

    @Column(name = "configuration_metadata",columnDefinition = "text")
    @Field(name = "configuration_metadata")
    private String configurationMetadata;
}
