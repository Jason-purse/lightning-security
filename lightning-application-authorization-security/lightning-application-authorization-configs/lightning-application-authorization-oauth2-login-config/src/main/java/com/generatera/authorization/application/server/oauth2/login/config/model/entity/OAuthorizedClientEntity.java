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
@Entity
@Document("oauthorized_client")
@Table(name = "oauthorized_client")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuthorizedClientEntity implements Serializable {

    @Id
    @MongoId
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "client_registration_id")
    @Field(name = "client_registration_id")
    private String clientRegistrationId;
    /**
     * 通过 String 存储算了 ..
     */
    @Column(name = "client_registration",length = 5000)
    @Field(name = "client_registration")
    private  String clientRegistration;

    @Column(name = "principal_name")
    @Field(name = "principal_name")
    private  String principalName;
    @Column(name = "access_token")
    @Field(name = "access_token")
    private  String accessToken;

    @Column(name = "refresh_token")
    @Field(name = "refresh_token")
    private  String refreshToken;
}
