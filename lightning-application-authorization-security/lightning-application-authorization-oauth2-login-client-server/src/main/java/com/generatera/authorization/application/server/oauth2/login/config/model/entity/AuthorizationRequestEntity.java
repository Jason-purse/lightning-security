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

/**
 * 授权请求info
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "authorization_request")
@Document("authorization_request")
public class AuthorizationRequestEntity implements Serializable {
    @Id
    @MongoId
    private String id;

    @Field(name = "authorization_uri")
    @Column(name = "authorization_uri")
    private String authorizationUri;
    @Field(name = "authorization_grant_type")
    @Column(name = "authorization_grant_type")
    private String authorizationGrantType;
    @Field(name = "response_type")
    @Column(name = "response_type")
    private String responseType;
    @Field(name = "client_id")
    @Column(name = "client_id")
    private String clientId;
    @Field(name = "redirect_uri")
    @Column(name = "redirect_uri")
    private String redirectUri;
    @Field(name = "scopes")
    @Column(name = "scopes")
    private String scopes;
    @Field(name = "state")
    @Column(name = "state")
    private String state;
    @Field(name = "additional_parameters")
    @Column(name = "additional_parameters",length = 2000)
    private String additionalParameters;
    @Field(name = "authorization_request_uri")
    @Column(name = "authorization_request_uri")
    private String authorizationRequestUri;
    @Field(name = "attributes")
    @Column(name = "attributes",length = 2000)
    private String attributes;
}
