package com.generatera.authorization.application.server.oauth2.login.config.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "client_registration")
@Entity
@Document("client_registration")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientRegistrationEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "scopes",length = 1000)
    @Field(name = "scopes")
    private String scopes;
    @Column(name = "client_name")
    @Field(name = "client_name")
    private String clientName;

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

    @Column(name = "configuration_metadata",length = 2000)
    @Field(name = "configuration_metadata")
    private String configurationMetadata;
}
