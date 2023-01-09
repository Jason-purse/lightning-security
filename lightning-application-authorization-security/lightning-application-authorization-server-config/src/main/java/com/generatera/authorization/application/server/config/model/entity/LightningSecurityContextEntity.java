package com.generatera.authorization.application.server.config.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 11:04
 * @Description 其实主要是恢复 Authentication 的内容 ...
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lightning_security_context")
@Document("lightning_security_context")
public class LightningSecurityContextEntity implements Serializable {

    @Id
    @MongoId
    @GeneratedValue(generator = "jpa-uuid")
    @GenericGenerator(name = "jpa-uuid",strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    //@Column(name = "access_token_value",length = 2000)
    //@Field("access_token_value")
    //private String accessTokenValue;
    //@Column(name = "refresh_token_value",length = 2000)
    //@Field("refresh_token_value")
    //private String refreshTokenValue;

    @Column(name = "authentication_authorities",length = 5000)
    @Field("authentication_authorities")
    private String authenticationAuthorities;
    @Column(name = "authentication_credentials",length = 1000)
    @Field("authentication_credentials")
    private String authenticationCredentials;
    @Column(name = "authentication_details",length = 2000)
    @Field("authentication_details")
    private String authenticationDetails;
    @Column(name = "authentication_principal",length = 4000)
    @Field("authentication_principal")
    private String authenticationPrincipal;
    @Column(name = "authentication_status")
    @Field("authentication_status")
    private Boolean authenticationStatus;

    @Column(name = "parser_class")
    @Field(name = "parser_class")
    private String parserClass;
}
